package org.project.infrastructure.repository;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.fleet.value_objects.CarBrand;
import org.project.domain.fleet.value_objects.CarColor;
import org.project.domain.fleet.value_objects.CarID;
import org.project.domain.fleet.value_objects.CarModel;
import org.project.domain.fleet.value_objects.CarYear;
import org.project.domain.fleet.value_objects.LicensePlate;
import org.project.domain.fleet.value_objects.SeatCount;
import org.project.domain.fleet.value_objects.UserID;
import org.project.domain.shared.containers.Result;

import com.hadzhy.jetquerious.jdbc.JetQuerious;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JetCarRepository implements CarRepository {

	private final JetQuerious jet;

	static final String SAVE_CAR = insert().into("car")
			.columns("id", "owner_id", "license_plate", "brand", "model", "color", "year", "seat_count", "created_at")
			.values().build().sql();

	static final String CAR_BY_ID = select().all().from("car").where("id = ?").build().sql();

	static final String PAGE_OF_CARS = select().all().from("car").where("owner_id = ?").orderBy("created_at DESC")
			.limitAndOffset().sql();

	JetCarRepository() {
		jet = JetQuerious.instance();
	}

	@Override
	public Result<Integer, Throwable> save(Car car) {
		return mapTransactionResult(jet.write(SAVE_CAR, car.id(), car.owner(),
				car.licensePlate().toString(), car.carBrand().toString(), car.carModel().toString(),
				car.carColor().toString(), car.carYear().value(), car.seatCount().value(), car.createdAt()));
	}

	static Result<Integer, Throwable> mapTransactionResult(
			com.hadzhy.jetquerious.util.Result<Integer, Throwable> result) {
		return new Result<>(result.value(), result.throwable(), result.success());
	}

	@Override
	public Result<Car, Throwable> findBy(CarID carID) {
		var result = jet.read(CAR_BY_ID, this::carMapper, carID);
		return new Result<>(result.value(), result.throwable(), result.success());
	}

	@Override
	public Result<List<Car>, Throwable> pageOf(org.project.domain.shared.value_objects.Pageable pageable,
			UserID userID) {
		var listOf = jet.readListOf(PAGE_OF_CARS, this::carMapper, userID, pageable.limit(), pageable.offset());
		return new Result<>(listOf.value(), listOf.throwable(), listOf.success());
	}

	private Car carMapper(ResultSet rs) throws SQLException {
		return Car.fromRepository(new CarID(UUID.fromString(rs.getString("id"))),
				new UserID(UUID.fromString(rs.getString("owner_id"))), new LicensePlate(rs.getString("license_plate")),
				new CarBrand(rs.getString("brand")), new CarModel(rs.getString("model")),
				new CarColor(rs.getString("color")), new CarYear(rs.getInt("year")),
				new SeatCount(rs.getInt("seat_count")), rs.getObject("created_at", Timestamp.class).toLocalDateTime());
	}
}
