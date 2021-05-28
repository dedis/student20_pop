package hub

import (
	"fmt"
	"github.com/gorilla/websocket"
	"golang.org/x/xerrors"
	"log"
	"net/http"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin:     func(r *http.Request) bool { return true },
}

func CreateAndServeWs(hubType HubType, socketType SocketType, h Hub, port int) error {
	http.HandleFunc("/"+string(hubType)+"/"+string(socketType)+"/", func(w http.ResponseWriter, r *http.Request) {
		serveWs(socketType, h, w, r)
	})

	log.Printf("Starting the %s WS server (for %s) at %d", hubType, socketType, port)
	var err = http.ListenAndServe(fmt.Sprintf(":%d", port), nil)
	if err != nil {
		log.Printf("Error while starting the server")
		return xerrors.Errorf("failed to start the %s server: %v", hubType, err)
	}

	return nil
}

func serveWs(socketType SocketType, h Hub, w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Printf("failed to upgrade connection: %v", err)
		return
	}

	switch socketType {
	case ClientSocketType:
		client := NewClientSocket(h, conn)

		go client.ReadPump()
		go client.WritePump()

		// cleanup go routine that removes clients that forgot to unsubscribe
		go func(c *ClientSocket, h Hub) {
			c.Wait.Wait()
			h.RemoveClientSocket(c)
		}(client, h)
	case WitnessSocketType:
		witness := NewWitnessSocket(h, conn)

		go witness.ReadPump()
		go witness.WritePump()
	}
}
