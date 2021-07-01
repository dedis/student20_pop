package hub

import (
	"encoding/base64"
	"encoding/json"
	"os"
	"student20_pop"
	"student20_pop/message"
	"testing"
	"time"

	"github.com/stretchr/testify/require"
	"go.dedis.ch/kyber/v3"
	"go.dedis.ch/kyber/v3/sign/schnorr"
	"golang.org/x/xerrors"
)

type keypair struct {
	public    kyber.Point
	publicBuf message.PublicKey
	private   kyber.Scalar
}

var organizerKeyPair keypair

var suite = student20_pop.Suite

var oHub *organizerHub

func generateKeyPair() (keypair, error) {
	secret := suite.Scalar().Pick(suite.RandomStream())
	point := suite.Point().Pick(suite.RandomStream())
	point = point.Mul(secret, point)

	pkbuf, err := point.MarshalBinary()
	if err != nil {
		return keypair{}, xerrors.Errorf("failed to create keypair: %v", err)
	}
	return keypair{point, pkbuf, secret}, nil
}

func timestamp() message.Timestamp {
	return message.Timestamp(time.Now().Unix())
}

func createLao(o *organizerHub, oKeypair keypair, name string) (string, *laoChannel, error) {
	// Data of the Lao
	creation := timestamp()
	laoID, err := message.Hash(message.Stringer("L"), oKeypair.publicBuf, creation, message.Stringer(name))
	if err != nil {
		return "", nil, err
	}
	// Creation of the data
	data := &message.CreateLAOData{
		GenericData: &message.GenericData{
			Action: message.DataAction(message.CreateLaoAction),
			Object: message.LaoObject,
		},
		ID:        laoID,
		Name:      name,
		Creation:  creation,
		Organizer: oKeypair.publicBuf,
		Witnesses: nil,
	}

	dataBuf, err := json.Marshal(data)
	if err != nil {
		return "", nil, err
	}

	signature, err := schnorr.Sign(suite, oKeypair.private, dataBuf)
	if err != nil {
		return "", nil, err
	}

	msg := &message.Message{
		Data:              data,
		Sender:            oKeypair.publicBuf,
		Signature:         signature,
		WitnessSignatures: nil,
	}

	publish := message.Publish{
		ID:     1,
		Method: "publish",
		Params: message.Params{
			Channel: "/root",
			Message: msg,
		},
	}
	o.createLao(publish)
	id := base64.URLEncoding.EncodeToString(laoID)

	channel, ok := oHub.channelByID[id]
	if !ok {
		return "", nil, xerrors.Errorf("Could not extract the channel of the lao")
	}
	laoChannel := channel.(*laoChannel)

	return id, laoChannel, nil
}

func newCreateRollCallData(id []byte, creation message.Timestamp, name string) *message.CreateRollCallData {
	data := &message.CreateRollCallData{
		GenericData: &message.GenericData{
			Action: message.DataAction(message.CreateRollCallAction),
			Object: message.RollCallObject,
		},
		ID:            id,
		Name:          name,
		Creation:      creation,
		ProposedStart: creation,
		ProposedEnd:   creation,
		Location:      "EPFL",
	}

	return data
}

func newCorrectCreateRollCallData(laoID string) (*message.CreateRollCallData, error) {
	name := "my_roll_call"
	creation := timestamp()
	id, err := message.Hash(message.Stringer('R'), message.Stringer(laoID), creation, message.Stringer(name))
	if err != nil {
		return nil, err
	}

	return newCreateRollCallData(id, creation, name), nil
}

func newCloseRollCallData(id []byte, prevID []byte, closedAt message.Timestamp, attendees []message.PublicKey) *message.CloseRollCallData {
	data := &message.CloseRollCallData{
		GenericData: &message.GenericData{
			Action: message.DataAction(message.CloseRollCallAction),
			Object: message.RollCallObject,
		},
		UpdateID:  id,
		Closes:    prevID,
		ClosedAt:  closedAt,
		Attendees: attendees,
	}

	return data
}

func newCorrectCloseRollCallData(laoID string, prevID []byte, attendees []message.PublicKey) (*message.CloseRollCallData, error) {
	closedAt := timestamp()
	prevID64 := base64.URLEncoding.EncodeToString(prevID)
	id, err := message.Hash(message.Stringer('R'), message.Stringer(laoID), message.Stringer(prevID64), closedAt)
	if err != nil {
		return nil, err
	}
	return newCloseRollCallData(id, prevID, closedAt, attendees), nil
}

