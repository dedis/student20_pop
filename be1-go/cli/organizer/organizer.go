package organizer

import (
	"context"
	"encoding/base64"
	"github.com/urfave/cli/v2"
	"golang.org/x/xerrors"
	"student20_pop"
	"student20_pop/hub"
	"student20_pop/network"
	"sync"
)

// Serve parses the CLI arguments and spawns a hub and a websocket server.
func Serve(cliCtx *cli.Context) error {

	// get command line args which specify public key, port to use for clients and witnesses
	clientPort := cliCtx.Int("client-port")
	witnessPort := cliCtx.Int("witness-port")
	if clientPort == witnessPort {
		return xerrors.Errorf("client and witness ports must be different")
	}

	pk := cliCtx.String("public-key")
	if pk == "" {
		return xerrors.Errorf("organizer's public key is required")
	}

	// decode public key and unmarshal public key
	pkBuf, err := base64.URLEncoding.DecodeString(pk)
	if err != nil {
		return xerrors.Errorf("failed to base64url decode public key: %v", err)
	}
	point := student20_pop.Suite.Point()
	err = point.UnmarshalBinary(pkBuf)
	if err != nil {
		return xerrors.Errorf("failed to unmarshal public key: %v", err)
	}

	// create organizer hub
	h, err := hub.NewOrganizerHub(point)
	if err != nil {
		return xerrors.Errorf("failed create the organizer hub: %v", err)
	}

	// make context release resources associated with it when all operations are done
	ctx, cancel := context.WithCancel(cliCtx.Context)
	defer cancel()

	// create wait group which waits for goroutines to finish
	wg := &sync.WaitGroup{}

	// increment wait group and create and serve servers for witnesses and clients
	clientSrv := network.CreateAndServeWS(ctx, hub.OrganizerHubType, hub.ClientSocketType, h, clientPort, wg)
	witnessSrv := network.CreateAndServeWS(ctx, hub.OrganizerHubType, hub.WitnessSocketType, h, witnessPort, wg)

	// increment wait group and launch organizer hub
	go h.Start(ctx, wg)

	// shut down client server and witness server when ctrl+c received
	network.ShutdownServers(ctx, witnessSrv, clientSrv)

	// cancel the context
	cancel()

	// wait for all goroutines to finish
	wg.Wait()

	return nil
}
