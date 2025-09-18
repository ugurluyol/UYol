package org.project.domain.communication.repositories;

import org.project.domain.communication.entities.Message;
import org.project.domain.communication.value_objects.ConversationID;
import org.project.domain.communication.value_objects.MessageID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.Pageable;

import java.util.List;

public interface MessageRepository {

    Result<Integer, Throwable> save(Message message);

    Result<Integer, Throwable> update(Message message);

    Result<Message, Throwable> findBy(MessageID messageId);

    Result<List<Message>, Throwable> findBy(ConversationID conversationId, Pageable pageable);
}
