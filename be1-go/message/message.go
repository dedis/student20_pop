package message

import (
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"student20_pop"

	"go.dedis.ch/kyber/v3/sign/schnorr"
	"golang.org/x/xerrors"
)

type Base64URLBytes []byte

type internalMessage struct {
	MessageID         Base64URLBytes           `json:"message_id"`
	Data              Base64URLBytes           `json:"data"`
	Sender            PublicKey                `json:"sender"`
	Signature         Signature                `json:"signature"`
	WitnessSignatures []PublicKeySignaturePair `json:"witness_signatures"`
}

// Message represents the `params.Message` field in the payload.
type Message struct {
	MessageID         Base64URLBytes           `json:"message_id"`
	Data              Data                     `json:"data"`
	Sender            PublicKey                `json:"sender"`
	Signature         Signature                `json:"signature"`
	WitnessSignatures []PublicKeySignaturePair `json:"witness_signatures"`
	RawData           Base64URLBytes
}

// NewMessage returns an instance of a `Message`.
func NewMessage(sender PublicKey, signature Signature, witnessSignatures []PublicKeySignaturePair, data Data) (*Message, error) {
	msg := &Message{
		Data:              data,
		Sender:            sender,
		Signature:         signature,
		WitnessSignatures: witnessSignatures,
	}

	h := sha256.New()

	dataBuf, err := json.Marshal(data)
	if err != nil {
		return nil, xerrors.Errorf("failed to marshal data: %v", err)
	}

	h.Write(dataBuf)
	h.Write(signature)

	idBuf := h.Sum(nil)

	msg.MessageID = idBuf

	return msg, nil
}

// MarshalJSON implements custom marshaling logic for a Message.
func (m Message) MarshalJSON() ([]byte, error) {

	var dataBuf []byte
	if m.RawData != nil {
		dataBuf = m.RawData
	} else {
		buf, err := json.Marshal(m.Data)
		if err != nil {
			return nil, xerrors.Errorf("error marshaling data: %v", err)
		}
		dataBuf = buf
	}

	tmp := internalMessage{
		MessageID:         m.MessageID,
		Sender:            m.Sender,
		Signature:         m.Signature,
		WitnessSignatures: m.WitnessSignatures,
		Data:              dataBuf,
	}
	return json.Marshal(tmp)
}

// UnmarshalJSON implements custom unmarshaling logic for a Message.
func (m *Message) UnmarshalJSON(data []byte) error {
	tmp := &internalMessage{}
	err := json.Unmarshal(data, tmp)
	if err != nil {
		return xerrors.Errorf("error parsing message: %v", err)
	}

	m.MessageID = tmp.MessageID
	m.Sender = tmp.Sender
	m.Signature = tmp.Signature
	m.WitnessSignatures = tmp.WitnessSignatures
	m.RawData = tmp.Data

	return nil
}

// MarshalJSON implements custom marshaling logic for Base64URLBytes.
func (b Base64URLBytes) MarshalJSON() ([]byte, error) {
	dst := make([]byte, base64.URLEncoding.EncodedLen(len(b)))
	base64.URLEncoding.Encode(dst, []byte(b))

	// we call the marshaller on a string type because we want
	// the output to be enclosed between quotes
	return json.Marshal(string(dst))
}

// UnmarshalJSON implements custom unmarshaling logic for Base64URLBytes.
func (b *Base64URLBytes) UnmarshalJSON(data []byte) error {
	// We first unmarshal the string type. Refer to MarshalJSON for
	// more information
	var tmp string

	err := json.Unmarshal(data, &tmp)
	if err != nil {
		return xerrors.Errorf("failed to unmarshal base64Url bytes: %v", err)
	}

	// We now base64 URL Decode the value enclosed between the quotes
	dst := make([]byte, base64.URLEncoding.DecodedLen(len(tmp)))

	n, err := base64.URLEncoding.Decode(dst, []byte(tmp))
	if err != nil {
		return xerrors.Errorf("failed to unmarshal base64Url bytes: %v", err)
	}

	// Since base64 URL encoding pads the output we must slice the output
	// buffer to the actual amount of data encoded
	*b = Base64URLBytes(dst[:n])

	return nil
}

// MarshalJSON implements custom marshaling logic for a Signature.
func (s Signature) MarshalJSON() ([]byte, error) {
	b := Base64URLBytes(s)
	result, err := b.MarshalJSON()
	if err != nil {
		return nil, xerrors.Errorf("failed to marshal signature: %v", err)
	}
	return result, nil
}

// MarshalJSON implements custom unmarshaling logic for a Signature.
func (s *Signature) UnmarshalJSON(data []byte) error {
	err := json.Unmarshal(data, (*Base64URLBytes)(s))
	if err != nil {
		return xerrors.Errorf("failed to unmarshal signature: %v", err)
	}

	return nil
}

// MarshalJSON implements custom marshaling logic for a PublicKey.
func (p PublicKey) MarshalJSON() ([]byte, error) {
	b := Base64URLBytes(p)
	result, err := b.MarshalJSON()
	if err != nil {
		return nil, xerrors.Errorf("failed to marshal public key: %v", err)
	}
	return result, nil
}

// MarshalJSON implements custom unmarshaling logic for a PublicKey.
func (p *PublicKey) UnmarshalJSON(data []byte) error {
	err := json.Unmarshal(data, (*Base64URLBytes)(p))
	if err != nil {
		return xerrors.Errorf("failed to unmarshal public key: %v", err)
	}
	return nil
}

