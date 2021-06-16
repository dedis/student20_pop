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
   --organizer-address value, --org value  organizer's IP address for witness to connect to organizer (default value "localhost")
   --client-port value, --cp value         port on which to open websocket for clients (default value 9000 for organizer, 9002 for witness)
   --witness-port value, --wp value        port on which to open websocket for witnesses (default value 9002)
   --organizer-port value, --op value      port on which witness connects to organizer (default value 9001)
   --other-witness value, --ow value       address and port on which to connect to another witness, can be used as many times as necessary
   --help, -h                              show help (default: false)

```

You may start the organizer server at ports `9000` for clients and `9001` for witnesses by executing `./pop organizer --pk "<base64url encoded pk>" serve`.
Please use the `-cp` and `-wp` flags to specify an alternative port.
The full path to connect to the organizer as a client is `ws://host:clientport/organizer/client/` and as a witness `ws://host:witnessport/organizer/witness/`.

You may start the witness server at ports `9002` for clients and `9001` for organizer by executing `./pop witness --pk "<base64 encoded pk>" serve`.
Please use the `-cp` and `-op` flags to specify an alternative port.
The full path to connect to the organizer as a client is `ws://host:clientport/organizer/client/` and as a witness `ws://host:witnessport/organizer/witness/`.
Using the `-ow` flag as many times as necessary, you can specify you can specify the `address:port` of each of the other witnesses.

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