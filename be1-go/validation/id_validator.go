package validation

import (
	"bytes"
	"encoding/base64"
	"fmt"
	"student20_pop/message"

	"golang.org/x/xerrors"
)

type IdValidatior struct {
	// channelID corresponds to the channelID without the prefix /root/
	channelID string
}

func NewIdValidator(id string) *IdValidatior {
	return &IdValidatior{
		// Remove the prefix /root/
		channelID: id[:6],
	}
}

func (idValidator *IdValidatior) checkID(id []byte, strs ...fmt.Stringer) bool {
	hash, err := message.Hash(strs...)
	if err != nil {
		return false
	}
	return bytes.Equal(hash, id)
}

func (idValidator *IdValidatior) VerifyIDs(msg *message.Message) error {
	err := idValidator.verifyMessageID(msg)
	if err != nil {
		return message.NewError("failed to validate the ID of the message", err)
	}

	err = idValidator.verifyDataID(msg.Data)
	if err != nil {
		return message.NewError("failed to validate an ID of the data", err)
	}

	return nil
}

func (idValidator *IdValidatior) verifyMessageID(msg *message.Message) error {
	data := base64.URLEncoding.EncodeToString(msg.RawData)
	signature := base64.URLEncoding.EncodeToString(msg.Signature)

	ok := idValidator.checkID(msg.MessageID, message.Stringer(data), message.Stringer(signature))
	if !ok {
		return &message.Error{
			Code:        -4,
			Description: "The the field message_id of the message does not correspond to Hash(data||signature)",
		}
	}

	return nil
}

func (idValidator *IdValidatior) verifyDataID(data message.Data) error {

	var err error

	switch data.GetObject() {
	case message.DataObject(message.LaoObject):
		err = idValidator.verifyLaoID(data)
	case message.DataObject(message.MeetingObject):
		err = idValidator.verifyMeetingID(data)

	// No ID to check for MessageObject
	case message.DataObject(message.MessageObject):
	case message.DataObject(message.RollCallObject):
		err = idValidator.verifyRollCallID(data)
	case message.DataObject(message.ElectionObject):
		err = idValidator.verifyElectionID(data)
	}

	if err != nil {
		return &message.Error{
			Code:        -4,
			Description: fmt.Sprintf("An ID does not correspond to the protocol: %v", err),
		}
	}

	return nil
}

func (idValidator *IdValidatior) verifyLaoID(data message.Data) error {
	switch message.LaoDataAction(data.GetAction()) {
	case message.CreateLaoAction:
		laoData, ok := data.(*message.CreateLAOData)
		if !ok {
			return xerrors.Errorf("failed to cast data to CreateLAOData")
		}
		ok = idValidator.checkLaoID(laoData.Organizer, laoData.Creation, message.Stringer(laoData.Name), laoData.ID)
		if !ok {
			return xerrors.Errorf("The id of the create lao data does not correspond to SHA256(organizer||creation||name)")
		}

	// No ID to verify for UpdateLaoData
	case message.UpdateLaoAction:
	case message.StateLaoAction:
		laoData, ok := data.(*message.StateLAOData)
		if !ok {
			return xerrors.Errorf("failed to cast data to StateLAOData")
		}
		ok = idValidator.checkLaoID(laoData.Organizer, laoData.Creation, message.Stringer(laoData.Name), laoData.ID)
		if !ok {
			return xerrors.Errorf("The id of the state lao data does not correspond to SHA256(organizer||creation||name)")
		}
	}

	return nil
}

func (idValidator *IdValidatior) checkLaoID(str1, str2, str3 fmt.Stringer, id []byte) bool {
	return idValidator.checkID(id, str1, str2, str3)
}

func (idValidator *IdValidatior) verifyMeetingID(data message.Data) error {
	switch message.MeetingDataAction(data.GetAction()) {
	case message.CreateMeetingAction:
		meetingData, ok := data.(*message.CreateMeetingData)
		if !ok {
			return xerrors.Errorf("failed to cast data to CreateMeetingData")
		}
		ok = idValidator.checkMeetingID(meetingData.Creation, message.Stringer(meetingData.Name), meetingData.ID)
		if !ok {
			return xerrors.Errorf("The id of the create meeting data does not correspond to SHA256(lao_id||creation||name)")
		}

	case message.StateMeetingAction:
		meetingData, ok := data.(*message.StateMeetingData)
		if !ok {
			return xerrors.Errorf("failed to cast data to StateMeetingData")
		}
		ok = idValidator.checkMeetingID(meetingData.Creation, message.Stringer(meetingData.Name), meetingData.ID)
		if !ok {
			return xerrors.Errorf("The id of the state meeting data does not correspond to SHA256(lao_id||creation||name)")
		}
	}
	return nil
}

