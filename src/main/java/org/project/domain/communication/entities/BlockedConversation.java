package org.project.domain.communication.entities;

import org.project.domain.communication.enumerations.ConversationStatus;
import org.project.domain.communication.value_objects.ConversationID;
import org.project.domain.communication.value_objects.Participant;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.shared.value_objects.DriverID;

import static org.project.domain.shared.util.Utils.required;

public record BlockedConversation(
        ConversationID conversationID,
        RideID rideID,
        Participant participant,
        DriverID driverID,
        Dates dates) implements Conversation {

    public BlockedConversation {
        required("conversationID", conversationID);
        required("rideID", rideID);
        required("participant", participant);
        required("driverID", driverID);
        required("dates", dates);
    }

    public ConversationStatus status() {
        return ConversationStatus.BLOCK;
    }
}
