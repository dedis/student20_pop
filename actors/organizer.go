/*
This class implements the functions an organizer provides. It stores messages in the database using the db package
and create and sends appropriate response depending on what message was received.
*/
package actors

import (
	"fmt"
	"log"
	"student20_pop/db"
	"student20_pop/event"
	"student20_pop/lib"
	"student20_pop/message"
	"student20_pop/parser"
	"student20_pop/security"
)

type Organizer struct {
	PublicKey string
	database  string
}

func NewOrganizer(pkey string, db string) *Organizer {
	return &Organizer{
		PublicKey: pkey,
		database:  db,
	}
}

/** processes what is received from the websocket */
// message_ stands for message but renamed to avoid clashing with package name
func (o *Organizer) HandleWholeMessage(receivedMsg []byte, userId int) (msgAndChannel []lib.MessageAndChannel, responseToSender []byte) {
	// in case the message is already an answer message (positive ack or error), ignore and answer noting to avoid falling into infinite error loops
	isAnswer, err := filterAnswers(receivedMsg)
	if err != nil {
		return nil, parser.ComposeResponse(lib.ErrIdNotDecoded, nil, message.Query{})
	}
	if isAnswer {
		return nil, nil
	}

	query, err := parser.ParseQuery(receivedMsg)
	if err != nil {
		return nil, parser.ComposeResponse(lib.ErrIdNotDecoded, nil, query)
	}

	var history []byte = nil
	var msg []lib.MessageAndChannel = nil

	switch query.Method {
	case "subscribe":
		msg, err = nil, handleSubscribe(query, userId)
	case "unsubscribe":
		msg, err = nil, handleUnsubscribe(query, userId)
	case "publish":
		msg, err = o.handlePublish(query)
	case "message":
		msg, err = o.handleMessage(query)
	//Or they are only notification, and we just want to check that it was a success
	case "catchup":
		history, err = o.handleCatchup(query)
	default:
		fmt.Printf("method not recognized, generating default response")
		msg, err = nil, lib.ErrRequestDataInvalid
	}

	return msg, parser.ComposeResponse(err, history, query)
}

func (o *Organizer) handleMessage(query message.Query) (msgAndChannel []lib.MessageAndChannel, err error) {
	params, errs := parser.ParseParamsIncludingMessage(query.Params)
	if errs != nil {
		fmt.Printf("unable to analyse paramsLight in handleMessage()")
		return nil, lib.ErrRequestDataInvalid
	}

	msg, errs := parser.ParseMessage(params.Message)
	if errs != nil {
		fmt.Printf("unable to analyse Message in handleMessage()")
		return nil, lib.ErrRequestDataInvalid
	}

	data, errs := parser.ParseData(string(msg.Data))
	if errs != nil {
		fmt.Printf("unable to analyse data in handleMessage()")
		return nil, lib.ErrRequestDataInvalid
	}

	errs = db.CreateMessage(msg, params.Channel, o.database)
	if errs != nil {
		return nil, errs
	}

	switch data["object"] {
	case "message":
		switch data["action"] {
		case "witness":
			return o.handleWitnessMessage(msg, params.Channel, query)
		default:
			return nil, lib.ErrRequestDataInvalid
		}
	case "state":
		{
			return o.handleLAOState(msg, params.Channel, query)
		}
	default:
		return nil, lib.ErrRequestDataInvalid
	}

}

