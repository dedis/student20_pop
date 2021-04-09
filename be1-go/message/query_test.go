package message

import (
	"bytes"
	"encoding/json"
	"testing"
	"time"

	"github.com/stretchr/testify/require"
	"golang.org/x/xerrors"
)

var channel = "/root/channel_id"

func createMessage(i int, timestamp Timestamp) (Message, error) {
	data, err := NewCreateLAOData("test", timestamp, []byte{byte(i)}, []PublicKey{})
	if err != nil {
		return Message{}, err
	}

	msg := Message{
		MessageID:         []byte{byte(i)},
		Data:              data,
		Sender:            []byte{byte(i)},
		Signature:         []byte{byte(i)},
		WitnessSignatures: []PublicKeySignaturePair{},
	}

	return msg, nil
}

func compareQueries(q1, q2 Query) (bool, error) {
	m1, m2 := q1.GetMethod(), q2.GetMethod()
	if m1 != m2 {
		return false, nil
	}
	if q1.GetChannel() != q2.GetChannel() {
		return false, nil
	}

	switch m1 {
	case "broadcast":
		if q1.GetID() != q2.GetID() {
			return false, nil
		}
		return compareParams(q1, q2)
	case "subscribe":
		return true, nil
	case "unsubscribe":
		return true, nil
	case "catchup":
		return true, nil
	default:
		return compareParams(q1, q2)
	}
}

func compareParams(q1, q2 Query) (bool, error) {
	p1, ok := q1.GetParams()
	if !ok {
		return false, xerrors.Errorf("failed to get the params of the first query")
	}
	p2, ok := q2.GetParams()
	if !ok {
		return false, xerrors.Errorf("failed to get the params of the second query")
	}

	return compareMessages(*p1.Message, *p2.Message), nil
}

func compareMessages(m1, m2 Message) bool {
	return bytes.Equal(m1.Sender, m2.Sender) && bytes.Equal(m1.Signature, m2.Signature)
}

func testQuery(t *testing.T, q1 Query) {
	buf, err := json.Marshal(q1)
	require.NoError(t, err)

	genericMsg := &GenericMessage{}
	err = json.Unmarshal(buf, genericMsg)
	require.NoError(t, err)

	q2 := *genericMsg.Query
	res, err := compareQueries(q1, q2)
	require.NoError(t, err)
	require.True(t, res)
}

func Test_MarshalBroadcast(t *testing.T) {
	timestamp := Timestamp(time.Now().UnixNano())
	msg, err := createMessage(1, timestamp)
	require.NoError(t, err)

	q1 := Query{
		Broadcast: NewBroadcast(channel, &msg),
	}
	testQuery(t, q1)
}

func Test_MarshalPublish(t *testing.T) {
	timestamp := Timestamp(time.Now().UnixNano())
	msg, err := createMessage(1, timestamp)
	require.NoError(t, err)

	q1 := Query{
		Publish: &Publish{
			ID:     10,
			Method: "publish",
			Params: Params{
				Channel: channel,
				Message: &msg,
			},
		},
	}
	testQuery(t, q1)
}

func Test_MarshalSubscribe(t *testing.T) {
	q1 := Query{
		Subscribe: &Subscribe{
			ID:     10,
			Method: "subscribe",
			Params: Params{
				Channel: channel,
			},
		},
	}
	testQuery(t, q1)
}

func Test_MarshalUnsubscribe(t *testing.T) {
	q1 := Query{
		Subscribe: &Subscribe{
			ID:     10,
			Method: "unsubscribe",
			Params: Params{
				Channel: channel,
			},
		},
	}
	testQuery(t, q1)
}

func Test_MarshalCatchup(t *testing.T) {
	q1 := Query{
		Subscribe: &Subscribe{
			ID:     10,
			Method: "catchup",
			Params: Params{
				Channel: channel,
			},
		},
	}
	testQuery(t, q1)
}
