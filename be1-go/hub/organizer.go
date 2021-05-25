package hub

import (
	"bytes"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"log"
	"path/filepath"
	"student20_pop"
	"sync"

	"student20_pop/message"

	"github.com/xeipuuv/gojsonschema"
	"go.dedis.ch/kyber/v3"
	"go.dedis.ch/kyber/v3/sign/schnorr"
	"golang.org/x/xerrors"
)

type organizerHub struct {
	messageChan chan IncomingMessage

	sync.RWMutex
	channelByID map[string]Channel

	public kyber.Point

	schemas map[string]*gojsonschema.Schema
}

const (
	GenericMsgSchema string = "genericMsgSchema"
	DataSchema       string = "dataSchema"
)

const rootPrefix = "/root/"

// NewOrganizerHub returns a Organizer Hub.
func NewOrganizerHub(public kyber.Point) (Hub, error) {
	// Import the Json schemas defined in the protocol section
	protocolPath, err := filepath.Abs("../protocol")
	if err != nil {
		return nil, xerrors.Errorf("failed to load the path for the json schemas: %v", err)
	}
	protocolPath = "file://" + protocolPath

	schemas := make(map[string]*gojsonschema.Schema)

	// Import the schema for generic messages
	genericMsgLoader := gojsonschema.NewReferenceLoader(protocolPath + "/genericMessage.json")
	genericMsgSchema, err := gojsonschema.NewSchema(genericMsgLoader)
	if err != nil {
		return nil, xerrors.Errorf("failed to load the json schema for generic messages: %v", err)
	}
	schemas[GenericMsgSchema] = genericMsgSchema

	// Impot the schema for data
	dataSchemaLoader := gojsonschema.NewReferenceLoader(protocolPath + "/query/method/message/data/data.json")
	dataSchema, err := gojsonschema.NewSchema(dataSchemaLoader)
	if err != nil {
		return nil, xerrors.Errorf("failed to load the json schema for data: %v", err)
	}
	schemas[DataSchema] = dataSchema

	// Create the organizer hub
	return &organizerHub{
		messageChan: make(chan IncomingMessage),
		channelByID: make(map[string]Channel),
		public:      public,
		schemas:     schemas,
	}, nil
}

// RemoveClient removes the client from this hub.
func (o *organizerHub) RemoveClientSocket(client *ClientSocket) {
	o.RLock()
	defer o.RUnlock()

	for _, channel := range o.channelByID {
		channel.Unsubscribe(client, message.Unsubscribe{})
	}
}

// Recv accepts a message and enques it for processing in the hub.
func (o *organizerHub) Recv(msg IncomingMessage) {
	log.Printf("organizerHub::Recv")
	o.messageChan <- msg
}

