package org.project.infrastructure.repository;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;
import static com.hadzhy.jetquerious.sql.QueryForge.update;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.value_objects.DriverLicense;
import org.project.domain.fleet.value_objects.UserID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.shared.value_objects.DriverID;

import com.hadzhy.jetquerious.jdbc.JetQuerious;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JetDriverRepository implements DriverRepository {

	private final JetQuerious jet;

	static final String SAVE_DRIVER = insert().into("driver")
			.columns("id", "user_id", "license_number", "created_at", "last_updated").values().build().sql();

	static final String UPDATE_LICENSE = update("driver").set("license_number = ?,last_updated = ?").where("id = ?")
			.build().sql();

	static final String FIND_BY_ID = select().all().from("driver").where("id = ?").build().sql();

	static final String FIND_BY_USER_ID = select().all().from("driver").where("user_id = ?").build().sql();

	public JetDriverRepository() {
		this.jet = JetQuerious.instance();
	}

	@Override
	public Result<Integer, Throwable> save(Driver driver) {
		return mapTransactionResult(jet.write(SAVE_DRIVER, driver.id().toString(), driver.userID().toString(),
				driver.license().licenseNumber(), driver.dates().createdAt(), driver.dates().lastUpdated()));
	}

	@Override
	public Result<Integer, Throwable> updateLicense(Driver driver) {
		return mapTransactionResult(jet.write(UPDATE_LICENSE, driver.license().licenseNumber(),
				driver.dates().lastUpdated(), driver.id().toString()));
	}

	@Override
	public Result<Driver, Throwable> findBy(DriverID driverID) {
		var result = jet.read(FIND_BY_ID, this::driverMapper, driverID.toString());
		return new Result<>(result.value(), result.throwable(), result.success());
	}

	@Override
	public Result<Driver, Throwable> findBy(UserID userID) {
		var result = jet.read(FIND_BY_USER_ID, this::driverMapper, userID.toString());
		return new Result<>(result.value(), result.throwable(), result.success());
	}

	private Driver driverMapper(ResultSet rs) throws SQLException {
		return Driver.fromRepository(new DriverID(UUID.fromString(rs.getString("id"))),
				new UserID(UUID.fromString(rs.getString("user_id"))), new DriverLicense(rs.getString("license_number")),
				new Dates(rs.getObject("created_at", LocalDateTime.class),
						rs.getObject("last_updated", LocalDateTime.class)));
	}

	private static Result<Integer, Throwable> mapTransactionResult(
			com.hadzhy.jetquerious.util.Result<Integer, Throwable> result) {
		return new Result<>(result.value(), result.throwable(), result.success());
	}
}