func newOpenRollCallData(id []byte, prevID []byte, openedAt message.Timestamp, action message.OpenRollCallActionType) *message.OpenRollCallData {
	data := &message.OpenRollCallData{
		GenericData: &message.GenericData{
			Action: message.DataAction(action),
			Object: message.RollCallObject,
		},
		UpdateID: id,
		Opens:    prevID,
		OpenedAt: openedAt,
	}

	return data
}

func newCorrectOpenRollCallData(laoID string, prevID []byte, action message.OpenRollCallActionType) (*message.OpenRollCallData, error) {
	openedAt := timestamp()
	prevID64 := base64.URLEncoding.EncodeToString(prevID)
	id, err := message.Hash(message.Stringer('R'), message.Stringer(laoID), message.Stringer(prevID64), openedAt)
	if err != nil {
		return nil, err
	}
	return newOpenRollCallData(id, prevID, openedAt, action), nil
}

func createMessage(data message.Data, publicKey message.PublicKey) message.Message {
	return message.Message{
		MessageID:         []byte{1, 2, 3},
		Data:              data,
		Sender:            publicKey,
		Signature:         []byte{1, 2, 3},
		WitnessSignatures: []message.PublicKeySignaturePair{},
	}
}

func TestMain(m *testing.M) {
	organizerKeyPair, _ = generateKeyPair()

	baseHub, err := NewBaseHub(organizerKeyPair.public)
	if err != nil {
		panic(err)
	}

	oHub = &organizerHub{
		baseHub: baseHub,
	}

	res := m.Run()
	os.Exit(res)
}

func TestOrganizer_CreateLAO(t *testing.T) {
	_, _, err := createLao(oHub, organizerKeyPair, "my lao")
	require.NoError(t, err)
}

// test Created → Opened → Closed → Reopened → Closed
func TestOrganizer_RollCall(t *testing.T) {
	laoID, laoChannel, err := createLao(oHub, organizerKeyPair, "lao roll call")
	require.NoError(t, err)

	// Create
	dataCreate, err := newCorrectCreateRollCallData(laoID)
	require.NoError(t, err)
	msg := createMessage(dataCreate, organizerKeyPair.publicBuf)
	err = laoChannel.processRollCallObject(msg)
	require.NoError(t, err)
	require.Equal(t, laoChannel.rollCall.state, Created)
	require.Equal(t, laoChannel.rollCall.id, string(dataCreate.ID))

	// Open
	dataOpen, err := newCorrectOpenRollCallData(laoID, dataCreate.ID, message.OpenRollCallAction)
	require.NoError(t, err)
	msg = createMessage(dataOpen, organizerKeyPair.publicBuf)
	err = laoChannel.processRollCallObject(msg)
	require.NoError(t, err)
	require.Equal(t, laoChannel.rollCall.state, Open)
	require.Equal(t, laoChannel.rollCall.id, string(dataOpen.UpdateID))

	// Generate public keys
	var attendees []message.PublicKey

	for i := 0; i < 10; i++ {
		keypair, err := generateKeyPair()
		require.NoError(t, err)
		attendees = append(attendees, keypair.publicBuf)
	}

	// Close
	dataClose1, err := newCorrectCloseRollCallData(laoID, dataOpen.UpdateID, attendees[:8])
	require.NoError(t, err)
	msg = createMessage(dataClose1, organizerKeyPair.publicBuf)
	err = laoChannel.processRollCallObject(msg)
	require.NoError(t, err)
	require.Equal(t, laoChannel.rollCall.state, Closed)
	require.Equal(t, laoChannel.rollCall.id, string(dataClose1.UpdateID))

	for _, attendee := range attendees[:8] {
		ok := laoChannel.attendees.IsPresent(attendee.String())
		require.True(t, ok)
	}

	// Reopen
	dataReopen, err := newCorrectOpenRollCallData(laoID, dataClose1.UpdateID, message.ReopenRollCallAction)
	require.NoError(t, err)
	msg = createMessage(dataReopen, organizerKeyPair.publicBuf)
	err = laoChannel.processRollCallObject(msg)
	require.NoError(t, err)
	require.Equal(t, laoChannel.rollCall.state, Open)
	require.Equal(t, laoChannel.rollCall.id, string(dataReopen.UpdateID))

	// Close
	dataClose2, err := newCorrectCloseRollCallData(laoID, dataReopen.UpdateID, attendees)
	require.NoError(t, err)
	msg = createMessage(dataClose2, organizerKeyPair.publicBuf)
	err = laoChannel.processRollCallObject(msg)
	require.NoError(t, err)
	require.Equal(t, laoChannel.rollCall.state, Closed)
	require.Equal(t, laoChannel.rollCall.id, string(dataClose2.UpdateID))

	for _, attendee := range attendees {
		ok := laoChannel.attendees.IsPresent(attendee.String())
		require.True(t, ok)
	}
}

