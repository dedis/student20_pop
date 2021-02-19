package message

import (
	"bytes"
	"encoding/json"
	"fmt"

	"student20_pop"

	"go.dedis.ch/kyber/v3"
	"go.dedis.ch/kyber/v3/sign/schnorr"
	"golang.org/x/xerrors"
)

type Query struct {
	Subscribe   *Subscribe
	Unsubscribe *Unsubscribe
	Publish     *Publish
	Catchup     *Catchup
	Broadcast   *Broadcast
}

type Subscribe struct {
	ID     int    `json:"id"`
	Method string `json:"method"`
	Params Params `json:"params"`
}

type Params struct {
	Channel string `json:"channel"`

	Message *Message `json:"message"`
}

type Unsubscribe struct {
	ID     int    `json:"id"`
	Method string `json:"method"`
	Params Params `json:"params"`
}

type Publish struct {
	ID     int    `json:"id"`
	Method string `json:"method"`
	Params Params `json:"params"`
}

type Catchup struct {
	ID     int    `json:"id"`
	Method string `json:"method"`
	Params Params `json:"params"`
}

type Broadcast struct {
	Method string `json:"method"`
	Params Params `json:"params"`
}

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
	case "message":
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
		return q.Publish.Params.Channel
	}

	return ""
}

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

func (q *Query) Verify(organizerPublic kyber.Point) error {
	if q.Subscribe != nil || q.Unsubscribe != nil || q.Catchup != nil {
		return nil
	}

	organizerPublicBuf, err := organizerPublic.MarshalBinary()
	if err != nil {
		return &Error{
			Code:        -6,
			Description: fmt.Sprintf("failed to marshal organizerPublic key: %v", err),
		}
	}

	data := []byte{}
	signature := Signature{}
	public := PublicKey{}

	object := DataObject("")

	if q.Broadcast != nil {
		data = q.Broadcast.Params.Message.Data.GetRaw()
		signature = q.Broadcast.Params.Message.Signature
		public = q.Broadcast.Params.Message.Sender
		object = q.Broadcast.Params.Message.Data.GetObject()
	} else if q.Publish != nil {
		data = q.Publish.Params.Message.Data.GetRaw()
		signature = q.Publish.Params.Message.Signature
		public = q.Publish.Params.Message.Sender
		object = q.Publish.Params.Message.Data.GetObject()
	} else {
		return &Error{
			Code:        -4,
			Description: "invalid method",
		}
	}

	if q.Broadcast != nil || object == DataObject(MessageObject) {
		return schnorr.VerifyWithChecks(student20_pop.Suite, public, data, signature)
	}

	if !bytes.Equal(organizerPublicBuf, public) {
		return &Error{
			Code:        -5,
			Description: "you do not have permissions to sign this object",
		}
	}

	err = schnorr.Verify(student20_pop.Suite, organizerPublic, data, signature)
	if err != nil {
		return &Error{
			Code:        -5,
			Description: "failed to verify signature using organizer's public key",
		}
	}

	return nil
}

func (q Query) MarshalJSON() ([]byte, error) {
	type internal struct {
		JSONRpc     string `json:"jsonrpc"`
		Subscribe   *Subscribe
		Unsubscribe *Unsubscribe
		Publish     *Publish
		Catchup     *Catchup
		Broadcast   *Broadcast
	}

	tmp := internal{
		JSONRpc:     "2.0",
		Subscribe:   q.Subscribe,
		Unsubscribe: q.Unsubscribe,
		Publish:     q.Publish,
		Catchup:     q.Catchup,
		Broadcast:   q.Broadcast,
	}

	return json.Marshal(tmp)
}

func NewBroadcast(channel string, msg *Message) *Broadcast {
	return &Broadcast{
		Method: "message",
		Params: Params{
			Channel: channel,
			Message: msg,
		},
	}
}