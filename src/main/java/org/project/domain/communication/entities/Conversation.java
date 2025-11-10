package org.project.domain.communication.entities;

import org.project.domain.communication.enumerations.ConversationStatus;
import org.project.domain.communication.value_objects.ConversationID;
import org.project.domain.communication.value_objects.Participant;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.shared.value_objects.DriverID;

public sealed interface Conversation permits ActiveConversation, ClosedConversation, BlockedConversation {
    ConversationID conversationID();
    RideID rideID();
    Participant participant();
    DriverID driverID();
    Dates dates();
    ConversationStatus status();
}
