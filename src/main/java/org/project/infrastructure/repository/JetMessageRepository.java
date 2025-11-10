package org.project.infrastructure.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import com.hadzhy.jetquerious.sql.QueryForge;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.domain.communication.entities.Message;
import org.project.domain.communication.repositories.MessageRepository;
import org.project.domain.communication.value_objects.ConversationID;
import org.project.domain.communication.value_objects.MessageContent;
import org.project.domain.communication.value_objects.MessageID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.shared.value_objects.Pageable;
import org.project.domain.shared.value_objects.UserID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;
import static org.project.infrastructure.repository.JetOTPRepository.mapTransactionResult;

@ApplicationScoped
public class JetMessageRepository implements MessageRepository {

    private final JetQuerious jet;

    public static final String MESSAGE = insert()
            .into("message")
            .column("id")
            .column("conversation_id")
            .column("sender_id")
            .column("content")
            .column("creation_date")
            .column("last_updated")
            .column("edited")
            .values()
            .build()
            .sql();

    public static final String UPDATE_MESSAGE = QueryForge.update("message")
            .set("content = ?, edited = ?")
            .where("id = ?")
            .build()
            .sql();

    public static final String MESSAGE_BY_ID = select()
            .all()
            .from("message")
            .where("id = ?")
            .build()
            .sql();

    public static final String CONVERSATION_MESSAGES = select()
            .all()
            .from("message")
            .where("conversation_id = ?")
            .limitAndOffset()
            .sql();

    JetMessageRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public Result<Integer, Throwable> save(Message message) {
        return mapTransactionResult(jet.write(MESSAGE,
                message.messageID(),
                message.conversationID(),
                message.sender(),
                message.content(),
                message.dates().createdAt(),
                message.dates().lastUpdated(),
                message.edited()
        ));
    }

    @Override
    public Result<Integer, Throwable> update(Message message) {
        return mapTransactionResult(jet.write(UPDATE_MESSAGE,
                message.content(),
                message.edited(),
                message.messageID()
        ));
    }

    @Override
    public Result<Message, Throwable> findBy(MessageID messageId) {
        return mapResult(jet.read(MESSAGE_BY_ID, this::mapMessage, messageId));
    }

    @Override
    public Result<List<Message>, Throwable> findBy(ConversationID conversationId, Pageable pageable) {
        return mapListResult(jet.readListOf(CONVERSATION_MESSAGES, this::mapMessage, conversationId, pageable.limit(), pageable.offset()));
    }

    private Message mapMessage(ResultSet rs) throws SQLException {
        return new Message(
                MessageID.from(rs.getString("id")),
                ConversationID.from(rs.getString("conversation_id")),
                UserID.fromString(rs.getString("sender_id")),
                new MessageContent(rs.getString("content")),
                dates(rs),
                rs.getBoolean("edited")
        );
    }

    private Result<Message, Throwable> mapResult(
            com.hadzhy.jetquerious.util.Result<Message, Throwable> result) {
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    private Result<List<Message>, Throwable> mapListResult(
            com.hadzhy.jetquerious.util.Result<List<Message>, Throwable> res) {
        return new Result<>(res.value(), res.throwable(), res.success());
    }

    private Dates dates(ResultSet rs) throws SQLException {
        return new Dates(rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }
}
