package hub

import (
	"student20_pop/message"
)

func (c *laoChannel) processCreateRollCall(data message.Data) error {

	rollCallData := data.(*message.CreateRollCallData)

	// Check that the ProposedEnd is greater than the ProposedStart
	if rollCallData.ProposedStart > rollCallData.ProposedEnd {
		return &message.Error{
			Code:        -4,
			Description: "The field `proposed_start` is greater than the field `proposed_end`",
		}
	}

	c.rollCall.id = string(rollCallData.ID)
	c.rollCall.state = Created
	return nil
}

func (c *laoChannel) processOpenRollCall(data message.Data, action message.RollCallAction) error {
	if action == message.RollCallAction(message.OpenRollCallAction) {
		// If the action is an OpenRollCallAction,
		// the previous roll call action should be a CreateRollCallAction
		if c.rollCall.state != Created {
			return &message.Error{
				Code:        -1,
				Description: "The roll call can not be opened since it has not been created previously",
			}
		}
	} else {
		// If the action is an RepenRollCallAction,
		// the previous roll call action should be a CloseRollCallAction
		if c.rollCall.state != Closed {
			return &message.Error{
				Code:        -1,
				Description: "The roll call can not be reopened since it has not been closed previously",
			}
		}
	}

	rollCallData := data.(*message.OpenRollCallData)

	if !c.rollCall.checkPrevID(rollCallData.Opens) {
		return &message.Error{
			Code:        -4,
			Description: "The field `opens` does not correspond to the id of the previous roll call message",
		}
	}

	c.rollCall.id = string(rollCallData.UpdateID)
	c.rollCall.state = Open
	return nil
}

func (c *laoChannel) processCloseRollCall(data message.Data) error {
	if c.rollCall.state != Open {
		return &message.Error{
			Code:        -1,
			Description: "The roll call can not be closed since it is not open",
		}
	}

	rollCallData := data.(*message.CloseRollCallData)
	if !c.rollCall.checkPrevID(rollCallData.Closes) {
		return &message.Error{
			Code:        -4,
			Description: "The field `closes` does not correspond to the id of the previous roll call message",
		}
	}

	c.rollCall.id = string(rollCallData.UpdateID)
	c.rollCall.state = Closed

	for _, attendee := range rollCallData.Attendees {
		c.attendees.Add(attendee.String())
	}

	return nil
}

// Helper functions

func (r *rollCall) checkPrevID(prevID []byte) bool {
	return string(prevID) == r.id
}
