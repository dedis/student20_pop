package organizer

import (
	"encoding/base64"
	"student20_pop"
	"student20_pop/hub"
	"student20_pop/validation"

	"github.com/urfave/cli/v2"
	"golang.org/x/xerrors"
)

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

	h, err := hub.NewOrganizerHub(point, protocolLoader)
	if err != nil {
		return xerrors.Errorf("failed create the organizer hub: %v", err)
	}

	done := make(chan struct{})
	go h.Start(done)

	go hub.CreateAndServeWs(hub.OrganizerHubType, hub.WitnessSocketType, h, witnessPort)
	hub.CreateAndServeWs(hub.OrganizerHubType, hub.ClientSocketType, h, clientPort)

	done <- struct{}{}

	return nil
}
