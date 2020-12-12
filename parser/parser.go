package parser

import (
	"encoding/json"
	"fmt"
	"student20_pop/lib"
	"student20_pop/message"
)

/**
 * Function that takes a byte array as input and returns
 * a Message struct
 */
func ParseGeneric(generic []byte) (message.Generic, error) {
	m := message.Generic{}
	err := json.Unmarshal(generic, &m)
	return m, err
}

func ParseParamsLight(params json.RawMessage) (message.ParamsLight, error) {
	m := message.ParamsLight{}
	err := json.Unmarshal(params, &m)
	return m, err
}

func ParseParamsFull(params json.RawMessage) (message.ParamsFull, error) {
	m := message.ParamsFull{}
	err := json.Unmarshal(params, &m)
	if err != nil {
		return m, lib.ErrEncodingFault
	}
	d, err := lib.Decode(m.Channel)
	m.Channel = string(d)
	return m, err
}

func ParseMessage(msg json.RawMessage) (message.Message, error) {
	m := message.Message{}
	err := json.Unmarshal(msg, &m)

	d, err := lib.Decode(m.Sender)
	if err != nil {
		return m, lib.ErrEncodingFault
	}
	m.Sender = string(d)

	d, err = lib.Decode(m.Message_id)
	if err != nil {
		return m, lib.ErrEncodingFault
	}
	m.Message_id = string(d)

	d, err = lib.Decode(m.Signature)
	if err != nil {
		return m, lib.ErrEncodingFault
	}
	m.Signature = string(d)

	d, err = lib.Decode(string(m.Data))
	if err != nil {
		return m, lib.ErrEncodingFault
	}
	//TODO super bizarre, il decode correctement la deuxieme fois dans le tests?
	/*d, err = lib.Decode(string(d))
	if err != nil {
		return m, ErrEncodingFault
	}
	*/
	m.Data = d

	for i := 0; i < len(m.WitnessSignatures); i++ {
		d, err = lib.Decode(m.WitnessSignatures[i])
		if err != nil {
			return m, lib.ErrEncodingFault
		}
		m.WitnessSignatures[i] = string(d)
	}
	return m, err
}
func ParseWitnessSignature(witnessSignatures string) (message.ItemWitnessSignatures, error) {
	m := message.ItemWitnessSignatures{}
	err := json.Unmarshal([]byte(witnessSignatures), &m)

	d, err := lib.Decode(m.Signature)
	if err != nil {
		return m, lib.ErrEncodingFault
	}
	m.Signature = string(d)

	d, err = lib.Decode(m.Witness)
	if err != nil {
		return m, lib.ErrEncodingFault
	}
	m.Witness = string(d)

	return m, err
}
func ParseData(data string) (message.Data, error) {
	m := message.Data{}
	err := json.Unmarshal([]byte(data), &m)
	return m, err
}

// TODO : Change name toParseDataLAO ? Because creation / state are the same. Or not ?
func ParseDataCreateLAO(data json.RawMessage) (message.DataCreateLAO, error) {
	m := message.DataCreateLAO{}
	err := json.Unmarshal(data, &m)
	//decryption of ID
	d, err := lib.Decode(m.ID)
	if err != nil {
		return m, lib.ErrEncodingFault
	}
	m.ID = string(d)
	//decryption of organizer
	d, err = lib.Decode(m.Organizer)
	if err != nil {
		return m, lib.ErrEncodingFault
	}
	m.Organizer = string(d)
	//decryption of witnesses public keys
	for i := 0; i < len(m.Witnesses); i++ {
		d, err = lib.Decode(m.Witnesses[i])
		if err != nil {
			return m, lib.ErrEncodingFault
		}
		m.Witnesses[i] = string(d)
	}
	return m, err
}

func ParseDataWitnessMessage(data json.RawMessage) (message.DataWitnessMessage, error) {
	m := message.DataWitnessMessage{}
	d, err := lib.Decode(string(data))
	if err != nil {
		fmt.Printf("error decoding the string : %v", err)
		return m, err
	}
	err = json.Unmarshal(d, &m)
	if err != nil {
		fmt.Printf("error unmarshalling the string : %v", err)
		return m, err
	}
	return m, err
}

func ParseDataCreateMeeting(data json.RawMessage) (message.DataCreateMeeting, error) {
	m := message.DataCreateMeeting{}
	d, err := lib.Decode(string(data))
	err = json.Unmarshal(d, &m)
	//decryption of ID
	d, err = lib.Decode(m.ID)
	if err != nil {
		return m, lib.ErrEncodingFault
	}
	m.ID = string(d)
	return m, err
}
func ParseDataCreateRollCall(data json.RawMessage) (message.DataCreateRollCall, error) {
	m := message.DataCreateRollCall{}
	d, err := lib.Decode(string(data))
	err = json.Unmarshal(d, &m)
	return m, err
}

func ParseDataCreatePoll(data json.RawMessage) (message.DataCreatePoll, error) {
	m := message.DataCreatePoll{}
	d, err := lib.Decode(string(data))
	err = json.Unmarshal(d, &m)
	return m, err
}
