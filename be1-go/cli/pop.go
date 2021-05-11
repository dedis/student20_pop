package main

import (
	"log"
	"os"
	"student20_pop/cli/organizer"
	"student20_pop/cli/witness"

	"github.com/urfave/cli/v2"
)

func main() {
	app := &cli.App{
		Name:  "pop",
		Usage: "backend for the PoP project",
		Commands: []*cli.Command{
			{
				Name:  "organizer",
				Usage: "manage the organizer",
				Flags: []cli.Flag{
					&cli.StringFlag{
						Name:    "public-key",
						Aliases: []string{"pk"},
						Usage:   "base64 encoded organizer's public key",
					},
				},
				Subcommands: []*cli.Command{
					{
						Name:  "serve",
						Usage: "start the organizer server",
						Flags: []cli.Flag{
							&cli.IntFlag{
								Name:    "client-port",
								Aliases: []string{"cp"},
								Usage:   "port to listen websocket connections from clients on",
								Value:   9000,
							},
							&cli.IntFlag{
								Name:    "witness-port",
								Aliases: []string{"wp"},
								Usage:   "port to listen websocket connections from witnesses on",
								Value:   9001,
							},
						},
						Action: organizer.Serve,
					},
				},
			},
			{
				Name:  "witness",
				Usage: "manage the witness",
				Flags: []cli.Flag{
					&cli.StringFlag{
						Name:    "public-key",
						Aliases: []string{"pk"},
						Usage:   "base64 encoded witness's public key",
					},
				},
				Subcommands: []*cli.Command{
					{
						Name:  "serve",
						Usage: "start the organizer server",
						Flags: []cli.Flag{
							&cli.StringFlag{
								Name:    "organizer-address",
								Aliases: []string{"org"},
								Usage:   "ip address of organizer",
								Value:   "localhost",
							},
							&cli.IntFlag{
								Name:    "organizer-port",
								Aliases: []string{"op"},
								Usage:   "port on which to connect to organizer websocket",
								Value:   9000,
							},
						},
						Action: witness.Serve,
					},
				},
			},
		},
	}

	err := app.Run(os.Args)
	if err != nil {
		log.Fatal(err)
	}
}
