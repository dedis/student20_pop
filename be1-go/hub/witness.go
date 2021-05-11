package hub

import (
	"log"
	"sync"

	"go.dedis.ch/kyber/v3"
)

type witnessHub struct {
	messageChan chan IncomingMessage

	sync.RWMutex
	channelByID map[string]Channel

	public kyber.Point
}

// NewWitnessHub returns a Witness Hub.
func NewWitnessHub(public kyber.Point) Hub {
	return &witnessHub{
		messageChan: make(chan IncomingMessage),
		channelByID: make(map[string]Channel),
		public:      public,
	}
}

func (w *witnessHub) RemoveClientSocket(client *ClientSocket) {
	//TODO
}

func (w *witnessHub) Recv(msg IncomingMessage) {
	log.Printf("witnessHub::Recv")
	w.messageChan <- msg
}

func (w *witnessHub) handleMessageFromOrganizer(incomingMessage *IncomingMessage) {
	//TODO
}

func (w *witnessHub) handleMessageFromClient(incomingMessage *IncomingMessage) {
	//TODO
}

func (w *witnessHub) handleMessageFromWitness(incomingMessage *IncomingMessage) {
	//TODO
}

func (w *witnessHub) handleIncomingMessage(incomingMessage *IncomingMessage) {
	log.Printf("organizerHub::handleIncomingMessage: %s", incomingMessage.Message)

	switch incomingMessage.Socket.socketType {
	case OrganizerSocketType:
		w.handleMessageFromOrganizer(incomingMessage)
		return
	case ClientSocketType:
		w.handleMessageFromClient(incomingMessage)
		return
	case WitnessSocketType:
		w.handleMessageFromWitness(incomingMessage)
		return
	}

}

func (w *witnessHub) Start(done chan struct{}) {
	log.Printf("started witness ..")

	for {
		select {
		case incomingMessage := <-w.messageChan:
			w.handleIncomingMessage(&incomingMessage)
		case <-done:
			return
		}
	}
}
