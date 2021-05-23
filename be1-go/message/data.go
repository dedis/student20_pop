package message

import (
	"crypto/sha256"
	"encoding/json"
	"fmt"

	"golang.org/x/xerrors"
)

type Stringer string

func (s Stringer) String() string {
	return string(s)
}

// DataObject represents the type for the "object" key associated with the
// message data.
type DataObject string

var (
	// LaoObject represents a "lao" message data.
	LaoObject DataObject = "lao"

	// MessageObject represents a "message" message data.
	MessageObject DataObject = "message"

	// MeetingObject represents a "meeting" message data.
	MeetingObject DataObject = "meeting"

	// RollCallObject represents a "roll call" message data.
	RollCallObject DataObject = "roll_call"

	//ElectionObject represents a "election" message data
	ElectionObject DataObject = "election"
)

// DataAction represents the type for the "action" key associated with the
// message data.
type DataAction string

// Timestamp represents the type for a timestamp in message data.
type Timestamp int64

// String returns the string representation of a timestamp.
func (t Timestamp) String() string {
	return fmt.Sprintf("%d", t)
}

// Data defines an interface for all message data types.
type Data interface {
	GetAction() DataAction

	GetObject() DataObject

	//GetTimestamp() Timestamp
}

// GenericData implements `Data` and contains fields that map to "action"
// and "object" fields in the message data
type GenericData struct {
	Action DataAction `json:"action"`
	Object DataObject `json:"object"`
}

// GetAction returns the DataAction.
func (g *GenericData) GetAction() DataAction {
	return g.Action
}

// GetObject returns the DataObject.
func (g *GenericData) GetObject() DataObject {
	return g.Object
}

// LaoDataAction represents actions associated with a "lao" data message.
type LaoDataAction DataAction

var (
	// CreateLaoAction is the action associated with the data for creating a LAO.
	CreateLaoAction LaoDataAction = "create"

	// UpdateLaoAction is the action associated with the data for updating a LAO.
	UpdateLaoAction LaoDataAction = "update_properties"

	// StateLaoAction is the action associated with the data for denoting a LAO state.
	StateLaoAction LaoDataAction = "state"
)

// CreateLAOData represents the message data used for creating a LAO.
type CreateLAOData struct {
	*GenericData

	ID        Base64URLBytes `json:"id"`
	Name      string         `json:"name"`
	Creation  Timestamp      `json:"creation"`
	Organizer PublicKey      `json:"organizer"`
	Witnesses []PublicKey    `json:"witnesses"`
}

// GetTimestamp returns the creation timestamp.
func (c *CreateLAOData) GetTimestamp() Timestamp {
	return c.Creation
}

func (c *CreateLAOData) setID() error {
	id, err := Hash(Stringer("L"), c.Organizer, c.Creation, Stringer(c.Name))
	if err != nil {
		return xerrors.Errorf("error creating hash: %v", err)
	}

	c.ID = id
	return nil
}

// UpdateLAOData represents the message data used for updating a LAO.
type UpdateLAOData struct {
	*GenericData

	ID           Base64URLBytes `json:"id"`
	Name         string         `json:"name"`
	LastModified Timestamp      `json:"last_modified"`
	Witnesses    []PublicKey    `json:"witnesses"`
}

// GetTimestamp returns the last modified timestamp.
func (u *UpdateLAOData) GetTimestamp() Timestamp {
	return u.LastModified
}

// StateLAOData represents the message data used for propagating a LAO state.
type StateLAOData struct {
	*GenericData

	ID                     Base64URLBytes           `json:"id"`
	Name                   string                   `json:"name"`
	LastModified           Timestamp                `json:"last_modified"`
	Creation               Timestamp                `json:"creation"`
	Organizer              PublicKey                `json:"organizer"`
	Witnesses              []PublicKey              `json:"witnesses"`
	ModificationID         Base64URLBytes           `json:"modification_id"`
	ModificationSignatures []PublicKeySignaturePair `json:"modification_signatures"`
}

// GetTimestamp returns the last modified timestamp.
func (s *StateLAOData) GetTimestamp() Timestamp {
	return s.LastModified
}

