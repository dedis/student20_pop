package hub

import (
	"student20_pop/message"
)

type HubType string

const (
	OrganizerHubType HubType = "organizer"
	WitnessHubType   HubType = "witness"
)

// Hub defines the methods a PoP server must implement to receive messages
// and handle clients.
type Hub interface {
	Start(done chan struct{})

	Recv(msg IncomingMessage)

	RemoveClientSocket(client *ClientSocket)
}

// IncomingMessage wraps the raw message from the websocket connection and pairs
// it with a `Client` instance.
type IncomingMessage struct {
	Socket  *baseSocket
	Message []byte
}

// Channel represents a PoP channel - like a LAO.
type Channel interface {
	Subscribe(client *ClientSocket, msg message.Subscribe) error

	Unsubscribe(client *ClientSocket, msg message.Unsubscribe) error

	Publish(msg message.Publish) error

	Catchup(msg message.Catchup) []message.Message
}
