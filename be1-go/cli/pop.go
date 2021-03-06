package main

import (
	"context"
	"log"
	"os"
	"student20_pop/cli/organizer"
	"student20_pop/cli/witness"

	"github.com/urfave/cli/v2"
)

func main() {
	publicKeyFlag := &cli.StringFlag{
		Name:    "public-key",
		Aliases: []string{"pk"},
		Usage:   "base64url encoded server's public key",
	}
	organizerAddressFlag := &cli.StringFlag{
		Name:    "organizer-address",
		Aliases: []string{"org"},
		Usage:   "address and port of organizer",
		Value:   "localhost:9002",
	}
	clientPortFlag := &cli.IntFlag{
		Name:    "client-port",
		Aliases: []string{"cp"},
		Usage:   "port to listen websocket connections from clients on",
		Value:   9000,
	}
	witnessPortFlag := &cli.IntFlag{
		Name:    "witness-port",
		Aliases: []string{"wp"},
		Usage:   "port to listen websocket connections from witnesses on",
		Value:   9002,
	}
	otherWitnessFlag := &cli.StringSliceFlag{
		Name:    "other-witness",
		Aliases: []string{"ow"},
		Usage:   "address and port to connect to other witness",
	}

	app := &cli.App{
		Name:  "pop",
		Usage: "backend for the PoP project",
		Commands: []*cli.Command{
			{
				Name:  "organizer",
				Usage: "manage the organizer",
				Flags: []cli.Flag{
					publicKeyFlag,
				},
				Subcommands: []*cli.Command{
					{
						Name:  "serve",
						Usage: "start the organizer server",
						Flags: []cli.Flag{
							clientPortFlag,
							witnessPortFlag,
						},
						Action: organizer.Serve,
					},
				},
			},
			{
				Name:  "witness",
				Usage: "manage the witness",
				Flags: []cli.Flag{
					publicKeyFlag,
				},
				Subcommands: []*cli.Command{
					{
						Name:  "serve",
						Usage: "start the witness server",
						Flags: []cli.Flag{
							organizerAddressFlag,
							clientPortFlag,
							witnessPortFlag,
							otherWitnessFlag,
						},
						Action: witness.Serve,
					},
				},
			},
		},
	}

	err := app.RunContext(context.Background(), os.Args)
	if err != nil {
		log.Fatal(err)
	}
}