func (o *organizerHub) handleMessageFromClient(incomingMessage *IncomingMessage) {
	client := ClientSocket{
		incomingMessage.Socket,
	}
	byteMessage := incomingMessage.Message

	// Check if the GenericMessage has a field "id"
	genericMsg := &message.GenericMessage{}
	id, ok := genericMsg.UnmarshalID(byteMessage)
	if !ok {
		log.Printf("The message does not have a valid `id` field")
		client.SendError(nil, &message.Error{
			Code:        -1,
			Description: "The message does not have a valid `id` field",
		})
		return
	}

	// Verify the message
	err := o.verifyJson(byteMessage, GenericMsgSchema)
	if err != nil {
		log.Printf("failed to verify incoming message: %v", err)
		client.SendError(&id, err)
		return
	}

	// Unmarshal the message
	err = json.Unmarshal(byteMessage, genericMsg)
	if err != nil {
		log.Printf("failed to unmarshal incoming message: %v", err)
		client.SendError(&id, err)
		return
	}

	query := genericMsg.Query

	if query == nil {
		return
	}

	channelID := query.GetChannel()
	log.Printf("channel: %s", channelID)

	if channelID == "/root" {
		if query.Publish == nil {
			log.Printf("only publish is allowed on /root")
			client.SendError(&id, err)
			return
		}

		// Check if the structure of the message is correct
		msg := query.Publish.Params.Message

		// Verify the data
		err := o.verifyJson(msg.RawData, DataSchema)
		if err != nil {
			log.Printf("failed to validate the data: %v", err)
			client.SendError(&id, err)
			return
		}

		// Unmarshal the data
		err = query.Publish.Params.Message.VerifyAndUnmarshalData()
		if err != nil {
			log.Printf("failed to verify and unmarshal data: %v", err)
			client.SendError(&id, err)
			return
		}

		if query.Publish.Params.Message.Data.GetAction() == message.DataAction(message.CreateLaoAction) &&
			query.Publish.Params.Message.Data.GetObject() == message.DataObject(message.LaoObject) {
			err := o.createLao(*query.Publish)
			if err != nil {
				log.Printf("failed to create lao: %v", err)
				client.SendError(&id, err)
				return
			}
		} else {
			log.Printf("invalid method: %s", query.GetMethod())
			client.SendError(&id, &message.Error{
				Code:        -1,
				Description: "you may only invoke lao/create on /root",
			})
			return
		}

		status := 0
		result := message.Result{General: &status}
		log.Printf("sending result: %+v", result)
		client.SendResult(id, result)
		return
	}

	if channelID[:6] != rootPrefix {
		log.Printf("channel id must begin with /root/")
		client.SendError(&id, &message.Error{
			Code:        -2,
			Description: "channel id must begin with /root/",
		})
		return
	}

	channelID = channelID[6:]
	o.RLock()
	channel, ok := o.channelByID[channelID]
	if !ok {
		log.Printf("invalid channel id: %s", channelID)
		client.SendError(&id, &message.Error{
			Code:        -2,
			Description: fmt.Sprintf("channel with id %s does not exist", channelID),
		})
		return
	}
	o.RUnlock()

	method := query.GetMethod()
	log.Printf("method: %s", method)

	msg := []message.Message{}

	// TODO: use constants
	switch method {
	case "subscribe":
		err = channel.Subscribe(&client, *query.Subscribe)
	case "unsubscribe":
		err = channel.Unsubscribe(&client, *query.Unsubscribe)
	case "publish":
		err = channel.Publish(*query.Publish)
	case "message":
		log.Printf("cannot handle broadcasts right now")
	case "catchup":
		msg = channel.Catchup(*query.Catchup)
		// TODO send catchup response to client
	}

	if err != nil {
		log.Printf("failed to process query: %v", err)
		client.SendError(&id, err)
		return
	}

	result := message.Result{}

	if method == "catchup" {
		result.Catchup = msg
	} else {
		general := 0
		result.General = &general
	}

	client.SendResult(id, result)
}

func (o *organizerHub) handleMessageFromWitness(incomingMessage *IncomingMessage) {
	//TODO

}

func (o *organizerHub) handleIncomingMessage(incomingMessage *IncomingMessage) {
	log.Printf("organizerHub::handleMessageFromClient: %s", incomingMessage.Message)

	switch incomingMessage.Socket.socketType {
	case ClientSocketType:
		o.handleMessageFromClient(incomingMessage)
		return
	case WitnessSocketType:
		o.handleMessageFromWitness(incomingMessage)
		return
	default:
		log.Printf("error: invalid socket type")
		return
	}

}

func (o *organizerHub) Start(done chan struct{}) {
	log.Printf("started organizer hub...")

	for {
		select {
		case incomingMessage := <-o.messageChan:
			o.handleIncomingMessage(&incomingMessage)
		case <-done:
			return
		}
	}
}

func (o *organizerHub) verifyJson(byteMessage []byte, schemaName string) error {
	// Validate the Json "byteMessage" with a schema
	messageLoader := gojsonschema.NewBytesLoader(byteMessage)
	resultErrors, err := o.schemas[schemaName].Validate(messageLoader)
	if err != nil {
		return &message.Error{
			Code:        -1,
			Description: err.Error(),
		}
	}
	errorsList := resultErrors.Errors()
	descriptionErrors := ""
	// Concatenate all error descriptions
	for index, e := range errorsList {
		descriptionErrors += fmt.Sprintf(" (%d) %s", index+1, e.Description())
	}

	if len(errorsList) > 0 {
		return &message.Error{
			Code:        -1,
			Description: descriptionErrors,
		}
	}

	return nil
}

func (o *organizerHub) createLao(publish message.Publish) error {
	o.Lock()
	defer o.Unlock()

	data, ok := publish.Params.Message.Data.(*message.CreateLAOData)
	if !ok {
		return &message.Error{
			Code:        -4,
			Description: "failed to cast data to CreateLAOData",
		}
	}

	encodedID := base64.URLEncoding.EncodeToString(data.ID)
	if _, ok := o.channelByID[encodedID]; ok {
		return &message.Error{
			Code:        -3,
			Description: "failed to create lao: another one with the same ID exists",
		}
	}

	if _, ok := o.channelByID[encodedID]; ok {
		return &message.Error{
			Code:        -3,
			Description: "failed to create lao: another one with the same ID exists",
		}
	}
	laoChannelID := rootPrefix + encodedID

	laoCh := laoChannel{
		rollCall:    rollCall{},
		attendees:   make(map[string]struct{}),
		baseChannel: createBaseChannel(o, laoChannelID),
	}
	messageID := base64.URLEncoding.EncodeToString(publish.Params.Message.MessageID)
	laoCh.inbox[messageID] = *publish.Params.Message

	o.channelByID[encodedID] = &laoCh

	return nil
}

