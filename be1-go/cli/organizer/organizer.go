package organizer

import (
	"encoding/base64"
	"fmt"
	"log"
	"net/http"
	"student20_pop"
	"student20_pop/hub"

	"github.com/gorilla/websocket"
	"github.com/urfave/cli/v2"
	"golang.org/x/xerrors"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin:     func(r *http.Request) bool { return true },
}

// Serve parses the CLI arguments and spawns a hub and a websocket server.
func Serve(context *cli.Context) error {
	clientPort := context.Int("client-port")
	witnessPort := context.Int("witness-port")
	if clientPort == witnessPort {
		return xerrors.Errorf("client and witness ports must be different")
	}

	pk := context.String("public-key")

	if pk == "" {
		return xerrors.Errorf("organizer's public key is required")
	}

	pkBuf, err := base64.StdEncoding.DecodeString(pk)
	if err != nil {
		return xerrors.Errorf("failed to base64 decode public key: %v", err)
	}

	point := student20_pop.Suite.Point()
	err = point.UnmarshalBinary(pkBuf)
	if err != nil {
		return xerrors.Errorf("failed to unmarshal public key: %v", err)
	}

	h := hub.NewOrganizerHub(point)

	done := make(chan struct{})
	go h.Start(done)

	go createAndServeWs(hub.WitnessSocketType, h, witnessPort)
	createAndServeWs(hub.ClientSocketType, h, clientPort)

	done <- struct{}{}

	return nil
}

func createAndServeWs(socketType hub.SocketType, h hub.Hub, port int) error {
	http.HandleFunc(string("/organizer/"+socketType+"/"), func(w http.ResponseWriter, r *http.Request) {
		serveWs(socketType, h, w, r)
	})

	log.Printf("Starting the organizer WS server (for %s) at %d", socketType, port)
	var err = http.ListenAndServe(fmt.Sprintf(":%d", port), nil)
	if err != nil {
		return xerrors.Errorf("failed to start the server: %v", err)
	}

	return nil
}

func serveWs(socketType hub.SocketType, h hub.Hub, w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Printf("failed to upgrade connection: %v", err)
		return
	}

	switch socketType {
	case hub.ClientSocketType:
		client := hub.NewClientSocket(h, conn)

		go client.ReadPump()
		go client.WritePump()

		// cleanup go routine that removes clients that forgot to unsubscribe
		go func(c *hub.ClientSocket, h hub.Hub) {
			c.Wait.Wait()
			h.RemoveClientSocket(c)
		}(client, h)
	case hub.WitnessSocketType:
		witness := hub.NewWitnessSocket(h, conn)

		go witness.ReadPump()
		go witness.WritePump()
	}
}