/* handles a received publish message */
func (o *Organizer) handlePublish(query message.Query) (msgAndChannel []lib.MessageAndChannel, err error) {
	params, errs := parser.ParseParamsIncludingMessage(query.Params)
	if errs != nil {
		fmt.Printf("1. unable to analyse paramsLight in handlePublish()")
		return nil, lib.ErrRequestDataInvalid
	}

	msg, errs := parser.ParseMessage(params.Message)
	if errs != nil {
		fmt.Printf("2. unable to analyse Message in handlePublish()")
		return nil, lib.ErrRequestDataInvalid
	}

	errs = security.MessageIsValid(msg)
	if errs != nil {
		fmt.Printf("7")
		return nil, lib.ErrRequestDataInvalid
	}

	data, errs := parser.ParseData(string(msg.Data))
	if errs != nil {
		fmt.Printf("3. unable to analyse data in handlePublish()")
		return nil, lib.ErrRequestDataInvalid
	}

	switch data["object"] {
	case "lao":
		switch data["action"] {
		case "create":
			return o.handleCreateLAO(msg, params.Channel, query)
		case "update_properties":
			return o.handleUpdateProperties(msg, params.Channel, query)
		case "state":
			return o.handleLAOState(msg, params.Channel, query) // should never happen
		default:
			return nil, lib.ErrInvalidAction
		}

	case "message":
		switch data["action"] {
		case "witness":
			// TODO : send state broadcast if more signatures than threshold
			return o.handleWitnessMessage(msg, params.Channel, query)
			// TODO : state broadcast done on root
		default:
			return nil, lib.ErrInvalidAction
		}
	case "roll call":
		switch data["action"] {
		case "create":
			return o.handleCreateRollCall(msg, params.Channel, query)
		//case "state":  TODO : waiting on protocol definition
		default:
			return nil, lib.ErrInvalidAction
		}
	case "meeting":
		switch data["action"] {
		case "create":
			return o.handleCreateMeeting(msg, params.Channel, query)
		case "state": //
			// TODO: waiting on protocol definition
			return nil, lib.ErrNotYetImplemented
		default:
			return nil, lib.ErrInvalidAction
		}
	case "poll":
		switch data["action"] {
		case "create":
			return o.handleCreatePoll(msg, params.Channel, query)
		case "state":
			// TODO: waiting on protocol definition
			return nil, lib.ErrNotYetImplemented
		default:
			return nil, lib.ErrInvalidAction
		}
	default:
		fmt.Printf("data[action] (%v) not recognized in handlepublish, generating default response ", data["action"])
		return nil, lib.ErrRequestDataInvalid
	}
}

/* handles the creation of a LAO */
func (o *Organizer) handleCreateLAO(msg message.Message, canal string, query message.Query) (msgAndChannel []lib.MessageAndChannel, err error) {

	if canal != "/root" {
		return nil, lib.ErrInvalidResource
	}

	data, errs := parser.ParseDataCreateLAO(msg.Data)
	if errs != nil {
		return nil, lib.ErrInvalidResource
	}

	if !security.LAOIsValid(data, true) {
		return nil, lib.ErrInvalidResource
	}

	errs = db.CreateMessage(msg, canal, o.database)
	if errs != nil {
		return nil, err
	}

	lao := event.LAO{
		ID:            data.ID,
		Name:          data.Name,
		Creation:      data.Creation,
		OrganizerPKey: data.Organizer,
		Witnesses:     data.Witnesses,
	}
	errs = db.CreateChannel(lao, o.database)
	if errs != nil {
		return nil, err
	}

	msgAndChan := []lib.MessageAndChannel{{
		Message: parser.ComposeBroadcastMessage(query),
		Channel: []byte(canal),
	}}

	return msgAndChan, nil
}

func (o *Organizer) handleCreateRollCall(msg message.Message, canal string, query message.Query) (msgAndChannel []lib.MessageAndChannel, err error) {
	if canal == "/root" {
		return nil, lib.ErrInvalidResource
	}

	data, errs := parser.ParseDataCreateRollCall(msg.Data)
	if errs != nil {
		return nil, lib.ErrInvalidResource
	}

	if !security.RollCallCreatedIsValid(data, msg) {
		return nil, errs
	}

	// don't need to check for validity if we use json schema
	rollCall := event.RollCall{ID: data.ID,
		Name:     data.Name,
		Creation: data.Creation,
		Location: data.Location,
		Start:    data.Start,
		End:      data.End,
		Extra:    data.Extra,
	}
	errs = db.CreateChannel(rollCall, o.database)
	if errs != nil {
		return nil, errs
	}

	errs = db.CreateMessage(msg, canal, o.database)
	if errs != nil {
		return nil, errs
	}

	msgAndChan := []lib.MessageAndChannel{{
		Message: parser.ComposeBroadcastMessage(query),
		Channel: []byte(canal),
	}}

	return msgAndChan, nil
}

