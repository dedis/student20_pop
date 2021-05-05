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

func newElectionSetupData(id []byte, creation, start, end message.Timestamp, name string, questions []message.Question) *message.ElectionSetupData {
	data := &message.ElectionSetupData{
		GenericData: &message.GenericData{
			Action: message.DataAction(message.ElectionSetupAction),
			Object: message.ElectionObject,
		},
		ID:        id,
		Name:      name,
		Version:   "1.0.0",
		CreatedAt: creation,
		StartTime: start,
		EndTime:   end,
		Questions: questions,
	}

	return data
}

func newQuestion(id []byte, question string, votingMethod string, options []string, writeIn bool) *message.Question {
	var ballotOptions []message.BallotOption
	for _, option := range options {
		ballotOptions = append(ballotOptions, message.BallotOption(option))
	}

	data := &message.Question{
		ID:            id,
		QuestionAsked: question,
		VotingMethod:  message.VotingMethod(votingMethod),
		BallotOptions: ballotOptions,
		WriteIn:       writeIn,
	}

	return data
}

func newCastVote(laoID, electionID []byte, creation message.Timestamp, votes []message.Vote, questions []message.Question) *message.CastVoteData {
	data := &message.CastVoteData{
		GenericData: &message.GenericData{
			Action: message.DataAction(message.CastVoteAction),
			Object: message.ElectionObject,
		},
		LaoID:      laoID,
		ElectionID: electionID,
		CreatedAt:  creation,
		Votes:      votes,
	}

	return data
}

func newVote(id, questionID []byte, creation message.Timestamp, voteIndexes []int, writeIn string) *message.Vote {
	vote := &message.Vote{
		ID:          id,
		QuestionID:  questionID,
		VoteIndexes: voteIndexes,
		WriteIn:     writeIn,
	}

	return vote
}

func newElectionEndData(laoID, electionID, registeredVotes []byte, creation message.Timestamp, voteIndexes []int) *message.ElectionEndData {
	data := &message.ElectionEndData{
		GenericData: &message.GenericData{
			Action: message.DataAction(message.ElectionEndAction),
			Object: message.ElectionObject,
		},
		LaoID:           laoID,
		ElectionID:      electionID,
		CreatedAt:       creation,
		RegisteredVotes: registeredVotes,
	}

	return data
}

func newElectionResultData(questionsResults []message.QuestionResult, witnessSignatures []message.PublicKeySignaturePair) *message.ElectionResultData {
	data := &message.ElectionResultData{
		GenericData: &message.GenericData{
			Action: message.DataAction(message.ElectionResultAction),
			Object: message.ElectionObject,
		},
		Questions:         questionsResults,
		WitnessSignatures: witnessSignatures,
	}

	return data
}

func newQuestionResult(id []byte, results []string) *message.QuestionResult {
	var ballotOptions []message.BallotOption
	for _, option := range results {
		ballotOptions = append(ballotOptions, message.BallotOption(option))
	}

	questionResult := &message.QuestionResult{
		ID:     id,
		Result: ballotOptions,
	}

	return questionResult
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
