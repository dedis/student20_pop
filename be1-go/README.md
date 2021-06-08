# student_21_pop
Proof-of-personhood, Spring 2021. Go back-end version.

## Overview

You may build the `pop` CLI to interact with the server by executing `go build -o pop ./cli/`.

```bash
./pop organizer -h
NAME:
   pop organizer - manage the organizer
   pop witness - manage the witness

USAGE:
   pop organizer command [command options] [arguments...]
   pop witness command [command options] [arguments...]

COMMANDS:
   serve    start the organizer or witness server
   help, h  Shows a list of commands or help for one command

OPTIONS:
   --public-key value, --pk value          base64url encoded organizer's public key
   --help, -h                              show help (default: false)

```

<<<<<<< HEAD
You may start the organizer server at ports `9000` for clients and `9001` for witnesses by executing `./pop organizer --pk "<base64url encoded pk>" serve`.
```bash
./pop organizer --pk "<base64url encoded pk>" serve -help
NAME:
   pop organizer serve - start the organizer server

USAGE:
   pop organizer serve [command options] [arguments...]

OPTIONS:
   --protocol-path value, --proto value  path to the protocol, if it is not set the github URL of the protocol is used instead
   --client-port value, --cp value       port to listen websocket connections from clients on (default: 9000)
   --witness-port value, --wp value      port to listen websocket connections from witnesses on (default: 9001)
   --help, -h                            show help (default: false)

```

Please use the `-cp` and `-wp` flags to specify an alternative port.
The full path to connect to the organizer as a client is `ws://host:clientport/organizer/client/` and as a witness `ws://host:witnessport/organizer/witness/`. 

You may start the witness server at ports `9002` for clients and `9001` for organizer by executing `./pop witness --pk "<base64 encoded pk>" serve`.

```bash
./pop witness --pk "<base64url encoded pk>" serve -help
NAME:
   pop witness serve - start the organizer server

USAGE:
   pop witness serve [command options] [arguments...]

OPTIONS:
   --protocol-path value, --proto value    path to the protocol, if it is not set the github URL of the protocol is used instead
   --organizer-address value, --org value  ip address of organizer (default: "localhost")
   --organizer-port value, --op value      port on which to connect to organizer websocket (default: 9001)
   --client-port value, --cp value         port to listen websocket connections from clients on (default: 9002)
   --help, -h                              show help (default: false)

```

Please use the `-cp` and `-op` flags to specify an alternative port.
The full path to connect to the organizer as a client is `ws://host:clientport/organizer/client/` and as a witness `ws://host:witnessport/organizer/witness/`.

## Packages

- `cli`: Entrypoint for starting the PoP server
- `message`: Contains structs that are used to marshal/unmarshal websocket payload
- `hub`: Defined an interface for the actions supported by the server. Refer to the interfaces in `hub/hub.go` for more information.


## Dependencies
Currently, the project works with the following libraries:
* [dedis/kyber](https://github.com/dedis/kyber) which adds support for cryptographic operations like signature verification.
* [gorilla/websocket](https://github.com/gorilla/websocket) is a websocket package for golang. We chose to use this 
package because it offers a good API, and it is more complete than the websocket package offered by the standard library.


### Sample Data

Sample test data for different websocket payloads is available at `test/json_test_strings.txt`.

## Unit-tests
Some unit-tests exist directly in their corresponding packages, as per Go specification. They can be run with `go test -v ./...` (recursive from the top-level folder) or simply `go test` for the current package.


## Potential improvements

### WebSockets
Maybe we should consider using websocket secure (websocket over TLS) instead of "standards" websocket (over TCP). It 
would prevent from MitM attacks, as we currently don't offer data privacy.
