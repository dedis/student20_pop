package witness

import (
	"encoding/base64"
	"fmt"
	"github.com/gorilla/websocket"
	"github.com/urfave/cli/v2"
	"golang.org/x/xerrors"
	"log"
	"net/http"
	"net/url"
	"student20_pop"
	"student20_pop/hub"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin:     func(r *http.Request) bool { return true },
}

func Serve(context *cli.Context) error {
	organizerAddr := context.String("organizer-address")
	organizerPort := context.Int("organizer-port")
	clientPort := 9001
	pk := context.String("public-key")

	if pk == "" {
		return xerrors.Errorf("witness' public key is required")
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

	h := hub.NewWitnessHub(point)

	ws, err := connectToOrganizer(organizerAddr, organizerPort)
	if err != nil {
		return xerrors.Errorf("failed to connect to organizer: %v", err)
	}

	organizerSocket := hub.NewOrganizerSocket(h, ws)

	go organizerSocket.WritePump()
	go organizerSocket.ReadPump()

	done := make(chan struct{})
	h.Start(done)

	createAndServeWs(hub.ClientSocketType, h, clientPort)

	done <- struct{}{}

	return nil
}

func connectToOrganizer(organizerAddr string, port int) (*websocket.Conn, error) {
	u, err := url.Parse(fmt.Sprintf("ws://%s:%d/organizer/witness/", organizerAddr, port))
	if err != nil {
		return nil, xerrors.Errorf("failure to connect to organizer: %v", err)
	}
	ws, _, err := websocket.DefaultDialer.Dial(u.String(), nil)
	if err != nil {
		return ws, xerrors.Errorf("failure to connect to organizer: %v", err)
	}
	return ws, nil
}

func createAndServeWs(socketType hub.SocketType, h hub.Hub, port int) error {
	http.HandleFunc(string("/witness/"+socketType+"/"), func(w http.ResponseWriter, r *http.Request) {
		serveWs(socketType, h, w, r)
	})

	log.Printf("Starting the witness WS server (for %s) at %d", socketType, port)
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
