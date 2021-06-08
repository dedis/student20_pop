package hub

import (
	"log"
	"student20_pop/validation"

	"go.dedis.ch/kyber/v3"
)

type witnessHub struct {
	*baseHub
}

// NewWitnessHub returns a Witness Hub.
func NewWitnessHub(public kyber.Point, protocolLoader validation.ProtocolLoader) (Hub, error) {
	baseHub, err := NewBaseHub(public, protocolLoader)
	return &witnessHub{
		baseHub,
	}, err
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
	for {
		select {
		case incomingMessage := <-w.messageChan:
			w.handleIncomingMessage(&incomingMessage)
		case <-done:
			return
		}
	}
}
