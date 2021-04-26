package hub

import (
	"encoding/base64"
	"encoding/json"
	"os"
	"strconv"
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

var laoCounter = 0

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

func getTime() message.Timestamp {
	return message.Timestamp(time.Now().Unix())
}

func createLao(o *organizerHub, oKeypair keypair) (string, *laoChannel, error) {
	// Data of the Lao
	name := strconv.Itoa(laoCounter)
	laoCounter += 1
	creation := getTime()
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
		Creation:  getTime(),
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
	id := base64.StdEncoding.EncodeToString(laoID)

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
	creation := getTime()
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
	closedAt := getTime()
	id, err := message.Hash(message.Stringer('R'), message.Stringer(laoID), message.Stringer(prevID), closedAt)
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
	openedAt := getTime()
	id, err := message.Hash(message.Stringer('R'), message.Stringer(laoID), message.Stringer(prevID), openedAt)
	if err != nil {
		return nil, err
	}
	return newOpenRollCallData(id, prevID, openedAt, action), nil
}

func TestMain(m *testing.M) {
	organizerKeyPair, _ = generateKeyPair()

	oHub = &organizerHub{
		messageChan: make(chan IncomingMessage),
		channelByID: make(map[string]Channel),
		public:      organizerKeyPair.public,
	}

	res := m.Run()
	os.Exit(res)
}

func TestOrganizer_CreateLAO(t *testing.T) {
	_, _, err := createLao(oHub, organizerKeyPair)
	require.NoError(t, err)
}

// test Created → Opened → Closed → Reopened → Closed
func TestOrganizer_RollCall(t *testing.T) {
	laoID, laoChannel, err := createLao(oHub, organizerKeyPair)
	require.NoError(t, err)

	// Create
	dataCreate, err := newCorrectCreateRollCallData(laoID)
	require.NoError(t, err)
	err = laoChannel.processRollCallObject(organizerKeyPair.publicBuf, dataCreate)
	require.NoError(t, err)
	require.Equal(t, laoChannel.rollCall.state, Created)
	require.Equal(t, laoChannel.rollCall.id, string(dataCreate.ID))

	// Open
	dataOpen, err := newCorrectOpenRollCallData(laoID, dataCreate.ID, message.OpenRollCallAction)
	require.NoError(t, err)
	err = laoChannel.processRollCallObject(organizerKeyPair.publicBuf, dataOpen)
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
	err = laoChannel.processRollCallObject(organizerKeyPair.publicBuf, dataClose1)
	require.NoError(t, err)
	require.Equal(t, laoChannel.rollCall.state, Closed)
	require.Equal(t, laoChannel.rollCall.id, string(dataClose1.UpdateID))
	for _, attendee := range attendees[:8] {
		_, ok := laoChannel.attendees[string(attendee)]
		require.True(t, ok)
	}

	// Reopen
	dataReopen, err := newCorrectOpenRollCallData(laoID, dataClose1.UpdateID, message.ReopenRollCallAction)
	require.NoError(t, err)
	err = laoChannel.processRollCallObject(organizerKeyPair.publicBuf, dataReopen)
	require.NoError(t, err)
	require.Equal(t, laoChannel.rollCall.state, Open)
	require.Equal(t, laoChannel.rollCall.id, string(dataReopen.UpdateID))

	// Close
	dataClose2, err := newCorrectCloseRollCallData(laoID, dataReopen.UpdateID, attendees)
	require.NoError(t, err)
	err = laoChannel.processRollCallObject(organizerKeyPair.publicBuf, dataClose2)
	require.NoError(t, err)
	require.Equal(t, laoChannel.rollCall.state, Closed)
	require.Equal(t, laoChannel.rollCall.id, string(dataClose2.UpdateID))
	for _, attendee := range attendees {
		_, ok := laoChannel.attendees[string(attendee)]
		require.True(t, ok)
	}
}

func TestOrganizer_CreateRollCallWrongID(t *testing.T) {
	_, laoChannel, err := createLao(oHub, organizerKeyPair)
	require.NoError(t, err)

	// create the roll call
	id := []byte{1}
	dataCreate := newCreateRollCallData(id, getTime(), "my roll call")
	err = laoChannel.processRollCallObject(organizerKeyPair.publicBuf, dataCreate)
	require.Error(t, err)
	require.Equal(t, string(laoChannel.rollCall.state), "")
	require.Equal(t, string(laoChannel.rollCall.id), "")
}

func TestOrganizer_CreateRollCallWrongSender(t *testing.T) {
	laoID, laoChannel, err := createLao(oHub, organizerKeyPair)
	require.NoError(t, err)

	keypair, err := generateKeyPair()
	require.NoError(t, err)

	// Create the roll call
	dataCreate, err := newCorrectCreateRollCallData(laoID)
	require.NoError(t, err)
	err = laoChannel.processRollCallObject(keypair.publicBuf, dataCreate)
	require.Error(t, err)
	require.Equal(t, string(laoChannel.rollCall.state), "")
	require.Equal(t, string(laoChannel.rollCall.id), "")
}

func TestOrganizer_RollCallWrongInstructions(t *testing.T) {
	laoID, laoChannel, err := createLao(oHub, organizerKeyPair)
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
				err = laoChannel.processRollCallObject(organizerKeyPair.publicBuf, data[j])
				require.Error(t, err)
			}
		}
		// Process the correct data
		err = laoChannel.processRollCallObject(organizerKeyPair.publicBuf, data[i])
		require.NoError(t, err)

	}

}