// MeetingDataAction represents actions associated with a "meeting" data message.
type MeetingDataAction DataAction

var (
	// CreateMeetingAction is the action associated with the data for creating a meeting.
	CreateMeetingAction MeetingDataAction = "create"

	// UpdateMeetingAction is the action associated with the data for updating a meeting.
	UpdateMeetingAction MeetingDataAction = "update_properties"

	// StateMeetingAction is the action associated with the data for closing a meeting.
	StateMeetingAction MeetingDataAction = "state"
)

// CreateMeetingData represents the message data used for creating a meeting.
type CreateMeetingData struct {
	*GenericData

	ID       Base64URLBytes `json:"id"`
	Name     string         `json:"name"`
	Creation Timestamp      `json:"creation"`
	Location string         `json:"location"`

	Start Timestamp `json:"start"`
	End   Timestamp `json:"end"`

	Extra json.RawMessage `json:"extra"`
}

// GetTimestamp returns the creation timestamp.
func (c *CreateMeetingData) GetTimestamp() Timestamp {
	return c.Creation
}

// StateMeetingData represents the message data used for propagating a meeting state.
type StateMeetingData struct {
	*GenericData

	ID       Base64URLBytes `json:"id"`
	Name     string         `json:"name"`
	Creation Timestamp      `json:"creation"`
	Location string         `json:"location"`

	Start Timestamp `json:"start"`
	End   Timestamp `json:"end"`

	ModificationID         Base64URLBytes           `json:"modification_id"`
	ModificationSignatures []PublicKeySignaturePair `json:"modification_signatures"`

	Extra json.RawMessage `json:"extra"`
}

// GetTimestamp returns the creation timestamp.
func (s *StateMeetingData) GetTimestamp() Timestamp {
	return s.Creation
}

// RollCallAction represents the actions associated with a "roll call" data message.
type RollCallAction DataAction

var (
	// CreateRollCallAction represents the action associated with the data for creating a roll call.
	CreateRollCallAction RollCallAction = "create"

	// CloseRollCallAction represents the action associated with the data for closing a roll call.
	CloseRollCallAction RollCallAction = "close"
)

// CreateRollCallData represents the message data used for creating a roll call.
type CreateRollCallData struct {
	*GenericData

	ID            Base64URLBytes `json:"id"`
	Name          string         `json:"name"`
	Creation      Timestamp      `json:"creation"`
	ProposedStart Timestamp      `json:"proposed_start"`
	ProposedEnd   Timestamp      `json:"proposed_end"`
	Location      string         `json:"location"`
	Description   string         `json:"description,omitempty"`
}

// OpenRollCallActionType represents the actions associated with opening or
// reopening a roll call.
type OpenRollCallActionType RollCallAction

var (
	// OpenRollCallAction reprents the action associated with the data for opening a roll call.
	OpenRollCallAction OpenRollCallActionType = "open"

	// ReopenRollCallAction reprents the action associated with the data for reopening a roll call.
	ReopenRollCallAction OpenRollCallActionType = "reopen"
)

// OpenRollCallData represents the message data used for opening a roll call.
type OpenRollCallData struct {
	*GenericData

	UpdateID Base64URLBytes `json:"update_id"`
	Opens    Base64URLBytes `json:"opens"`
	OpenedAt Timestamp      `json:"opened_at"`
}

// CloseRollCallData represents the message data used for closing a roll call.
type CloseRollCallData struct {
	*GenericData

	UpdateID  Base64URLBytes `json:"update_id"`
	Closes    Base64URLBytes `json:"closes"`
	ClosedAt  Timestamp      `json:"closed_at"`
	Attendees []PublicKey    `json:"attendees"`
}

// MessageDataAction represents the actions associated with a "message" data message.
type MessageDataAction DataAction

var (
	// WitnessAction represents the action associated with the data for witnessing a message.
	WitnessAction MessageDataAction = "witness"
)

// WitnessMessageData represents the message data used for witnessing a message.
type WitnessMessageData struct {
	*GenericData

	MessageID Base64URLBytes `json:"message_id"`
	Signature Signature      `json:"signature"`
}

// ElectionAction represents the action associated with an "election" data message.
type ElectionAction DataAction

