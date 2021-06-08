package witness

import (
	"encoding/base64"
	"fmt"
	"log"
	"net/http"
	"net/url"
	"student20_pop"
	"student20_pop/hub"
	"student20_pop/validation"

	"github.com/gorilla/websocket"
	"github.com/urfave/cli/v2"
	"golang.org/x/xerrors"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	CheckOrigin:     func(r *http.Request) bool { return true },
}

func Serve(context *cli.Context) error {
	organizerAddr := context.String("organizer-address")
	organizerPort := context.Int("organizer-port")
	clientPort := context.Int("client-port")
	pk := context.String("public-key")

	if pk == "" {
		return xerrors.Errorf("witness' public key is required")
	}

	pkBuf, err := base64.URLEncoding.DecodeString(pk)
	if err != nil {
		return xerrors.Errorf("failed to base64url decode public key: %v", err)
	}

	point := student20_pop.Suite.Point()
	err = point.UnmarshalBinary(pkBuf)
	if err != nil {
		return xerrors.Errorf("failed to unmarshal public key: %v", err)
	}

	protocolLoader := validation.GetProtocolLoader(context)

	h, err := hub.NewWitnessHub(point, protocolLoader)
	if err != nil {
		return xerrors.Errorf("failed create the witness hub: %v", err)
	}

	ws, err := connectToOrganizer(organizerAddr, organizerPort)
	if err != nil {
		return xerrors.Errorf("failed to connect to organizer: %v", err)
	}

	organizerSocket := hub.NewOrganizerSocket(h, ws)

	go organizerSocket.WritePump()
	go organizerSocket.ReadPump()

	done := make(chan struct{})
	go h.Start(done)

	hub.CreateAndServeWs(hub.WitnessHubType, hub.ClientSocketType, h, clientPort)

	done <- struct{}{}

	return nil
}

func connectToOrganizer(organizerAddr string, port int) (*websocket.Conn, error) {
	address := fmt.Sprintf("ws://%s:%d/organizer/witness/", organizerAddr, port)
	u, err := url.Parse(address)
	if err != nil {
		return nil, xerrors.Errorf("failure to connect to organizer: %v", err)
	}
	ws, _, err := websocket.DefaultDialer.Dial(u.String(), nil)
	if err != nil {
		return ws, xerrors.Errorf("failure to connect to organizer: %v", err)
	}
	log.Printf("connected to organizer at %s", address)
	return ws, nil
}