func (o *Organizer) handleCreateMeeting(msg message.Message, canal string, query message.Query) (msgAndChannel []lib.MessageAndChannel, err error) {

	if canal == "/root" {
		return nil, lib.ErrInvalidResource
	}

	data, errs := parser.ParseDataCreateMeeting(msg.Data)
	if errs != nil {
		return nil, lib.ErrInvalidResource
	}

	// don't need to check for validity if we use json schema
	meeting := event.Meeting{ID: data.ID,
		Name:     data.Name,
		Creation: data.Creation,
		Location: data.Location,
		Start:    data.Start,
		End:      data.End,
		Extra:    data.Extra,
	}
	errs = db.CreateChannel(meeting, o.database)
	if errs != nil {
		return nil, errs
	}
	errs = db.CreateMessage(msg, canal, o.database)
	if errs != nil {
		return nil, errs
	}

	msgAndChan := []lib.MessageAndChannel{{
		Message: parser.ComposeBroadcastMessage(query),
		Channel: []byte(canal),
	}}

	return msgAndChan, nil
}

func (o *Organizer) handleCreatePoll(msg message.Message, canal string, query message.Query) (msgAndChannel []lib.MessageAndChannel, err error) {

	if canal == "/root" {
		return nil, lib.ErrInvalidResource
	}

	data, errs := parser.ParseDataCreatePoll(msg.Data)
	if errs != nil {
		return nil, lib.ErrInvalidResource
	}

	poll := event.Poll{ID: data.ID,
		Name:     data.Name,
		Creation: data.Creation,
		Location: data.Location,
		Start:    data.Start,
		End:      data.End,
		Extra:    data.Extra,
	}

	errs = db.CreateChannel(poll, o.database)
	if errs != nil {
		return nil, err
	}

	msgAndChan := []lib.MessageAndChannel{{
		Message: parser.ComposeBroadcastMessage(query),
		Channel: []byte(canal),
	}}

	return msgAndChan, nil
}

func (o *Organizer) handleUpdateProperties(msg message.Message, canal string, query message.Query) (msgAndChannel []lib.MessageAndChannel, err error) {
	msgAndChan := []lib.MessageAndChannel{{
		Message: parser.ComposeBroadcastMessage(query),
		Channel: []byte(canal),
	}}
	return msgAndChan, db.CreateMessage(msg, canal, o.database)
}

func (o *Organizer) handleWitnessMessage(msg message.Message, canal string, query message.Query) (msgAndChannel []lib.MessageAndChannel, err error) {

	data, errs := parser.ParseDataWitnessMessage(msg.Data)
	if errs != nil {
		log.Printf("unable to parse received Message in handleWitnessMessage()")
		return nil, errs
	}

	//retrieve message to sign from database
	toSign := db.GetMessage([]byte(canal), []byte(data.Message_id), o.database)
	if toSign == nil {
		return nil, lib.ErrInvalidResource
	}

	toSignStruct, errs := parser.ParseMessage(toSign)
	if errs != nil {
		log.Printf("unable to parse stored Message in handleWitnessMessage()")
		return nil, lib.ErrRequestDataInvalid
	}

	errs = security.VerifySignature(msg.Sender, toSignStruct.Data, data.Signature)
	if errs != nil {
		return nil, errs
	}

	//if message was already signed by this witness, returns an error
	_, found := lib.FindStr(toSignStruct.WitnessSignatures, data.Signature)
	if found {
		return nil, lib.ErrResourceAlreadyExists
	}

	toSignStruct.WitnessSignatures = append(toSignStruct.WitnessSignatures, data.Signature)

	// update "LAOUpdateProperties" message in DB
	errs = db.UpdateMessage(toSignStruct, canal, o.database)
	if errs != nil {
		return nil, lib.ErrDBFault
	}
	//store received message in DB
	errs = db.CreateMessage(msg, canal, o.database)
	if errs != nil {
		return nil, lib.ErrDBFault
	}
	msgAndChan := []lib.MessageAndChannel{{
		Message: parser.ComposeBroadcastMessage(query),
		Channel: []byte(canal),
	}}
	//broadcast received message
	return msgAndChan, nil
}

func (o *Organizer) handleCatchup(query message.Query) ([]byte, error) {
	// TODO maybe pass userId as an arg in order to check access rights later on?
	params, err := parser.ParseParams(query.Params)
	if err != nil {
		fmt.Printf("unable to analyse paramsLight in handleCatchup()")
		return nil, lib.ErrRequestDataInvalid
	}
	history := db.GetChannel([]byte(params.Channel), o.database)

	return history, nil
}

//just to implement the interface, this function is not needed for the Organizer (as he's the one sending this message)
func (o *Organizer) handleLAOState(msg message.Message, chann string, query message.Query) (msgAndChannel []lib.MessageAndChannel, err error) {
	return nil, lib.ErrInvalidAction
}
