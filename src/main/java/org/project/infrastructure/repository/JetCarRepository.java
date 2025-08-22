package org.project.infrastructure.repository;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.project.application.dto.fleet.CarDTO;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.fleet.value_objects.CarBrand;
import org.project.domain.fleet.value_objects.CarColor;
import org.project.domain.shared.value_objects.CarID;
import org.project.domain.fleet.value_objects.CarModel;
import org.project.domain.fleet.value_objects.CarYear;
import org.project.domain.fleet.value_objects.LicensePlate;
import org.project.domain.fleet.value_objects.SeatCount;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.Pageable;

import com.hadzhy.jetquerious.jdbc.JetQuerious;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JetCarRepository implements CarRepository {

	private final JetQuerious jet;

	static final String SAVE_CAR = insert().into("car").columns("id", "owner", "license_plate", "car_brand",
			"car_model", "car_color", "car_year", "seat_count", "created_at").values().build().sql();

	static final String CAR_BY_ID = select().all().from("car").where("id = ?").build().sql();

	static final String CAR_BY_LICENSE_PLATE = select().all().from("car").where("license_plate = ?").build().sql();

	static final String PAGE_OF_CARS = select()
			.column("license_plate")
			.column("car_brand")
			.column("car_model")
			.column("car_color")
			.column("car_year")
			.column("seat_count")
			.from("car")
			.where("owner = ?")
			.orderBy("created_at DESC")
			.limitAndOffset().sql();

	static final String IS_LICENSE_PLATE_EXISTS = select()
			.count("license_plate")
			.from("car")
			.where("license_plate = ?")
			.build().sql();

	JetCarRepository() {
		jet = JetQuerious.instance();
	}

	@Override
	public Result<Integer, Throwable> save(Car car) {

		return mapTransactionResult(
				jet.write(SAVE_CAR, car.id().value(), car.owner().value(), car.licensePlate().value(),
						car.carBrand().value(), car.carModel().value(), car.carColor().value(), car.carYear().value(),
						car.seatCount().value(), 
						car.createdAt()));
	}

	static Result<Integer, Throwable> mapTransactionResult(
			com.hadzhy.jetquerious.util.Result<Integer, Throwable> result) {
		return new Result<>(result.value(), result.throwable(), result.success());
	}

	@Override
	public Result<Car, Throwable> findBy(CarID carID) {
		var result = jet.read(CAR_BY_ID, this::carMapper, carID.value());
		return new Result<>(result.value(), result.throwable(), result.success());
	}

    @Override
    public Result<Car, Throwable> findBy(LicensePlate licensePlate) {
		var result = jet.read(CAR_BY_LICENSE_PLATE, this::carMapper, licensePlate.value());
		return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
	public Result<List<CarDTO>, Throwable> pageOf(Pageable pageable, UserID userID) {
		var listOf = jet.readListOf(PAGE_OF_CARS, this::carDTOMapper, userID.value(), pageable.limit(), pageable.offset());
		return new Result<>(listOf.value(), listOf.throwable(), listOf.success());
	}

	@Override
	public boolean isLicenseTemplateExists(LicensePlate license) {
		return jet.readObjectOf(IS_LICENSE_PLATE_EXISTS, Integer.class, license)
				.mapSuccess(count -> count != null && count > 0)
				.orElse(false);
	}


	private Car carMapper(ResultSet rs) throws SQLException {
		return Car.fromRepository(new CarID(UUID.fromString(rs.getString("id"))),
				new UserID(UUID.fromString(rs.getString("owner"))), new LicensePlate(rs.getString("license_plate")),
				new CarBrand(rs.getString("car_brand")), new CarModel(rs.getString("car_model")),
				new CarColor(rs.getString("car_color")), new CarYear(rs.getInt("car_year")),
				new SeatCount(rs.getInt("seat_count")), rs.getObject("created_at", Timestamp.class).toLocalDateTime());
	}

	private CarDTO carDTOMapper(ResultSet rs) throws SQLException {
		return new CarDTO(rs.getString("license_plate"),
				rs.getString("car_brand"),
				rs.getString("car_model"),
				rs.getString("car_color"),
				rs.getInt("car_year"),
				rs.getInt("seat_count"));
	}
}