// VerifyAndUnmarshalData verifies the signature in the Message and returns an
// error in case of failure. If the verification suceeds it tries to unmarshal
// the RawData field into one of the implementations of `Data`.
func (m *Message) VerifyAndUnmarshalData() error {
	err := schnorr.VerifyWithChecks(student20_pop.Suite, m.Sender, m.RawData, m.Signature)
	if err != nil {
		return xerrors.Errorf("error verifying signature: %v", err)
	}

	gd := &GenericData{}
	err = json.Unmarshal(m.RawData, gd)
	if err != nil {
		return xerrors.Errorf("error parsing data wrapper: %v", err)
	}

	action := gd.GetAction()

	switch gd.GetObject() {
	case DataObject(LaoObject):
		err := m.parseLAOData(LaoDataAction(action), m.RawData)
		if err != nil {
			return xerrors.Errorf("error parsing lao data: %v", err)
		}
	case DataObject(MeetingObject):
		err := m.parseMeetingData(MeetingDataAction(action), m.RawData)
		if err != nil {
			return xerrors.Errorf("error parsing meeting data: %v", err)
		}
	case DataObject(MessageObject):
		err := m.parseMessageData(MessageDataAction(action), m.RawData)
		if err != nil {
			return xerrors.Errorf("error parsing message data: %v", err)
		}
	case DataObject(RollCallObject):
		err := m.parseRollCallData(RollCallAction(action), m.RawData)
		if err != nil {
			return xerrors.Errorf("error parsing roll call data: %v", err)
		}
	case DataObject(ElectionObject):
		err := m.parseElectionData(ElectionAction(action), m.RawData)
		if err != nil {
			return xerrors.Errorf("error parsing election data %v", err)
		}
	default:
		return xerrors.Errorf("failed to parse data object of type: %s", gd.GetObject())
	}

	return nil
}

func (m *Message) parseRollCallData(action RollCallAction, data Base64URLBytes) error {
	switch action {
	case CreateRollCallAction:
		create := &CreateRollCallData{}

		err := json.Unmarshal(data, create)
		if err != nil {
			return xerrors.Errorf("failed to parse create lao data: %v", err)
		}

		m.Data = create
		return nil
	case RollCallAction(OpenRollCallAction), RollCallAction(ReopenRollCallAction):
		open := &OpenRollCallData{}

		err := json.Unmarshal(data, open)
		if err != nil {
			return xerrors.Errorf("failed to parse create lao data: %v", err)
		}

		m.Data = open
		return nil
	case CloseRollCallAction:
		closeInst := &CloseRollCallData{}

		err := json.Unmarshal(data, closeInst)
		if err != nil {
			return xerrors.Errorf("failed to parse create lao data: %v", err)
		}

		m.Data = closeInst
		return nil
	default:
		return xerrors.Errorf("invalid action: %s", action)
	}
}

func (m *Message) parseMessageData(action MessageDataAction, data Base64URLBytes) error {
	if action != WitnessAction {
		return xerrors.Errorf("invalid action type: %s", action)
	}

	witness := &WitnessMessageData{}

	err := json.Unmarshal(data, witness)
	if err != nil {
		return xerrors.Errorf("failed to parse witness action: %v", err)
	}

	m.Data = witness
	return nil
}

func (m *Message) parseMeetingData(action MeetingDataAction, data Base64URLBytes) error {
	switch action {
	case CreateMeetingAction:
		create := &CreateMeetingData{}

		err := json.Unmarshal(data, create)
		if err != nil {
			return xerrors.Errorf("failed to parse create lao data: %v", err)
		}

		m.Data = create
		return nil
	case StateMeetingAction:
		state := &StateMeetingData{}

		err := json.Unmarshal(data, state)
		if err != nil {
			return xerrors.Errorf("failed to parse state lao data: %v", err)
		}

		m.Data = state
		return nil
	default:
		return xerrors.Errorf("invalid action: %s", action)

	}
}

func (m *Message) parseLAOData(action LaoDataAction, data Base64URLBytes) error {
	switch action {
	case CreateLaoAction:
		create := &CreateLAOData{}

		err := json.Unmarshal(data, create)
		if err != nil {
			return xerrors.Errorf("failed to parse create lao data: %v", err)
		}

		m.Data = create
		return nil
	case UpdateLaoAction:
		update := &UpdateLAOData{}

		err := json.Unmarshal(data, update)
		if err != nil {
			return xerrors.Errorf("failed to parse update lao data: %v", err)
		}

		m.Data = update
		return nil
	case StateLaoAction:
		state := &StateLAOData{}

		err := json.Unmarshal(data, state)
		if err != nil {
			return xerrors.Errorf("failed to parse state lao data: %v", err)
		}
		m.Data = state
		return nil
	default:
		return xerrors.Errorf("invalid action: %s", action)
	}

}
func (m *Message) parseElectionData(action ElectionAction, data Base64URLBytes) error {
	switch action {
	case ElectionSetupAction:
		setup := &ElectionSetupData{}
		err := json.Unmarshal(data, setup)
		if err != nil {
			return xerrors.Errorf("failed to parse election setup data: %v", err)
		}

		m.Data = setup
		return nil
	case CastVoteAction:
		cast := &CastVoteData{}
		err := json.Unmarshal(data, cast)
		if err != nil {
			return xerrors.Errorf("failed to parse cast vote data : %v", err)
		}
		m.Data = cast
		return nil
	case ElectionEndAction:
		end := &ElectionEndData{}
		err := json.Unmarshal(data, end)
		if err != nil {
			return xerrors.Errorf("failed to parse end of election data : %v", err)
		}

		m.Data = end
		return nil
	case ElectionResultAction:
		result := &ElectionResultData{}
		err := json.Unmarshal(data, result)
		if err != nil {
			return xerrors.Errorf("failed to parse result of election data: %v", err)
		}

		m.Data = result
		return nil
	default:
		return xerrors.Errorf("invalid election action : %s", action)
	}
}
