package org.project.infrastructure.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import com.hadzhy.jetquerious.sql.QueryForge;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.domain.communication.entities.ActiveConversation;
import org.project.domain.communication.entities.BlockedConversation;
import org.project.domain.communication.entities.ClosedConversation;
import org.project.domain.communication.entities.Conversation;
import org.project.domain.communication.enumerations.ConversationStatus;
import org.project.domain.communication.repositories.ConversationRepository;
import org.project.domain.communication.value_objects.ConversationID;
import org.project.domain.communication.value_objects.Participant;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.shared.value_objects.OwnerID;
import org.project.domain.shared.value_objects.Pageable;
import org.project.domain.shared.value_objects.UserID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;
import static org.project.infrastructure.repository.JetOTPRepository.mapTransactionResult;

@ApplicationScoped
public class JetConversationRepository implements ConversationRepository {

    private final JetQuerious jet;

    public static final String CONVERSATION = insert()
            .into("conversation")
            .column("id")
            .column("ride_id")
            .column("participant")
            .column("participant_type")
            .column("driver_id")
            .column("created_at")
            .column("updated_at")
            .column("status")
            .values()
            .build()
            .sql();

    public static final String UPDATE_STATUS = QueryForge.update("conversation")
            .set("status = ?, updated_at = ?")
            .where("id = ?")
            .build()
            .sql();

    public static final String CONVERSATION_BY_ID = select()
            .all()
            .from("conversation")
            .where("id = ?")
            .build()
            .sql();

    public static final String RIDE_CONVERSATIONS = select()
            .all()
            .from("conversation")
            .where("ride_id = ?")
            .limitAndOffset()
            .sql();

    public JetConversationRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public Result<Integer, Throwable> save(Conversation conversation) {
        return mapTransactionResult(jet.write(CONVERSATION,
                conversation.conversationID(),
                conversation.rideID(),
                conversation.participant(),
                conversation.participant().type(),
                conversation.driverID(),
                conversation.dates().createdAt(),
                conversation.dates().lastUpdated(),
                conversation.status()
        ));
    }

    @Override
    public Result<Integer, Throwable> update(Conversation conversation) {
        return mapTransactionResult(jet.write(UPDATE_STATUS,
                conversation.status(),
                conversation.dates().lastUpdated(),
                conversation.conversationID()
        ));
    }

    @Override
    public Result<Conversation, Throwable> findBy(ConversationID conversationId) {
        return mapResult(jet.read(CONVERSATION_BY_ID, this::mapConversation, conversationId));
    }

    @Override
    public Result<List<Conversation>, Throwable> findBy(RideID rideID, Pageable pageable) {
        return mapListResult(jet.readListOf(RIDE_CONVERSATIONS, this::mapConversation, pageable.limit(), pageable.offset()));
    }

    private Conversation mapConversation(ResultSet rs) throws SQLException {
        ConversationStatus status = ConversationStatus.valueOf(rs.getString("status"));
        Participant.Type participantType = Participant.Type.valueOf(rs.getString("participant_type"));

        return switch (status) {
            case ACTIVE -> new ActiveConversation(
                    ConversationID.from(rs.getString("id")),
                    RideID.fromString(rs.getString("ride_id")),
                    participant(participantType, rs),
                    DriverID.fromString(rs.getString("driver_id")),
                    dates(rs));
            case CLOSED -> new ClosedConversation(
                    ConversationID.from(rs.getString("id")),
                    RideID.fromString(rs.getString("ride_id")),
                    participant(participantType, rs),
                    DriverID.fromString(rs.getString("driver_id")),
                    dates(rs));
            case BLOCK -> new BlockedConversation(
                    ConversationID.from(rs.getString("id")),
                    RideID.fromString(rs.getString("ride_id")),
                    participant(participantType, rs),
                    DriverID.fromString(rs.getString("driver_id")),
                    dates(rs));
        };
    }

    private Result<Conversation, Throwable> mapResult(
            com.hadzhy.jetquerious.util.Result<Conversation, Throwable> res) {
        return new Result<>(res.value(), res.throwable(), res.success());
    }

    private Result<List<Conversation>, Throwable> mapListResult(
            com.hadzhy.jetquerious.util.Result<List<Conversation>, Throwable> res) {
        return new Result<>(res.value(), res.throwable(), res.success());
    }

    private Dates dates(ResultSet rs) throws SQLException {
        return new Dates(rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }

    private Participant participant(Participant.Type participantType, ResultSet rs) throws SQLException {
        UUID participant = UUID.fromString(rs.getString("participant"));
        return switch (participantType) {
            case USER -> new Participant.UserParticipant(new UserID(participant));
            case OWNER -> new Participant.OwnerParticipant(new OwnerID(participant));
        };
    }
}