func TestOrganizer_CreateRollCallWrongID(t *testing.T) {
	_, laoChannel, err := createLao(oHub, organizerKeyPair, "lao roll call wrong id")
	require.NoError(t, err)

	// create the roll call
	id := []byte{1}
	dataCreate := newCreateRollCallData(id, timestamp(), "my roll call")
	msg := createMessage(dataCreate, organizerKeyPair.publicBuf)
	err = laoChannel.idValidator.VerifyIDs(&msg)
	require.Error(t, err)
}

func TestOrganizer_CreateRollCallWrongSender(t *testing.T) {
	laoID, laoChannel, err := createLao(oHub, organizerKeyPair, "lao roll call wrong sender")
	require.NoError(t, err)

	keypair, err := generateKeyPair()
	require.NoError(t, err)

	// Create the roll call
	dataCreate, err := newCorrectCreateRollCallData(laoID)
	require.NoError(t, err)
	msg := createMessage(dataCreate, keypair.publicBuf)
	err = laoChannel.processRollCallObject(msg)
	require.Error(t, err)
	require.Equal(t, string(laoChannel.rollCall.state), "")
	require.Equal(t, string(laoChannel.rollCall.id), "")
}

func TestOrganizer_RollCallWrongInstructions(t *testing.T) {
	laoID, laoChannel, err := createLao(oHub, organizerKeyPair, "lao roll call swrong instructions")
	require.NoError(t, err)

	// Create all the data
	dataCreate, err := newCorrectCreateRollCallData(laoID)
	require.NoError(t, err)

	dataOpen, err := newCorrectOpenRollCallData(laoID, dataCreate.ID, message.OpenRollCallAction)
	require.NoError(t, err)

	dataClose, err := newCorrectCloseRollCallData(laoID, dataOpen.UpdateID, []message.PublicKey{})
	require.NoError(t, err)

	dataReopen, err := newCorrectOpenRollCallData(laoID, dataClose.UpdateID, message.ReopenRollCallAction)
	require.NoError(t, err)

	data := []message.Data{dataCreate, dataOpen, dataClose, dataReopen}

	for i := 0; i < len(data); i += 1 {
		for j := 0; j < len(data); j += 1 {
			if j != 0 && j != i {
				// Try to process all the data that cannot be processed at this time
				msg := createMessage(data[j], organizerKeyPair.publicBuf)
				err = laoChannel.processRollCallObject(msg)
				require.Error(t, err)
			}
		}
		// Process the correct data
		msg := createMessage(data[i], organizerKeyPair.publicBuf)
		err = laoChannel.processRollCallObject(msg)
		require.NoError(t, err)

	}
}

func TestOrganizer_RollCallProposedStartEnd(t *testing.T) {
	_, laoChannel, err := createLao(oHub, organizerKeyPair, "lao roll call proposed start")
	require.NoError(t, err)

	creation := timestamp()

	// create CreatRollCall data with ProposedStart > ProposedEnd
	dataCreate := &message.CreateRollCallData{
		GenericData: &message.GenericData{
			Action: message.DataAction(message.CreateRollCallAction),
			Object: message.RollCallObject,
		},
		ID:            []byte{1},
		Name:          "my roll call",
		Creation:      creation,
		ProposedStart: creation + 10,
		ProposedEnd:   creation,
		Location:      "EPFL",
	}
	msg := createMessage(dataCreate, organizerKeyPair.publicBuf)
	err = laoChannel.processRollCallObject(msg)
	require.Error(t, err)
}
