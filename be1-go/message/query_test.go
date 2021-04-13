package message

import (
	"encoding/json"
	"testing"
	"time"

	"github.com/stretchr/testify/require"
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

func assertEqualQueries(t *testing.T, q1, q2 Query) {
	m1, m2 := q1.GetMethod(), q2.GetMethod()

	require.Equal(t, m1, m2)

	require.Equal(t, q1.GetChannel(), q2.GetChannel())

	switch m1 {
	case "broadcast":
		assertEqualParams(t, q1, q2)
	case "publish":
		require.Equal(t, q1.GetID(), q2.GetID())
		assertEqualParams(t, q1, q2)
	default:
		require.Equal(t, q1.GetID(), q2.GetID())
	}
}

func assertEqualParams(t *testing.T, q1, q2 Query) {
	p1, ok := q1.GetParams()
	require.True(t, ok)

	p2, ok := q2.GetParams()
	require.True(t, ok)

	assertEqualMessage(t, *p1.Message, *p2.Message)
}

func assertEqualMessage(t *testing.T, m1, m2 Message) {
	require.Equal(t, m1.Sender, m2.Sender)
	require.Equal(t, m1.Signature, m2.Signature)
}

func testQuery(t *testing.T, q1 Query) {
	buf, err := json.Marshal(q1)
	require.NoError(t, err)

	genericMsg := &GenericMessage{}
	err = json.Unmarshal(buf, genericMsg)
	require.NoError(t, err)

	q2 := *genericMsg.Query
	assertEqualQueries(t, q1, q2)
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
