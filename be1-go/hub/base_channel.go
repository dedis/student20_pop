package hub

import (
	"encoding/base64"
	"log"
	"student20_pop/message"
	"sync"

	"golang.org/x/xerrors"
)

// baseChannel represent a generic channel and contains all the fields that are
// used in all channels
type baseChannel struct {
	hub *organizerHub

	clientsMu sync.RWMutex
	clients   map[*Client]struct{}

	inboxMu sync.RWMutex
	inbox   map[string]message.Message

	// /root/<ID>
	channelID string

	witnessMu sync.Mutex
	witnesses []message.PublicKey
}

// CreateBaseChannel return an instance of a `baseChannel`
func createBaseChannel(h *organizerHub, channelID string) *baseChannel {
	return &baseChannel{
		hub:       h,
		channelID: channelID,
		clients:   make(map[*Client]struct{}),
		inbox:     make(map[string]message.Message),
	}
}

func (c *baseChannel) Subscribe(client *Client, msg message.Subscribe) error {
	log.Printf("received a subscribe with id: %d", msg.ID)
	c.clientsMu.Lock()
	defer c.clientsMu.Unlock()

	c.clients[client] = struct{}{}

	return nil
}

func (c *baseChannel) Unsubscribe(client *Client, msg message.Unsubscribe) error {
	log.Printf("received an unsubscribe with id: %d", msg.ID)

	c.clientsMu.Lock()
	defer c.clientsMu.Unlock()

	if _, ok := c.clients[client]; !ok {
		return &message.Error{
			Code:        -2,
			Description: "client is not subscribed to this channel",
		}
	}

	delete(c.clients, client)
	return nil
}

func (c *baseChannel) Catchup(catchup message.Catchup) []message.Message {
	log.Printf("received a catchup with id: %d", catchup.ID)

	c.inboxMu.RLock()
	defer c.inboxMu.RUnlock()

	result := make([]message.Message, 0, len(c.inbox))
	for _, msg := range c.inbox {
		result = append(result, msg)
	}

	return result
}

// Verify the if a Publish message is valid
func (c *baseChannel) VerifyPublishMessage(publish message.Publish) error {
	log.Printf("received a publish with id: %d", publish.ID)

	// Check if the structure of the message is correct
	msg := publish.Params.Message
	err := msg.VerifyAndUnmarshalData()
	if err != nil {
		return xerrors.Errorf("failed to verify and unmarshal data: %v", err)
	}

	msgIDEncoded := base64.StdEncoding.EncodeToString(msg.MessageID)
	
	// Check if the message already exists
	c.inboxMu.RLock()
	if _, ok := c.inbox[msgIDEncoded]; ok {
		c.inboxMu.RUnlock()
		return &message.Error{
			Code:        -3,
			Description: "message already exists",
		}
	}
	c.inboxMu.RUnlock()

	return nil
}

