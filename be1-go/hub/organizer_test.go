package hub

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
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

func newElectionSetupData(id, laoID []byte, creation, start, end message.Timestamp, name string, questions []message.Question) *message.ElectionSetupData {
	return &message.ElectionSetupData{
		GenericData: &message.GenericData{
			Action: message.DataAction(message.ElectionSetupAction),
			Object: message.ElectionObject,
		},
		ID:        id,
		LaoID:     laoID,
		Name:      name,
		Version:   "1.0.0",
		CreatedAt: creation,
		StartTime: start,
		EndTime:   end,
		Questions: questions,
	}
}

func newCorrectElectionSetupData(laoID string, creation, start, end message.Timestamp, questions []message.Question) (*message.ElectionSetupData, error) {
	name := "My election"
	id, err := getElectionSetupID(laoID, name, creation)
	if err != nil {
		return nil, err
	}

	for _, question := range questions {
		questionId, err := getQuestionID(id, question.QuestionAsked)
		if err != nil {
			return nil, err
		}
		question.ID = questionId
	}

	byteLaoID, err := base64.URLEncoding.DecodeString(laoID)
	if err != nil {
		return nil, err
	}

	return newElectionSetupData(id, byteLaoID, creation, start, end, name, questions), nil
}

func getElectionSetupID(laoID, name string, timestamp message.Timestamp) ([]byte, error) {
	id, err := message.Hash(message.Stringer("Election"), message.Stringer(laoID), timestamp, message.Stringer(name))
	if err != nil {
		return nil, err
	}
	return id, nil
}

func getElectionID(data *message.ElectionSetupData) string {
	return base64.URLEncoding.EncodeToString(data.ID)
}

func newQuestion(question string, votingMethod string, options []string, writeIn bool) *message.Question {
	var ballotOptions []message.BallotOption
	for _, option := range options {
		ballotOptions = append(ballotOptions, message.BallotOption(option))
	}

	return &message.Question{
		QuestionAsked: question,
		VotingMethod:  message.VotingMethod(votingMethod),
		BallotOptions: ballotOptions,
		WriteIn:       writeIn,
	}

}

func getQuestionID(electionID []byte, name string) ([]byte, error) {
	id, err := message.Hash(message.Stringer("Question"), message.Stringer(electionID), message.Stringer(name))
	if err != nil {
		return nil, err
	}
	return id, nil
}

func newCastVote(laoID, electionID []byte, creation message.Timestamp, votes []message.Vote, questions []message.Question) *message.CastVoteData {
	return &message.CastVoteData{
		GenericData: &message.GenericData{
			Action: message.DataAction(message.CastVoteAction),
			Object: message.ElectionObject,
		},
		LaoID:      laoID,
		ElectionID: electionID,
		CreatedAt:  creation,
		Votes:      votes,
	}
}

func newVote(id, questionID []byte, creation message.Timestamp, voteIndexes []int, writeIn string) *message.Vote {
	return &message.Vote{
		ID:          id,
		QuestionID:  questionID,
		VoteIndexes: voteIndexes,
		WriteIn:     writeIn,
	}
}

func newElectionEndData(laoID, electionID, registeredVotes []byte, creation message.Timestamp, voteIndexes []int) *message.ElectionEndData {
	return &message.ElectionEndData{
		GenericData: &message.GenericData{
			Action: message.DataAction(message.ElectionEndAction),
			Object: message.ElectionObject,
		},
		LaoID:           laoID,
		ElectionID:      electionID,
		CreatedAt:       creation,
		RegisteredVotes: registeredVotes,
	}
}

func newElectionResultData(questionsResults []message.QuestionResult, witnessSignatures []message.PublicKeySignaturePair) *message.ElectionResultData {
	return &message.ElectionResultData{
		GenericData: &message.GenericData{
			Action: message.DataAction(message.ElectionResultAction),
			Object: message.ElectionObject,
		},
		Questions:         questionsResults,
		WitnessSignatures: witnessSignatures,
	}
}

func newQuestionResult(id []byte, results []string) *message.QuestionResult {
	var ballotOptions []message.BallotOption
	for _, option := range results {
		ballotOptions = append(ballotOptions, message.BallotOption(option))
	}

	return &message.QuestionResult{
		ID:     id,
		Result: ballotOptions,
	}
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

func requireChannel(t *testing.T, channelID string) {
	_, ok := oHub.channelByID[channelID]
	require.True(t, ok, "The channel doesn't exist")
}

func requireErrorCode(t *testing.T, err error, code int) {
	msgError := &message.Error{}
	require.True(t, xerrors.As(err, &msgError), "The error not of the form `message.Error{}`")
	require.Equal(t, msgError.Code, code, fmt.Sprintf("The error should have the code %v", code))
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
	_, _, err := createLao(oHub, organizerKeyPair, "my lao")
	require.NoError(t, err)
}

// Basic test Setup Election
func TestOrganizer_SetupElection(t *testing.T) {
	laoID, laoChannel, err := createLao(oHub, organizerKeyPair, "Lao SetupElection")
	require.NoError(t, err)

	// Create a question
	question := newQuestion("Yes or no?", "Plurality", []string{"yes", "no"}, false)

	// Setup election data
	time := timestamp()
	data, err := newCorrectElectionSetupData(laoID, time, time, time, []message.Question{*question})
	require.NoError(t, err)

	msg := createMessage(data, organizerKeyPair.publicBuf)
	err = laoChannel.processElectionObject(msg)
	require.NoError(t, err)

	electionID := getElectionID(data)
	requireChannel(t, laoID+"/"+electionID)
}

// Check that an election with no question is not accepted
func TestOrganizer_SetupElectionNoQuestion(t *testing.T) {
	laoID, laoChannel, err := createLao(oHub, organizerKeyPair, "Lao SetupElectionNoQuestion")
	require.NoError(t, err)

	// Setup election data
	time := timestamp()
	data, err := newCorrectElectionSetupData(laoID, time, time, time, []message.Question{})
	require.NoError(t, err)

	msg := createMessage(data, organizerKeyPair.publicBuf)
	err = laoChannel.processElectionObject(msg)
	require.Error(t, err, "It should not be possible to create an election with no question")
	// Check that the error code is "-4 request data is invalid".
	requireErrorCode(t, err, -4)
}

// Check that only the organizer is able to setup an election
func TestOrganizer_SetupElectionWrongSender(t *testing.T) {
	laoID, laoChannel, err := createLao(oHub, organizerKeyPair, "Lao ElectionWrongSender")
	require.NoError(t, err)

	// Generate keypair
	keypair, err := generateKeyPair()
	require.NoError(t, err)

	// Create a question
	question := newQuestion("Yes or no?", "Plurality", []string{"yes", "no"}, false)

	// Setup election data
	time := timestamp()
	data, err := newCorrectElectionSetupData(laoID, time, time, time, []message.Question{*question})
	require.NoError(t, err)

	msg := createMessage(data, keypair.publicBuf)
	err = laoChannel.processElectionObject(msg)
	require.Error(t, err, "Only the organizer should be able to create an election")
	// Check that the error code is "-5 access denied".
	requireErrorCode(t, err, -5)
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
		_, ok := laoChannel.attendees[string(attendee)]
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
		_, ok := laoChannel.attendees[string(attendee)]
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
	err = laoChannel.processRollCallObject(msg)
	require.Error(t, err)
	require.Equal(t, string(laoChannel.rollCall.state), "")
	require.Equal(t, string(laoChannel.rollCall.id), "")
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
