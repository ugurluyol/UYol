package org.project.domain.communication.entities;

import org.project.domain.communication.enumerations.ConversationStatus;
import org.project.domain.communication.value_objects.ConversationID;
import org.project.domain.communication.value_objects.MessageContent;
import org.project.domain.communication.value_objects.Participant;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.shared.value_objects.DriverID;

import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public record ActiveConversation(
        ConversationID conversationID,
        RideID rideID,
        Participant participant,
        DriverID driverID,
        Dates dates) implements Conversation {

    public ActiveConversation {
        required("conversationID", conversationID);
        required("rideID", rideID);
        required("participant", participant);
        required("driverID", driverID);
        required("dates", ActiveConversation.this.dates);
    }

    public static ActiveConversation create(RideID rideID, Participant participant, DriverID driverID) {
        return new ActiveConversation(new ConversationID(UUID.randomUUID()), rideID, participant, driverID, Dates.defaultDates());
    }

    public ConversationStatus status() {
        return ConversationStatus.ACTIVE;
    }

    public Message participantWrites(MessageContent message) {
        return Message.create(conversationID, participant.userID(), message);
    }

    public Message driverWrites(MessageContent message) {
        return Message.create(conversationID, driverID.toUserID(), message);
    }

    public Message edit(Message message, MessageContent newMessage) {
        required("message", message);
        if (!message.conversationID().equals(conversationID))
            throw new IllegalDomainArgumentException("This particular message do not belong to this conversation");

        return message.edit(newMessage);
    }

    public BlockedConversation block() {
        return new BlockedConversation(conversationID, rideID, participant, driverID, dates.updated());
    }

    public ClosedConversation close() {
        return new ClosedConversation(conversationID, rideID, participant, driverID, dates.updated());
    }
}
