
package org.project.infrastructure.repository;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.repositories.OwnerRepository;
import org.project.domain.fleet.value_objects.UserID;
import org.project.domain.fleet.value_objects.Voen;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.OwnerID;

import com.hadzhy.jetquerious.jdbc.JetQuerious;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JetOwnerRepository implements OwnerRepository {

	private final JetQuerious jet;

	static final String SAVE_OWNER = insert().into("owner").columns("id", "user_id", "voen", "created_at").values()
			.build().sql();

	static final String OWNER_BY_ID = select().all().from("owner").where("id = ?").build().sql();

	static final String OWNER_BY_USER_ID = select().column("id").from("owner").where("user_id = ?").build().sql();

	public JetOwnerRepository() {
		this.jet = JetQuerious.instance();
	}

	@Override
	public Result<Integer, Throwable> save(Owner owner) {
		return mapTransactionResult(
				jet.write(SAVE_OWNER, owner.id(), owner.userID(), owner.voen().value(), owner.createdAt()));
	}

	@Override
	public Result<Owner, Throwable> findBy(OwnerID ownerID) {
		var result = jet.read(OWNER_BY_ID, this::ownerMapper, ownerID);
		return new Result<>(result.value(), result.throwable(), result.success());
	}

	@Override
	public Result<UserID, Throwable> findBy(UserID userID) {
		var result = jet.read(OWNER_BY_USER_ID, this::userIdMapper, userID);
		return new Result<>(result.value(), result.throwable(), result.success());
	}

	private Owner ownerMapper(ResultSet rs) throws SQLException {
		return Owner.fromRepository(new OwnerID(UUID.fromString(rs.getString("id"))),
				new UserID(UUID.fromString(rs.getString("user_id"))), new Voen(rs.getString("voen")),
				rs.getObject("created_at", LocalDateTime.class));
	}

	private UserID userIdMapper(ResultSet rs) throws SQLException {
		return new UserID(UUID.fromString(rs.getString("id")));
	}

	private static Result<Integer, Throwable> mapTransactionResult(
			com.hadzhy.jetquerious.util.Result<Integer, Throwable> result) {
		return new Result<>(result.value(), result.throwable(), result.success());
	}
}
