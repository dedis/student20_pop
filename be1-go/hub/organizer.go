package hub

import (
	"bytes"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"log"
	"student20_pop"
	"sync"
	"log"
	"student20_pop/message"
	"go.dedis.ch/kyber/v3"
	"go.dedis.ch/kyber/v3/sign/schnorr"
	"golang.org/x/xerrors"
)

type organizerHub struct {
	messageChan chan IncomingMessage

	sync.RWMutex
	channelByID map[string]Channel

	public kyber.Point
}

// NewOrganizerHub returns a Organizer Hub.
func NewOrganizerHub(public kyber.Point) Hub {
	return &organizerHub{
		messageChan: make(chan IncomingMessage),
		channelByID: make(map[string]Channel),
		public:      public,
	}
}

// RemoveClient removes the client from this hub.
func (o *organizerHub) RemoveClient(client *Client) {
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

func (o *organizerHub) handleIncomingMessage(incomingMessage *IncomingMessage) {
	log.Printf("organizerHub::handleIncomingMessage: %s", incomingMessage.Message)

	client := incomingMessage.Client

	// unmarshal the message
	genericMsg := &message.GenericMessage{}
	err := json.Unmarshal(incomingMessage.Message, genericMsg)
	if err != nil {
		log.Printf("failed to unmarshal incoming message: %v", err)
	}

	query := genericMsg.Query

	if query == nil {
		return
	}

	channelID := query.GetChannel()
	log.Printf("channel: %s", channelID)

	id := query.GetID()

	if channelID == "/root" {
		if query.Publish == nil {
			log.Printf("only publish is allowed on /root")
			client.SendError(query.GetID(), err)
			return
		}

		err := query.Publish.Params.Message.VerifyAndUnmarshalData()
		if err != nil {
			log.Printf("failed to verify and unmarshal data: %v", err)
			client.SendError(query.Publish.ID, err)
			return
		}

		if query.Publish.Params.Message.Data.GetAction() == message.DataAction(message.CreateLaoAction) &&
			query.Publish.Params.Message.Data.GetObject() == message.DataObject(message.LaoObject) {
			err := o.createLao(*query.Publish)
			if err != nil {
				log.Printf("failed to create lao: %v", err)
				client.SendError(query.Publish.ID, err)
				return
			}
		} else {
			log.Printf("invalid method: %s", query.GetMethod())
			client.SendError(id, &message.Error{
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

	if channelID[:6] != "/root/" {
		log.Printf("channel id must begin with /root/")
		client.SendError(id, &message.Error{
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
		client.SendError(id, &message.Error{
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
		err = channel.Subscribe(client, *query.Subscribe)
	case "unsubscribe":
		err = channel.Unsubscribe(client, *query.Unsubscribe)
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
		client.SendError(id, err)
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

	encodedID := base64.StdEncoding.EncodeToString(data.ID)
	if _, ok := o.channelByID[encodedID]; ok {
		return &message.Error{
			Code:        -3,
			Description: "failed to create lao: another one with the same ID exists",
		}
	}

	laoChannelID := "/root/" + encodedID

	if _, ok := o.channelByID[encodedID]; ok {
		return &message.Error{
			Code:        -3,
			Description: "failed to create lao: another one with the same ID exists",
		}
	}

	laoCh := laoChannel{
		createBaseChannel(o, laoChannelID),
	}
	messageID := base64.StdEncoding.EncodeToString(publish.Params.Message.MessageID)
	laoCh.inbox[messageID] = *publish.Params.Message

<<<<<<< HEAD
	id := base64.StdEncoding.EncodeToString(data.ID)

	o.channelByID[id] = &laoCh
=======
	o.channelByID[encodedID] = &laoCh
>>>>>>> 8f6810328696e81313f6b4cddf7ab4651d8c102a

	return nil
}

type laoChannel struct {
	*baseChannel
}

<<<<<<< HEAD

=======
>>>>>>> 8f6810328696e81313f6b4cddf7ab4651d8c102a
func (c *laoChannel) Publish(publish message.Publish) error {
	err := c.baseChannel.VerifyPublishMessage(publish)
	if err != nil {
		return xerrors.Errorf("failed to verify Publish message on a lao channel: %v", err)
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
		err = c.processRollCallObject(data)
	}

	if err != nil {
		log.Printf("failed to process %s object: %v", object, err)
		return xerrors.Errorf("failed to process %s object: %v", object, err)
	}

	c.broadcastToAllClients(*msg)
	return nil
}

<<<<<<< HEAD

=======
>>>>>>> 8f6810328696e81313f6b4cddf7ab4651d8c102a
func (c *laoChannel) processLaoObject(msg message.Message) error {
	action := message.LaoDataAction(msg.Data.GetAction())
	msgIDEncoded := base64.StdEncoding.EncodeToString(msg.MessageID)

	switch action {
	case message.UpdateLaoAction:
		c.inboxMu.Lock()
		c.inbox[msgIDEncoded] = msg
		c.inboxMu.Unlock()
	case message.StateLaoAction:
		err := c.processLaoState(msg.Data.(*message.StateLAOData))
		if err != nil {
			log.Printf("failed to process lao/state: %v", err)
			return xerrors.Errorf("failed to process lao/state: %v", err)
		}
	default:
		return &message.Error{
			Code:        -1,
			Description: fmt.Sprintf("invalid action: %s", action),
		}
	}

	return nil
}

func (c *laoChannel) processLaoState(data *message.StateLAOData) error {
	// Check if we have the update message
	updateMsgIDEncoded := base64.StdEncoding.EncodeToString(data.ModificationID)

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
			pk := base64.StdEncoding.EncodeToString(pair.Witness)
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

func (c *laoChannel) broadcastToAllClients(msg message.Message) {
	c.clientsMu.RLock()
	defer c.clientsMu.RUnlock()

	query := message.Query{
		Broadcast: message.NewBroadcast(c.baseChannel.channelID, &msg),
	}

	buf, err := json.Marshal(query)
	if err != nil {
		log.Fatalf("failed to marshal broadcast query: %v", err)
	}

	for client := range c.clients {
		client.Send(buf)
	}
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

		msgEncoded := base64.StdEncoding.EncodeToString(witnessData.MessageID)

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
		return &message.Error{
			Code:        -1,
			Description: fmt.Sprintf("invalid action: %s", action),
		}
	}

	return nil
}

func (c *laoChannel) processRollCallObject(data message.Data) error {
	action := message.RollCallAction(data.GetAction())

	switch action {
	case message.CreateRollCallAction:
	case message.RollCallAction(message.OpenRollCallAction):
	case message.RollCallAction(message.ReopenRollCallAction):
	case message.CloseRollCallAction:
	}

	return nil
}
<<<<<<< HEAD
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
		return xerrors.Errorf("failed to setup the election", err)
	}

	return nil
}

func (c *laoChannel) createElection(msg message.Message) error {
	o := c.hub

	o.Lock()
	defer o.Unlock()

	// Check the data
	data, ok := msg.Data.(*message.ElectionSetupData)

	if !ok {
		return &message.Error{
			Code:        -4,
			Description: "failed to cast data to SetupElectionData",
		}
	}

	// Check if the Lao ID of the message corresponds to the channel ID
	encodedLaoID := base64.StdEncoding.EncodeToString(data.LaoID)
	channelID := c.channelID[6:]
	if channelID != encodedLaoID {
		return &message.Error{
			Code:        -4,
			Description: fmt.Sprintf("Lao ID of the message (Lao: %s) is different from the channelID (channel: %s)", encodedLaoID, channelID),
		}
	}

	// Compute the new election channel id
	encodedElectionID := base64.StdEncoding.EncodeToString(data.ID)
	encodedID := encodedLaoID + "/" + encodedElectionID

	// Create the new election channel
	electionCh := electionChannel{
		createBaseChannel(o, "/root/"+encodedID),
		//TODO : check if this is what was meanth by method ot is it rather pluralityy and stuff??,
		//Also can it happen that there is a valid setup without questions?
		data.StartTime,
		data.EndTime,
		false,
		getAllQuestionsForElectionChannel(data.Questions,data),
	}

	// Add the SetupElection message to the new election channel
	messageID := base64.StdEncoding.EncodeToString(msg.MessageID)
	electionCh.inbox[messageID] = msg

	// Add the new election channel to the organizerHub
	o.channelByID[encodedID] = &electionCh

	return nil
}

func getAllQuestionsForElectionChannel(questions []message.Question,data *message.ElectionSetupData)map[string]question{
	qs := make(map[string]question)
	for _,q := range questions{
		qs[ base64.StdEncoding.EncodeToString(q.ID)] = question{
			q.ID,
			q.BallotOptions,
			make(map[string]validVote),
			data.Questions[0].VotingMethod,
		}
	}
	return qs
}

type electionChannel struct {
	*baseChannel


	// Starting time of the election
	start message.Timestamp

	// Ending time of the election
	end message.Timestamp

	// True if the election is over and false otherwise
	terminated bool

	// Questions asked to the participants
	//the key will be the string representation of the id of type byte[]
	questions map[string]question
}

type question struct {
	// ID of th question
	id []byte

	// Different options
	ballotOptions []message.BallotOption

	// list of all valid votes
	validVotes map[string]validVote

	// Voting method of the election
	method message.VotingMethod
}

type validVote struct {
	// time of the creation of the vote
	voteTime message.Timestamp

	// indexes of the ballot options
	indexes []int

}

func (c *electionChannel) Publish(publish message.Publish) error {
	err := c.baseChannel.VerifyPublishMessage(publish)
	if err != nil {
		return xerrors.Errorf("failed to verify publish message on an election channel: %v", err)
	}

	msg := publish.Params.Message

	data := msg.Data

	object := data.GetObject()

	if object == message.ElectionObject {

		action := message.ElectionAction(data.GetAction())
		switch action {
		case message.CastVoteAction:
			voteData, ok := msg.Data.(*message.CastVoteData)
			if !ok {
				return &message.Error{
					Code:        -4,
					Description: "failed to cast data to CastVoteData",
				}
			}
			if !ok{
				return xerrors.Errorf("Couldn't cast to castVoteData")
			}
			if voteData.CreatedAt > c.end {
				return xerrors.Errorf("Vote casted too late")
			}
			//This should update any previously set vote if the message ids are the same
			messageID := base64.StdEncoding.EncodeToString(msg.MessageID)
			c.inbox[messageID] = *msg
			for _,q := range voteData.Votes{
				QuestionID :=  base64.StdEncoding.EncodeToString(q.QuestionID)
				qs,ok := c.questions[QuestionID]
				if ok{
					earlierVote,ok := qs.validVotes[msg.Sender.String()]
					// if the sender didn't previously cast a vote or if the vote
					//is no longer valid update it
					if !ok  || earlierVote.voteTime > voteData.CreatedAt {
						qs.validVotes[msg.Sender.String()] =
							validVote{voteData.CreatedAt,
								q.VoteIndexes}
					}
				}else{
					return xerrors.Errorf("No Question with this ID exists")
				}
			}
		case message.ElectionEndAction:
		case message.ElectionResultAction:
		}
	}

	return nil
}






=======
>>>>>>> 8f6810328696e81313f6b4cddf7ab4651d8c102a
