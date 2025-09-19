package org.project.domain.communication.entities;

import org.project.domain.communication.value_objects.ConversationID;
import org.project.domain.communication.value_objects.MessageContent;
import org.project.domain.communication.value_objects.MessageID;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.shared.value_objects.UserID;

import static org.project.domain.shared.util.Utils.required;

public record Message(
        MessageID messageID,
        ConversationID conversationID,
        UserID sender,
        MessageContent content,
        Dates dates,
        boolean edited) {

    public Message {
        required("messageID", messageID);
        required("conversationID", conversationID);
        required("sender", sender);
        required("content", content);
        required("dates", dates);
    }

    static Message create(ConversationID convId, UserID sender, MessageContent content) {
        return new Message(MessageID.newID(), convId, sender, content, Dates.defaultDates(), false);
    }

    Message edit(MessageContent newContent) {
        return new Message(messageID, conversationID, sender, newContent, dates.updated(), true);
    }
}
