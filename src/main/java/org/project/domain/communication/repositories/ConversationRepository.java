package org.project.domain.communication.repositories;

import org.project.domain.communication.entities.Conversation;
import org.project.domain.communication.value_objects.ConversationID;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.Pageable;

import java.util.List;

public interface ConversationRepository {

    Result<Integer, Throwable> save(Conversation conversation);

    Result<Integer, Throwable> update(Conversation conversation);

    Result<Conversation, Throwable> findBy(ConversationID conversationId);

    Result<List<Conversation>, Throwable> findBy(RideID rideID, Pageable pageable);
}