type laoChannel struct {
	rollCall  rollCall
	attendees map[string]struct{}
	*baseChannel
}

type rollCallState string

const (
	Open    rollCallState = "open"
	Closed  rollCallState = "closed"
	Created rollCallState = "created"
)

type rollCall struct {
	state rollCallState
	id    string
}

func (c *laoChannel) Publish(publish message.Publish) error {
	err := c.baseChannel.VerifyPublishMessage(publish)
	if err != nil {
		return message.NewError("failed to verify Publish message on a lao channel", err)
	}

	msg := publish.Params.Message

	data := msg.Data

	object := data.GetObject()

	switch object {
	case message.LaoObject:
		err = c.processLaoObject(*msg)
	case message.MeetingObject:
		err = c.processMeetingObject(data)
	case message.MessageObject:
		err = c.processMessageObject(msg.Sender, data)
	case message.RollCallObject:
		err = c.processRollCallObject(*msg)
	case message.ElectionObject:
		err = c.processElectionObject(*msg)
	}

	if err != nil {
		log.Printf("failed to process %s object: %v", object, err)
		return xerrors.Errorf("failed to process %s object: %v", object, err)
	}

	c.broadcastToAllClients(*msg)
	return nil
}

func (c *laoChannel) processLaoObject(msg message.Message) error {
	action := message.LaoDataAction(msg.Data.GetAction())
	msgIDEncoded := base64.URLEncoding.EncodeToString(msg.MessageID)

	switch action {
	case message.UpdateLaoAction:
	case message.StateLaoAction:
		err := c.processLaoState(msg.Data.(*message.StateLAOData))
		if err != nil {
			log.Printf("failed to process lao/state: %v", err)
			return xerrors.Errorf("failed to process lao/state: %v", err)
		}
	default:
		return message.NewInvalidActionError(message.DataAction(action))
	}

	c.inboxMu.Lock()
	c.inbox[msgIDEncoded] = msg
	c.inboxMu.Unlock()

	return nil
}

func (c *laoChannel) processLaoState(data *message.StateLAOData) error {
	// Check if we have the update message
	updateMsgIDEncoded := base64.URLEncoding.EncodeToString(data.ModificationID)

	c.inboxMu.RLock()
	updateMsg, ok := c.inbox[updateMsgIDEncoded]
	c.inboxMu.RUnlock()

	if !ok {
		return &message.Error{
			Code:        -4,
			Description: fmt.Sprintf("cannot find lao/update_properties with ID: %s", updateMsgIDEncoded),
		}
	}

	// Check if the signatures are from witnesses we need. We maintain
	// the current state of witnesses for a LAO in the channel instance
	// TODO: threshold signature verification

	c.witnessMu.Lock()
	match := 0
	expected := len(c.witnesses)
	// TODO: O(N^2), O(N) possible
	for i := 0; i < expected; i++ {
		for j := 0; j < len(data.ModificationSignatures); j++ {
			if bytes.Equal(c.witnesses[i], data.ModificationSignatures[j].Witness) {
				match++
				break
			}
		}
	}
	c.witnessMu.Unlock()

	if match != expected {
		return &message.Error{
			Code:        -4,
			Description: fmt.Sprintf("not enough witness signatures provided. Needed %d got %d", expected, match),
		}
	}

	// Check if the signatures match
	for _, pair := range data.ModificationSignatures {
		err := schnorr.VerifyWithChecks(student20_pop.Suite, pair.Witness, data.ModificationID, pair.Signature)
		if err != nil {
			pk := base64.URLEncoding.EncodeToString(pair.Witness)
			return &message.Error{
				Code:        -4,
				Description: fmt.Sprintf("signature verification failed for witness %s", pk),
			}
		}
	}

	// Check if the updates are consistent with the update message
	updateMsgData, ok := updateMsg.Data.(*message.UpdateLAOData)
	if !ok {
		return &message.Error{
			Code:        -4,
			Description: fmt.Sprintf("modification id %s refers to a message which is not lao/update_properties", updateMsgIDEncoded),
		}
	}

	err := compareLaoUpdateAndState(updateMsgData, data)
	if err != nil {
		return xerrors.Errorf("failure while comparing lao/update and lao/state")
	}

	return nil
}

