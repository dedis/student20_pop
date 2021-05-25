package db

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"student20_pop/message"
	"sync"
	"testing"
	"time"

	"github.com/stretchr/testify/require"
)

func TestSQLite_NewRepository(t *testing.T) {
	repo, dir, err := createRepo("test.db")
	require.NoError(t, err)

	err = repo.Close()
	require.NoError(t, err)

	dbPath := fmt.Sprintf("%s/test.db", dir)

	_, err = os.Stat(dbPath)
	require.NoError(t, err)
}

func TestSQLite_AddMessage(t *testing.T) {
	repo, _, err := createRepo("add_message.db")
	require.NoError(t, err)

	defer repo.Close()

	channelID := "12345"
	timestamp := message.Timestamp(time.Now().UnixNano())

	msg, err := createMessage(0, timestamp)
	require.NoError(t, err)

	err = repo.AddMessage(channelID, msg, timestamp)
	require.NoError(t, err)
}

func createMessage(i int, timestamp message.Timestamp) (message.Message, error) {
	data, err := message.NewCreateLAOData("test", timestamp, []byte{1, 2, 3, byte(i)}, []message.PublicKey{})
	if err != nil {
		return message.Message{}, err
	}

	msg := message.Message{
		MessageID:         []byte{6, 7, 8, 9, byte(i)},
		Data:              data,
		Sender:            []byte{1, 2, 3, byte(i)},
		Signature:         []byte{1, 2, 3, byte(i)},
		WitnessSignatures: []message.PublicKeySignaturePair{},
	}

	return msg, nil
}

func addMessages(repo Repository, channelID string, limit int) (int64, error) {
	now := time.Now().UnixNano()

	for i := limit - 1; i >= 0; i-- {
		timestamp := message.Timestamp(now + int64(i))

		msg, err := createMessage(i, timestamp)
		if err != nil {
			return 0, err
		}

		err = repo.AddMessage(channelID, msg, timestamp)
		if err != nil {
			return 0, err
		}
	}

	return now, nil
}

func TestSQLite_GetMessages(t *testing.T) {
	repo, _, err := createRepo("get_messages.db")
	require.NoError(t, err)

	defer repo.Close()

	channelID := "12345"

	now, err := addMessages(repo, channelID, 100)
	require.NoError(t, err)

	messages, err := repo.GetMessages(channelID)
	require.NoError(t, err)

	require.Equal(t, 100, len(messages))

	// Check if we get the messages in sorted order of time
	for i := 0; i < 100; i++ {
		type internal struct {
			Creation int64 `json:"creation"`
		}

		tmp := &internal{}
		err := json.Unmarshal(messages[i].RawData, tmp)
		require.NoError(t, err)

		require.Equal(t, now+int64(i), tmp.Creation)
	}
}

func TestSQLite_GetMessagesInRange(t *testing.T) {
	repo, _, err := createRepo("get_messages_range.db")
	require.NoError(t, err)

	defer repo.Close()

	channelID := "12345"
	now, err := addMessages(repo, channelID, 100)
	require.NoError(t, err)

	start := message.Timestamp(now + int64(20))
	end := message.Timestamp(now + int64(50))
	messages, err := repo.GetMessagesInRange(channelID, start, end)
	require.NoError(t, err)

	require.Equal(t, 31, len(messages))
}

func TestSQLite_AddWitnessToMessage(t *testing.T) {
	repo, _, err := createRepo("add_witness.db")
	require.NoError(t, err)

	defer repo.Close()

	channelID := "12345"
	timestamp := message.Timestamp(time.Now().UnixNano())

	msg, err := createMessage(1, timestamp)
	require.NoError(t, err)

	messageID := base64.URLEncoding.EncodeToString(msg.MessageID)

	err = repo.AddMessage(channelID, msg, timestamp)
	require.NoError(t, err)

	addWitness := func(i byte, wg *sync.WaitGroup) {
		pair := message.PublicKeySignaturePair{
			Signature: []byte{i},
			Witness:   []byte{i},
		}

		err := repo.AddWitnessToMessage(messageID, pair)
		require.NoError(t, err)
		wg.Done()
	}

	wg := sync.WaitGroup{}
	for i := 0; i < 100; i++ {
		wg.Add(1)
		go addWitness(byte(i), &wg)
	}

	wg.Wait()

	messages, err := repo.GetMessages(channelID)
	require.NoError(t, err)

	require.Equal(t, 1, len(messages))

	require.Equal(t, 100, len(messages[0].WitnessSignatures))

	counts := make(map[byte]int)

	for i := 0; i < 100; i++ {
		witness := messages[0].WitnessSignatures[i].Witness
		require.Equal(t, 1, len(witness))

		idx := witness[0]
		counts[idx]++
	}

	require.Equal(t, 100, len(counts))

	for _, v := range counts {
		require.Equal(t, 1, v)
	}
}

func createRepo(dbName string) (Repository, string, error) {
	dir, err := ioutil.TempDir("", "pop")
	if err != nil {
		return nil, "", err
	}

	repo, err := NewSQLiteRepository(fmt.Sprintf("%s/%s", dir, dbName))
	if err != nil {
		return nil, "", err
	}

	return repo, dir, nil
}
