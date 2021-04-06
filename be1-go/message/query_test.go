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

func compareQuery(q1, q2 Query) (bool, error) {
	m1, m2 := q1.GetMethod(), q2.GetMethod()
	if m1 != m2 {
		return false, nil
	}
	if q1.GetChannel() != q2.GetChannel() {
		return false, nil
	}
	if m1 != "broadcast" {
		if q1.GetID() != q2.GetID() {
			return false, nil
		}
	}
	if m1 == "subscribe" || m1 == "unsubscribe" {
		return true, nil
	}
	p1, ok := q1.GetParams()
	if !ok {
		return false, xerrors.Errorf("failed to get the params of the first query")
	}
	p2, ok := q2.GetParams()
	if !ok {
		return false, xerrors.Errorf("failed to get the params of the second query")
	}

	return compareMessage(*p1.Message, *p2.Message), nil

}

func compareMessage(m1, m2 Message) bool {
	return bytes.Equal(m1.MessageID, m2.MessageID) && bytes.Equal(m1.Sender, m2.Sender) && bytes.Equal(m1.Signature, m2.Signature)
}

func Test_MarshalBroadcast(t *testing.T) {
	timestamp := Timestamp(time.Now().UnixNano())
	msg, err := createMessage(1, timestamp)
	require.NoError(t, err)

	q1 := Query{
		Broadcast: NewBroadcast(channel, &msg),
	}
	buf, err := json.Marshal(q1)
	require.NoError(t, err)

	genericMsg := &GenericMessage{}
	err = json.Unmarshal(buf, genericMsg)
	require.NoError(t, err)

	q2 := *genericMsg.Query
	res, err := compareQuery(q1, q2)
	require.NoError(t, err)
	require.True(t, res)
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
	buf, err := json.Marshal(q1)
	require.NoError(t, err)

	genericMsg := &GenericMessage{}
	err = json.Unmarshal(buf, genericMsg)
	require.NoError(t, err)

	q2 := *genericMsg.Query
	res, err := compareQuery(q1, q2)
	require.NoError(t, err)
	require.True(t, res)
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
	buf, err := json.Marshal(q1)
	require.NoError(t, err)

	genericMsg := &GenericMessage{}
	err = json.Unmarshal(buf, genericMsg)
	require.NoError(t, err)

	q2 := *genericMsg.Query
	res, err := compareQuery(q1, q2)
	require.NoError(t, err)
	require.True(t, res)
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
	buf, err := json.Marshal(q1)
	require.NoError(t, err)

	genericMsg := &GenericMessage{}
	err = json.Unmarshal(buf, genericMsg)
	require.NoError(t, err)

	q2 := *genericMsg.Query
	res, err := compareQuery(q1, q2)
	require.NoError(t, err)
	require.True(t, res)
}