func compareLaoUpdateAndState(update *message.UpdateLAOData, state *message.StateLAOData) error {
	if update.LastModified != state.LastModified {
		return &message.Error{
			Code:        -4,
			Description: fmt.Sprintf("mismatch between last modified: expected %d got %d", update.LastModified, state.LastModified),
		}
	}

	if update.Name != state.Name {
		return &message.Error{
			Code:        -4,
			Description: fmt.Sprintf("mismatch between name: expected %d got %d", update.LastModified, state.LastModified),
		}
	}

	if update.Name != state.Name {
		return &message.Error{
			Code:        -4,
			Description: fmt.Sprintf("mismatch between name: expected %d got %d", update.LastModified, state.LastModified),
		}
	}

	M := len(update.Witnesses)
	N := len(state.Witnesses)

	if M != N {
		return &message.Error{
			Code:        -4,
			Description: fmt.Sprintf("mismatch between witness count: expected %d got %d", M, N),
		}
	}

	match := 0

	for i := 0; i < M; i++ {
		for j := 0; j < N; j++ {
			if bytes.Equal(update.Witnesses[i], state.Witnesses[j]) {
				match++
				break
			}
		}
	}

	if match != M {
		return &message.Error{
			Code:        -4,
			Description: fmt.Sprintf("mismatch between witness keys: expected %d keys to match but %d matched", M, match),
		}
	}

	return nil
}

func (c *laoChannel) processMeetingObject(data message.Data) error {
	action := message.MeetingDataAction(data.GetAction())

	switch action {
	case message.CreateMeetingAction:
	case message.UpdateMeetingAction:
	case message.StateMeetingAction:
	}

	return nil
}

func (c *laoChannel) processMessageObject(public message.PublicKey, data message.Data) error {
	action := message.MessageDataAction(data.GetAction())

	switch action {
	case message.WitnessAction:
		witnessData := data.(*message.WitnessMessageData)

		msgEncoded := base64.URLEncoding.EncodeToString(witnessData.MessageID)

		err := schnorr.VerifyWithChecks(student20_pop.Suite, public, witnessData.MessageID, witnessData.Signature)
		if err != nil {
			return &message.Error{
				Code:        -4,
				Description: "invalid witness signature",
			}
		}

		c.inboxMu.Lock()
		msg, ok := c.inbox[msgEncoded]
		if !ok {
			// TODO: We received a witness signature before the message itself.
			// We ignore it for now but it might be worth keeping it until we
			// actually receive the message
			log.Printf("failed to find message_id %s for witness message", msgEncoded)
			c.inboxMu.Unlock()
			return nil
		}
		msg.WitnessSignatures = append(msg.WitnessSignatures, message.PublicKeySignaturePair{
			Witness:   public,
			Signature: witnessData.Signature,
		})
		c.inboxMu.Unlock()
	default:
		return message.NewInvalidActionError(message.DataAction(action))
	}

	return nil
}

func (c *laoChannel) processRollCallObject(msg message.Message) error {
	sender := msg.Sender
	data := msg.Data

	// Check if the sender of the roll call message is the organizer
	senderPoint := student20_pop.Suite.Point()
	err := senderPoint.UnmarshalBinary(sender)
	if err != nil {
		return xerrors.Errorf("failed to unmarshal public key of the sender: %v", err)
	}

	if !c.hub.public.Equal(senderPoint) {
		return &message.Error{
			Code:        -5,
			Description: "The sender of the roll call message has a different public key from the organizer",
		}
	}

	action := message.RollCallAction(data.GetAction())

	switch action {
	case message.CreateRollCallAction:
		err = c.processCreateRollCall(data)
	case message.RollCallAction(message.OpenRollCallAction), message.RollCallAction(message.ReopenRollCallAction):
		err = c.processOpenRollCall(data, action)
	case message.CloseRollCallAction:
		err = c.processCloseRollCall(data)
	default:
		return message.NewInvalidActionError(message.DataAction(action))
	}

	if err != nil {
		return xerrors.Errorf("failed to process %v roll-call action: %v", action, err)
	}

	msgIDEncoded := base64.URLEncoding.EncodeToString(msg.MessageID)
	c.inboxMu.Lock()
	c.inbox[msgIDEncoded] = msg
	c.inboxMu.Unlock()

	return nil
}

func (c *laoChannel) processElectionObject(msg message.Message) error {
	action := message.ElectionAction(msg.Data.GetAction())

	if action != message.ElectionSetupAction {
		return &message.Error{
			Code:        -1,
			Description: fmt.Sprintf("invalid action: %s", action),
		}
	}

	err := c.createElection(msg)
	if err != nil {
		return xerrors.Errorf("failed to setup the election %v", err)
	}

	log.Printf("Election has created with success")
	return nil
}
