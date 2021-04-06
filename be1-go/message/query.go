package message

import (
	"encoding/json"

	"golang.org/x/xerrors"
)

// Query represents a Query PoP message which may be one of the given types.
type Query struct {
	Subscribe   *Subscribe
	Unsubscribe *Unsubscribe
	Publish     *Publish
	Catchup     *Catchup
	Broadcast   *Broadcast
}

// Subscribe represents a Subscribe message.
type Subscribe struct {
	ID     int    `json:"id"`
	Method string `json:"method"`
	Params Params `json:"params"`
}

// Params represents the Params field in a Query.
type Params struct {
	Channel string `json:"channel"`

	Message *Message `json:"message,omitempty"`
}

// Unsubscribe represents the Unsubscribe message.
type Unsubscribe struct {
	ID     int    `json:"id"`
	Method string `json:"method"`
	Params Params `json:"params"`
}

// Publish represents a Publish message.
type Publish struct {
	ID     int    `json:"id"`
	Method string `json:"method"`
	Params Params `json:"params"`
}

// Catchup represents a catchup message.
type Catchup struct {
	ID     int    `json:"id"`
	Method string `json:"method"`
	Params Params `json:"params"`
}

// Broadcast represents a broadcast message.
type Broadcast struct {
	Method string `json:"method"`
	Params Params `json:"params"`
}

// UnmarshalJSON impelments custom unmarshaling logic for a query.
func (q *Query) UnmarshalJSON(data []byte) error {
	type internal struct {
		Method string `json:"method"`
	}

	tmp := &internal{}

	err := json.Unmarshal(data, tmp)
	if err != nil {
		return xerrors.Errorf("failed to parse query method: %v", err)
	}

	switch tmp.Method {
	case "subscribe":
		subscribe := &Subscribe{}

		err := json.Unmarshal(data, subscribe)
		if err != nil {
			return xerrors.Errorf("failed to parse subscribe message: %v", err)
		}

		q.Subscribe = subscribe
		return nil
	case "unsubscribe":
		unsubscribe := &Unsubscribe{}

		err := json.Unmarshal(data, unsubscribe)
		if err != nil {
			return xerrors.Errorf("failed to parse subscribe message: %v", err)
		}

		q.Unsubscribe = unsubscribe
		return nil
	case "publish":
		publish := &Publish{}

		err := json.Unmarshal(data, publish)
		if err != nil {
			return xerrors.Errorf("failed to parse publish message: %v", err)
		}

		q.Publish = publish
		return nil
	case "broadcast":
		broadcast := &Broadcast{}

		err := json.Unmarshal(data, broadcast)
		if err != nil {
			return xerrors.Errorf("failed to parse broadcast message: %v", err)
		}

		q.Broadcast = broadcast
		return nil
	case "catchup":
		catchup := &Catchup{}

		err := json.Unmarshal(data, catchup)
		if err != nil {
			return xerrors.Errorf("failed to parse catchup message: %v", err)
		}

		q.Catchup = catchup
		return nil
	default:
		return xerrors.Errorf("failed to parse query: invalid method type: %s", tmp.Method)
	}
}

// GetChannel returns the channel associated with a query.
func (q *Query) GetChannel() string {
	if q.Subscribe != nil {
		return q.Subscribe.Params.Channel
	} else if q.Unsubscribe != nil {
		return q.Unsubscribe.Params.Channel
	} else if q.Broadcast != nil {
		return q.Broadcast.Params.Channel
	} else if q.Publish != nil {
		return q.Publish.Params.Channel
	} else if q.Catchup != nil {
		return q.Catchup.Params.Channel
	}

	return ""
}

// GetMethod returns the method associated with a query.
func (q *Query) GetMethod() string {
	if q.Subscribe != nil {
		return q.Subscribe.Method
	} else if q.Unsubscribe != nil {
		return q.Unsubscribe.Method
	} else if q.Broadcast != nil {
		return q.Broadcast.Method
	} else if q.Publish != nil {
		return q.Publish.Method
	} else if q.Catchup != nil {
		return q.Catchup.Method
	}

	return ""
}

// GetID returns the ID associated with a query.
func (q *Query) GetID() int {
	if q.Subscribe != nil {
		return q.Subscribe.ID
	} else if q.Unsubscribe != nil {
		return q.Unsubscribe.ID
	} else if q.Publish != nil {
		return q.Publish.ID
	} else if q.Catchup != nil {
		return q.Catchup.ID
	}
	return -1
}

// GetParams returns params associated with a query
func (q *Query) GetParams() (Params, bool) {
	switch {
	case q.Subscribe != nil:
		return q.Subscribe.Params, true
	case q.Unsubscribe != nil:
		return q.Unsubscribe.Params, true
	case q.Broadcast != nil:
		return q.Broadcast.Params, true
	case q.Publish != nil:
		return q.Publish.Params, true
	case q.Catchup != nil:
		return q.Catchup.Params, true
	}
	return Params{}, false
}

// MarshalJSON implements custom marshaling logic for a query.
func (q Query) MarshalJSON() ([]byte, error) {

	type internal struct {
		JSONRpc string  `json:"jsonrpc"`
		ID      int     `json:"id,omitempty"`
		Method  string  `json:"method"`
		Params  *Params `json:"params"`
	}

	method := q.GetMethod()
	params, ok := q.GetParams()
	if !ok {
		return nil, xerrors.Errorf("failed to get the params of the query of type %s", method)
	}

	tmp := internal{
		JSONRpc: "2.0",
		Method:  method,
		Params:  &params,
	}

	if method != "broadcast" {
		tmp.ID = q.GetID()
	}

	return json.Marshal(tmp)
}

// NewBroadcast creates a new instance of a broadcast message.
func NewBroadcast(channel string, msg *Message) *Broadcast {
	return &Broadcast{
		Method: "broadcast",
		Params: Params{
			Channel: channel,
			Message: msg,
		},
	}
}
