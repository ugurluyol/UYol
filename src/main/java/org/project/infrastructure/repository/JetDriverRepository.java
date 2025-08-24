package org.project.infrastructure.repository;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;
import static com.hadzhy.jetquerious.sql.QueryForge.update;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.enumerations.DriverStatus;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.value_objects.*;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.shared.value_objects.DriverID;

import com.hadzhy.jetquerious.jdbc.JetQuerious;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JetDriverRepository implements DriverRepository {

	private final JetQuerious jet;

	static final String SAVE_DRIVER = insert()
			.into("driver")
			.columns(
					"id",
					"user_id",
					"driver_license",
					"status",
					"rides",
					"total_reviews",
					"sum_of_scores",
					"created_at",
					"last_updated"
			)
			.values()
			.build()
			.sql();

	static final String UPDATE_LICENSE = update("driver")
			.set("driver_license = ?, last_updated = ?")
			.where("id = ?")
			.build()
			.sql();

	static final String UPDATE_STATUS = update("driver")
			.set("status = ?, last_updated = ?")
			.where("id = ?")
			.build()
			.sql();

	static final String UPDATE_RIDES = update("driver")
			.set("rides = ?, last_updated = ?")
			.where("id = ?")
			.build()
			.sql();

	static final String UPDATE_RATING = update("driver")
			.set("total_reviews = ?, sum_of_scores = ?, last_updated = ?")
			.where("id = ?")
			.build()
			.sql();

	static final String FIND_BY_ID = select()
			.all()
			.from("driver")
			.where("id = ?")
			.build()
			.sql();

	static final String FIND_BY_USER_ID = select()
			.all()
			.from("driver")
			.where("user_id = ?")
			.build()
			.sql();

	static final String IS_LICENSE_EXISTS = select()
			.count("driver_license")
			.from("driver")
			.where("driver_license = ?")
			.build()
			.sql();

	static final String IS_DRIVER_EXISTS = select().count("user_id")
			.from("driver").where("user_id = ?").build().sql();

	JetDriverRepository() {
		this.jet = JetQuerious.instance();
	}

	@Override
	public Result<Integer, Throwable> save(Driver driver) {
		int totalReviews;
		int sumOfScores;
		switch (driver.rating()) {
			case NoRating ignored -> { totalReviews = 0; sumOfScores = 0; }
            case Rated rated -> { totalReviews = rated.totalReviews(); sumOfScores = rated.sumOfScores(); }
        }

		return mapTransactionResult(jet.write(SAVE_DRIVER,
				driver.id(),
				driver.userID(),
				driver.license(),
				driver.status(),
				driver.rides(),
				totalReviews,
				sumOfScores,
				driver.dates().createdAt(),
				driver.dates().lastUpdated()
		));
	}

	@Override
	public Result<Integer, Throwable> updateLicense(Driver driver) {
		return mapTransactionResult(
				jet.write(UPDATE_LICENSE, driver.license().licenseNumber(), driver.dates().lastUpdated(), driver.id())
		);
	}

	@Override
	public Result<Integer, Throwable> updateStatus(Driver driver) {
		return mapTransactionResult(
				jet.write(UPDATE_STATUS, driver.status(), driver.dates().lastUpdated(), driver.id())
		);
	}

	@Override
	public Result<Integer, Throwable> updateRides(Driver driver) {
		return mapTransactionResult(
				jet.write(UPDATE_RIDES, driver.rides(), driver.dates().lastUpdated(), driver.id())
		);
	}

	@Override
	public Result<Integer, Throwable> updateRating(Driver driver) {
		int totalReviews;
		int sumOfScores;
		switch (driver.rating()) {
			case NoRating ignored -> { totalReviews = 0; sumOfScores = 0; }
			case Rated rated -> { totalReviews = rated.totalReviews(); sumOfScores = rated.sumOfScores(); }
		}

		return mapTransactionResult(
				jet.write(UPDATE_RATING, totalReviews, sumOfScores, driver.dates().lastUpdated(), driver.id())
		);
	}

	@Override
	public Result<Driver, Throwable> findBy(DriverID driverID) {
		var result = jet.read(FIND_BY_ID, this::driverMapper, driverID);
		return new Result<>(result.value(), result.throwable(), result.success());
	}

	@Override
	public Result<Driver, Throwable> findBy(UserID userID) {
		var result = jet.read(FIND_BY_USER_ID, this::driverMapper, userID);
		return new Result<>(result.value(), result.throwable(), result.success());
	}

	@Override
	public boolean isLicenseExists(DriverLicense license) {
		return jet.readObjectOf(IS_LICENSE_EXISTS, Integer.class, license)
				.mapSuccess(count -> count != null && count > 0)
				.orElse(false);
	}

	@Override
	public boolean isDriverExists(UserID userID) {
		return jet.readObjectOf(IS_DRIVER_EXISTS, Integer.class, userID)
				.mapSuccess(count -> count != null && count > 0)
				.orElse(false);
	}

	private Driver driverMapper(ResultSet rs) throws SQLException {
		return Driver.fromRepository(
				new DriverID(UUID.fromString(rs.getString("id"))),
				new UserID(UUID.fromString(rs.getString("user_id"))),
				new DriverLicense(rs.getString("driver_license")),
				new Dates(rs.getObject("created_at", LocalDateTime.class), rs.getObject("last_updated", LocalDateTime.class)),
				DriverStatus.valueOf(rs.getString("status")),
				new TotalRides(rs.getInt("rides")),
				extractDriverRating(rs)
		);
	}

	private static DriverRating extractDriverRating(ResultSet rs) throws SQLException {
		long totalReviews = rs.getLong("total_reviews");
		long sumOfScores = rs.getLong("sum_of_scores");

		if (rs.wasNull() || totalReviews == 0) return new NoRating();
		return new Rated((int) totalReviews, (int) sumOfScores);
	}

	private static Result<Integer, Throwable> mapTransactionResult(
			com.hadzhy.jetquerious.util.Result<Integer, Throwable> result) {
		return new Result<>(result.value(), result.throwable(), result.success());
	}
}
