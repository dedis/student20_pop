package hub

import (
	"github.com/gorilla/websocket"
	"sync"
)

// socket types
type SocketType string

const (
	clientSocket SocketType = "client"
	organizerSocket SocketType= "organizer"
	witnessSocket SocketType= "witness"
)

func newSocket(socketType SocketType, h Hub, conn *websocket.Conn) *baseSocket {
	return &baseSocket {
		socketType: socketType,
		hub:  h,
		conn: conn,
		send: make(chan []byte, 256),
		Wait: sync.WaitGroup{},
	}
}

type ClientSocket struct {
	*baseSocket
}

// NewClient returns an instance of a baseSocket.
func NewClientSocket (h Hub, conn *websocket.Conn) *ClientSocket {
	return &ClientSocket{
		newSocket(clientSocket, h, conn),
	}
}

type OrganizerSocket struct {
	*baseSocket
}

func NewOrganizerSocket(h Hub, conn *websocket.Conn) *OrganizerSocket {
	return &OrganizerSocket {
		newSocket(organizerSocket, h, conn),
	}
}

type WitnessSocket struct {
	*baseSocket
}

func NewWitnessSocket(h Hub, conn *websocket.Conn) *WitnessSocket {
	return &WitnessSocket {
		newSocket(witnessSocket, h, conn),
	}
}