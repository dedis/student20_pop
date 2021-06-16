package hub

import (
	"sync"

	"github.com/gorilla/websocket"
)

// socket types
type SocketType string

const (
	ClientSocketType    SocketType = "client"
	OrganizerSocketType SocketType = "organizer"
	WitnessSocketType   SocketType = "witness"
)

func newSocket(socketType SocketType, h Hub, conn *websocket.Conn, wg *sync.WaitGroup) *baseSocket {
	return &baseSocket{
		socketType: socketType,
		hub:        h,
		conn:       conn,
		send:       make(chan []byte, 256),
		Wait:       wg,
	}
}

type ClientSocket struct {
	*baseSocket
}

// NewClient returns an instance of a baseSocket.
func NewClientSocket(h Hub, conn *websocket.Conn, wg *sync.WaitGroup) *ClientSocket {
	return &ClientSocket{
		newSocket(ClientSocketType, h, conn, wg),
	}
}

type OrganizerSocket struct {
	*baseSocket
}

func NewOrganizerSocket(h Hub, conn *websocket.Conn, wg *sync.WaitGroup) *OrganizerSocket {
	return &OrganizerSocket{
		newSocket(OrganizerSocketType, h, conn, wg),
	}
}

type WitnessSocket struct {
	*baseSocket
}

func NewWitnessSocket(h Hub, conn *websocket.Conn, wg *sync.WaitGroup) *WitnessSocket {
	return &WitnessSocket{
		newSocket(WitnessSocketType, h, conn, wg),
	}
}