func (idValidator *IdValidatior) checkMeetingID(str1, str2 fmt.Stringer, id []byte) bool {
	return idValidator.checkID(id, message.Stringer(idValidator.channelID), str1, str2)
}

func (idValidator *IdValidatior) verifyRollCallID(data message.Data) error {
	switch message.RollCallAction(data.GetAction()) {
	case message.CreateRollCallAction:
		rollCallData, ok := data.(*message.CreateRollCallData)
		if !ok {
			return xerrors.Errorf("failed to cast data to CreateRollCallData")
		}
		ok = idValidator.checkRollCallID(rollCallData.Creation, message.Stringer(rollCallData.Name), rollCallData.ID)
		if !ok {
			return xerrors.Errorf("The id of the roll call does not correspond to SHA256(‘R’||lao_id||creation||name)")
		}
	case message.RollCallAction(message.ReopenRollCallAction), message.RollCallAction(message.ReopenRollCallAction):
		rollCallData, ok := data.(*message.OpenRollCallData)
		if !ok {
			return xerrors.Errorf("failed to cast data to OpenRollCallData")
		}
		opens := base64.URLEncoding.EncodeToString(rollCallData.Opens)
		ok = idValidator.checkRollCallID(message.Stringer(opens), rollCallData.OpenedAt, rollCallData.UpdateID)
		if !ok {
			return xerrors.Errorf("The id of the roll call does not correspond to SHA256(‘R’||lao_id||opens||opened_at)")
		}
	case message.CloseRollCallAction:
		rollCallData, ok := data.(*message.CloseRollCallData)
		if !ok {
			return xerrors.Errorf("failed to cast data to CloseRollCallData")
		}
		closes := base64.URLEncoding.EncodeToString(rollCallData.Closes)
		ok = idValidator.checkRollCallID(message.Stringer(closes), rollCallData.ClosedAt, rollCallData.UpdateID)
		if !ok {
			return xerrors.Errorf("The id of the roll call does not correspond to SHA256(‘R’||lao_id||closes||closed_at)")
		}
	}

	return nil
}

func (idValidator *IdValidatior) checkRollCallID(str1, str2 fmt.Stringer, id []byte) bool {
	return idValidator.checkID(id, message.Stringer('R'), message.Stringer(idValidator.channelID), str1, str2)
}

func (idValidator *IdValidatior) verifyElectionID(data message.Data) error {
	switch message.ElectionAction(data.GetAction()) {
	case message.ElectionSetupAction:
		return idValidator.verifyElectionSetupID(data)
	case message.CastVoteAction:
		return idValidator.verifyCastVoteID(data)
	}

	return nil
}

func (idValidator *IdValidatior) verifyElectionSetupID(data message.Data) error {
	electionData, ok := data.(*message.ElectionSetupData)
	if !ok {
		return xerrors.Errorf("failed to cast data to SetupElectionData")
	}

	// Check the id of the setup election message
	ok = idValidator.checkID(electionData.ID, message.Stringer("Election"), message.Stringer(idValidator.channelID), electionData.CreatedAt, message.Stringer(electionData.Name))
	if !ok {
		return xerrors.Errorf("The id of the setup election data does not correspond to SHA256('Election'||lao_id||created_at||name)")
	}

	// Check the id of each question
	for _, question := range electionData.Questions {
		ok = idValidator.checkID(question.ID, message.Stringer("Question"), message.Stringer(idValidator.channelID), message.Stringer(question.QuestionAsked))
		if !ok {
			return xerrors.Errorf("The id of question %s does not correspond to SHA256('Question'||election_id||question)", question.QuestionAsked)
		}
	}

	return nil
}

func (idValidator *IdValidatior) verifyCastVoteID(data message.Data) error {
	electionData, ok := data.(*message.CastVoteData)
	if !ok {
		return xerrors.Errorf("failed to cast data to CastVoteData")
	}

	// Check the id of each vote
	for _, vote := range electionData.Votes {
		questionID := base64.URLEncoding.EncodeToString(vote.QuestionID)
		stringers := []fmt.Stringer{message.Stringer("Vote"), message.Stringer(idValidator.channelID), message.Stringer(questionID)}

		// Add the indexes of each vote to stringers
		for _, index := range vote.VoteIndexes {
			stringers = append(stringers, message.Stringer(fmt.Sprintf("%v", index)))
		}

		ok = idValidator.checkID(vote.ID, stringers...)
		if !ok {
			return xerrors.Errorf("The id of the vote does not correspond to SHA256('Vote'||election_id||question_id||(vote_index(es)|write_in)")
		}
	}

	return nil
}
