package message

import (
	"encoding/json"

	"golang.org/x/xerrors"
)

// GenericMessage represents a message that may be sent as a payload on websocket.
// It may either be a `Query` or an `Answer`.
type GenericMessage struct {
	Query  *Query
	Answer *Answer
}

// UnmarshalJSON implements custom unmarshaling logic for a GenericMessage.
func (m *GenericMessage) UnmarshalJSON(data []byte) error {
	type internal struct {
		Method string `json:"method"`
	}

	tmp := &internal{}
	err := json.Unmarshal(data, tmp)
	if err != nil {
		return xerrors.Errorf("failed to find method in GenericMessage: %s", err)
	}

	if tmp.Method == "" {
		answer := &Answer{}

		err = json.Unmarshal(data, answer)
		if err != nil {
			return xerrors.Errorf("failed to unmarshal Answer in GenericMessage: %s", err)
		}

		m.Answer = answer
		return nil
	}

	query := &Query{}

	err = json.Unmarshal(data, query)
	if err != nil {
		return xerrors.Errorf("failed to unmarshal Query in GenericMessage: %s", err)
	}

	m.Query = query
	return nil
}

// Unmarshal the ID of a message
func (m *GenericMessage) UnmarshalID(data []byte) (int, bool) {
	type idStruct struct {
		ID *int `json:"id"`
	}

	tmp := &idStruct{}
	json.Unmarshal(data, tmp)
	if tmp.ID != nil {
		return *tmp.ID, true
	}

	return -1, false
}
