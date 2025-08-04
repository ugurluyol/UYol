package org.project.infrastructure.repository;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;
import static com.hadzhy.jetquerious.sql.QueryForge.update;
import static org.project.infrastructure.repository.JetOTPRepository.mapTransactionResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import org.project.domain.shared.containers.Result;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.user.value_objects.Email;
import org.project.domain.user.value_objects.KeyAndCounter;
import org.project.domain.user.value_objects.PersonalData;
import org.project.domain.user.value_objects.Phone;
import org.project.domain.user.value_objects.RefreshToken;
import org.project.domain.user.value_objects.Identifier;

import com.hadzhy.jetquerious.jdbc.JetQuerious;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JetUserRepository implements UserRepository {

    private final JetQuerious jet;

    static final String SAVE_USER = insert()
            .into("user_account")
            .column("id")
            .column("firstname")
            .column("surname")
            .column("phone")
            .column("email")
            .column("password")
            .column("birth_date")
            .column("is_verified")
            .column("is_banned")
            .column("is_2fa_enabled")
            .column("secret_key")
            .column("counter")
            .column("creation_date")
            .column("last_updated")
            .values()
            .build()
            .sql();

    static final String SAVE_REFRESH_TOKEN = insert()
            .into("refresh_token")
            .columns("user_id", "token")
            .values()
            .onConflict("user_id")
            .doUpdateSet("token = ?")
            .build()
            .sql();

    static final String UPDATE_PHONE = update("user_account")
            .set("phone = ?, last_updated = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_COUNTER = update("user_account")
            .set("counter = ?, last_updated = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_VERIFICATION = update("user_account")
            .set("is_verified = ?, last_updated = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_2FA = update("user_account")
            .set("is_2FA_enabled = ?, last_updated = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_BAN = update("user_account")
            .set("is_banned = ?, last_updated = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_PASSWORD = update("user_account")
            .set("password = ?, last_updated = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String IS_EMAIL_EXISTS = select()
            .count("email")
            .from("user_account")
            .where("email = ?")
            .build()
            .sql();

    static final String IS_PHONE_EXISTS = select()
            .count("phone")
            .from("user_account")
            .where("phone = ?")
            .build()
            .sql();

    static final String USER_BY_ID = select()
            .all()
            .from("user_account")
            .where("id = ?")
            .build()
            .sql();

    static final String USER_BY_EMAIL = select()
            .all()
            .from("user_account")
            .where("email = ?")
            .build()
            .sql();

    static final String USER_BY_PHONE = select()
            .all()
            .from("user_account")
            .where("phone = ?")
            .build()
            .sql();

    static final String REFRESH_TOKEN = select()
            .all()
            .from("refresh_token")
            .where("token = ?")
            .build()
            .sql();

    JetUserRepository() {
        jet = JetQuerious.instance();
    }

    @Override
    public Result<Integer, Throwable> save(User user) {
        PersonalData personalData = user.personalData();
        return mapTransactionResult(jet.write(SAVE_USER,
                user.id().toString(),
                personalData.firstname(),
                personalData.surname(),
                personalData.phone().orElse(null),
                personalData.email().orElse(null),
                personalData.password().orElse(null),
                personalData.birthDate(),
                user.isVerified(),
                user.isBanned(),
                user.is2FAEnabled(),
                user.keyAndCounter().key(),
                user.keyAndCounter().counter(),
                user.accountDates().createdAt(),
                user.accountDates().lastUpdated()));
    }

    @Override
    public Result<Integer, Throwable> saveRefreshToken(RefreshToken refreshToken) {
        return mapTransactionResult(jet.write(SAVE_REFRESH_TOKEN,
                refreshToken.userID().toString(),
                refreshToken.refreshToken(),
                refreshToken.refreshToken()));
    }

    @Override
    public Result<Integer, Throwable> updatePhone(User user) {
        return mapTransactionResult(
                jet.write(UPDATE_PHONE,
                        user.personalData().phone().orElseThrow(),
                        user.accountDates().lastUpdated(),
                        user.id().toString()));
    }

    @Override
    public Result<Integer, Throwable> updateCounter(User user) {
        return mapTransactionResult(jet.write(UPDATE_COUNTER,
                user.keyAndCounter().counter(),
                user.accountDates().lastUpdated(),
                user.id().toString()));
    }

    @Override
    public Result<Integer, Throwable> updateVerification(User user) {
        return mapTransactionResult(jet.write(UPDATE_VERIFICATION,
                user.isVerified(),
                user.accountDates().lastUpdated(),
                user.id().toString()));
    }

    @Override
    public Result<Integer, Throwable> updateBan(User user) {
        return mapTransactionResult(
                jet.write(UPDATE_BAN, user.isBanned(), user.accountDates().lastUpdated(), user.id()));
    }

    @Override
    public Result<Integer, Throwable> update2FA(User user) {
        return mapTransactionResult(jet.write(UPDATE_2FA, user.is2FAEnabled(), user.accountDates().lastUpdated(), user.id().toString()));
    }

    @Override
    public Result<Integer, Throwable> updatePassword(User user) {
        return mapTransactionResult(jet.write(UPDATE_PASSWORD,
                user.personalData().password().orElse(null),
                user.accountDates().lastUpdated(),
                user.id()));
    }

    @Override
    public boolean isEmailExists(Email email) {
        return jet.readObjectOf(IS_EMAIL_EXISTS, Integer.class, email.email())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking email existence.");
                    return false;
                });
    }

    @Override
    public boolean isPhoneExists(Phone phone) {
        return jet.readObjectOf(IS_PHONE_EXISTS, Integer.class, phone.phoneNumber())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking phone existence");
                    return false;
                });
    }

    @Override
    public Result<User, Throwable> findBy(UUID id) {
        var result = jet.read(USER_BY_ID, this::userMapper, id.toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<User, Throwable> findBy(Email email) {
        var result = jet.read(USER_BY_EMAIL, this::userMapper, email.email());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<User, Throwable> findBy(Phone phone) {
        var result = jet.read(USER_BY_PHONE, this::userMapper, phone.phoneNumber());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<User, Throwable> findBy(Identifier identifier) {
        return switch (identifier) {
            case Email e -> findBy(e);
            case Phone p -> findBy(p);
        };
    }

    @Override
    public Result<RefreshToken, Throwable> findRefreshToken(String refreshToken) {
        var result = jet.read(REFRESH_TOKEN, this::refreshTokenMapper, refreshToken);
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    private RefreshToken refreshTokenMapper(ResultSet rs) throws SQLException {
        return new RefreshToken(UUID.fromString(rs.getString("user_id")), rs.getString("token"));
    }

    private User userMapper(ResultSet rs) throws SQLException {
        PersonalData personalData = new PersonalData(
                rs.getString("firstname"),
                rs.getString("surname"),
                rs.getString("phone"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getObject("birth_date", Timestamp.class)
                        .toLocalDateTime()
                        .toLocalDate());

        return User.fromRepository(
                UUID.fromString(rs.getString("id")),
                personalData,
                rs.getBoolean("is_verified"),
                rs.getBoolean("is_banned"),
                new KeyAndCounter(rs.getString("secret_key"), rs.getInt("counter")),
                new Dates(rs.getObject("creation_date", Timestamp.class).toLocalDateTime(),
                        rs.getObject("last_updated", Timestamp.class).toLocalDateTime()),
                rs.getBoolean("is_2fa_enabled"));
    }
}