var (
	// ElectionSetupAction represents the action associated with the data for setting up an election.
	ElectionSetupAction ElectionAction = "setup"

	// CastVoteAction represents the action associated with the data for casting a vote in
	// an election.
	CastVoteAction ElectionAction = "cast_vote"

	// ElectionEndAction represents the action associated with the data for ending an election.
	ElectionEndAction ElectionAction = "end"

	// ElectionResultAction represents the action associated with the data for the tallying of
	// an election.
	ElectionResultAction ElectionAction = "result"
)

// VotingMethod represents the method used for a particular vote.
type VotingMethod string

var (
	// PluralityMethod represents the relative majority voting method.
	PluralityMethod VotingMethod = "Plurality"
	// ApprovalMethod represents a single-winner electoral system where each voter may
	// approve any number of ballot options.
	ApprovalMethod VotingMethod = "Approval"
)

// BallotOption represents a response option to a question.
type BallotOption string

// Question represents a question that is asked during an election.
type Question struct {
	ID            PublicKey      `json:"id"`
	QuestionAsked string         `json:"question"`
	VotingMethod  VotingMethod   `json:"voting_method"`
	BallotOptions []BallotOption `json:"ballot_options"`
	WriteIn       bool           `json:"write_in"`
}

// ElectionSetupData represents the message data used for setting up an election.
type ElectionSetupData struct {
	*GenericData

	ID        Base64URLBytes `json:"id"`
	LaoID     Base64URLBytes `json:"lao"`
	Name      string         `json:"name"`
	Version   string         `json:"version"`
	CreatedAt Timestamp      `json:"created_at"`
	StartTime Timestamp      `json:"start_time"`
	EndTime   Timestamp      `json:"end_time"`
	Questions []Question     `json:"questions"`
}

// Vote represents a vote in an election.
type Vote struct {
	ID          PublicKey      `json:"id"`
	QuestionID  Base64URLBytes `json:"question"`
	VoteIndexes []int          `json:"vote"`
	WriteIn     string         `json:"write_in"`
}

// CastVoteData represents the message data used for casting a vote during an election.
type CastVoteData struct {
	*GenericData
	LaoID      Base64URLBytes `json:"lao"`
	ElectionID Base64URLBytes `json:"election"`
	CreatedAt  Timestamp      `json:"created_at"`
	Votes      []Vote         `json:"votes"`
}

// ElectionEndData represents the message data used for ending an election.
type ElectionEndData struct {
	*GenericData
	LaoID           Base64URLBytes `json:"lao"`
	ElectionID      Base64URLBytes `json:"election"`
	CreatedAt       Timestamp      `json:"created_at"`
	RegisteredVotes Base64URLBytes `json:"registered_votes"`
}

// QuestionResult represents the result of a question in an election.
type QuestionResult struct {
	ID     PublicKey      `json:"id"`
	Result []BallotOption `json:"result"`
}

// ElectionResultData represents the message data for the result of an election.
type ElectionResultData struct {
	*GenericData
	Questions         []QuestionResult         `json:"questions"`
	WitnessSignatures []PublicKeySignaturePair `json:"witness_signatures"`
}

// NewCreateLAOData returns an instance of `CreateLAOData`.
func NewCreateLAOData(name string, creation Timestamp, organizer PublicKey, witnesses []PublicKey) (*CreateLAOData, error) {
	create := &CreateLAOData{
		GenericData: &GenericData{
			Action: DataAction(CreateLaoAction),
			Object: LaoObject,
		},
		Name:      name,
		Creation:  creation,
		Organizer: organizer,
		Witnesses: witnesses,
	}

	err := create.setID()
	if err != nil {
		return nil, xerrors.Errorf("failed to set ID for CreateLAOData: %v", err)
	}

	return create, nil
}

func Hash(strs ...fmt.Stringer) ([]byte, error) {
	h := sha256.New()
	for i, str := range strs {
		s := str.String()
		if len(s) == 0 {
			return nil, xerrors.Errorf("empty string to hash() at index: %d, %v", i, strs)
		}
		h.Write([]byte(fmt.Sprintf("%d%s", len(s), s)))
	}
	return h.Sum(nil), nil
}
