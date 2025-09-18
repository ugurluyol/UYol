package org.project.domain.communication.entities;

public sealed interface Conversation permits ActiveConversation, ClosedConversation, BlockedConversation {
}
