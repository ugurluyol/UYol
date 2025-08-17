package org.project.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hadzhy.jetquerious.jdbc.JetQuerious;
import com.hadzhy.jetquerious.sql.Order;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.application.dto.ride.RideDTO;
import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.enumerations.RideRule;
import org.project.domain.ride.enumerations.RideStatus;
import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.ride.repositories.RideRepository;
import org.project.domain.ride.value_object.*;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.hadzhy.jetquerious.sql.QueryForge.*;
import static org.project.infrastructure.repository.JetOTPRepository.mapTransactionResult;

@ApplicationScoped
public class JetRideRepository implements RideRepository {

    private final JetQuerious jet;

    private final ObjectMapper objectMapper = new ObjectMapper();

    static final String RIDE = insert()
            .into("ride")
            .columns(
                    "id",
                    "driver_id",
                    "owner_id",
                    "from_location_desc",
                    "from_latitude",
                    "from_longitude",
                    "to_location_desc",
                    "to_latitude",
                    "to_longitude",
                    "start_time",
                    "end_time",
                    "price",
                    "seats",
                    "status",
                    "description",
                    "rules",
                    "creation_date",
                    "last_updated"
            )
            .values()
            .build()
            .sql();

    static final String UPDATE_SEATS = update("ride")
            .set("seats = ?, last_updated = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_STATUS = update("ride")
            .set("status = ?, last_updated = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_RULES = update("ride")
            .set("rules = ?, last_updated = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String FIND_BY_ID = select()
            .all()
            .from("ride")
            .where("id = ?")
            .build()
            .sql();

    static final String FIND_BY_USER_ID = select()
            .column("r.id").as("id")
            .column("r.driver_id").as("driver_id")
            .column("r.owner_id").as("owner_id")
            .column("r.from_location_desc").as("from_location_desc")
            .column("r.from_latitude").as("from_latitude")
            .column("r.from_longitude").as("from_longitude")
            .column("r.to_location_desc").as("to_location_desc")
            .column("r.to_latitude").as("to_latitude")
            .column("r.to_longitude").as("to_longitude")
            .column("r.start_time").as("start_time")
            .column("r.end_time").as("end_time")
            .column("r.price").as("price")
            .column("r.status").as("status")
            .from("ride r")
            .join("ride_contract rc", "r.id = rc.ride_id")
            .where("rc.user_id = ?")
            .orderBy("r.start_time", Order.DESC)
            .limitAndOffset()
            .sql();

    static final String FIND_BY_OWNER_ID = select()
            .column("id")
            .column("driver_id")
            .column("owner_id")
            .column("from_location_desc")
            .column("from_latitude")
            .column("from_longitude")
            .column("to_location_desc")
            .column("to_latitude")
            .column("to_longitude")
            .column("start_time")
            .column("end_time")
            .column("price")
            .column("status")
            .from("ride")
            .where("owner_id = ?")
            .limitAndOffset()
            .sql();

    static final String FIND_BY_DRIVER_ID = select()
            .column("id")
            .column("driver_id")
            .column("owner_id")
            .column("from_location_desc")
            .column("from_latitude")
            .column("from_longitude")
            .column("to_location_desc")
            .column("to_latitude")
            .column("to_longitude")
            .column("start_time")
            .column("end_time")
            .column("price")
            .column("status")
            .from("ride")
            .where("driver_id = ?")
            .limitAndOffset()
            .sql();

    static final String FIND_BY_DATE = select()
            .column("id")
            .column("driver_id")
            .column("owner_id")
            .column("from_location_desc")
            .column("from_latitude")
            .column("from_longitude")
            .column("to_location_desc")
            .column("to_latitude")
            .column("to_longitude")
            .column("start_time")
            .column("end_time")
            .column("price")
            .column("status")
            .from("ride")
            .where("CAST(start_time AS DATE) = CAST(? AS DATE)")
            .limitAndOffset()
            .sql();

    static final String ACTUAL_FOR = select()
            .column("id")
            .column("driver_id")
            .column("owner_id")
            .column("from_location_desc")
            .column("from_latitude")
            .column("from_longitude")
            .column("to_location_desc")
            .column("to_latitude")
            .column("to_longitude")
            .column("start_time")
            .column("end_time")
            .column("price")
            .column("status")
            .column("POWER(from_latitude - ?, 2) + POWER(from_longitude - ?, 2)").as("start_distance_sq")
            .column("POWER(to_latitude - ?, 2) + POWER(to_longitude - ?, 2)").as("end_distance_sq")
            .from("ride")
            .where("start_time >= ?")
            .and("POWER(from_latitude - ?, 2) + POWER(from_longitude - ?, 2) <= ?")
            .and("POWER(to_latitude - ?, 2) + POWER(to_longitude - ?, 2) <= ?")
            .and("status = 'PENDING'")
            .orderBy("start_time ASC")
            .limitAndOffset()
            .sql();

    JetRideRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public Result<Integer, Throwable> save(Ride ride) {
        String seats;
        String rules;
        try {
            seats = objectMapper.writeValueAsString(ride.seatMap().seats());
            rules = objectMapper.writeValueAsString(ride.rideRules());
        } catch (JsonProcessingException e) {
            return Result.failure(e);
        }

        Location from = ride.route().from();
        Location to = ride.route().to();

        return mapTransactionResult(jet.write(RIDE,
                ride.id(),
                ride.rideOwner().driverID(),
                ride.rideOwner().ownerID(),
                from.description(),
                from.latitude(),
                from.longitude(),
                to.description(),
                to.latitude(),
                to.longitude(),
                ride.rideTime().startOfTheTrip(),
                ride.rideTime().endOfTheTrip(),
                ride.price(),
                seats,
                ride.status(),
                ride.rideDesc(),
                rules,
                ride.dates().createdAt(),
                ride.dates().lastUpdated()));
    }

    @Override
    public Result<Integer, Throwable> updateSeats(Ride ride) {
        String seats;
        try {
            seats = objectMapper.writeValueAsString(ride.seatMap().seats());
        } catch (JsonProcessingException e) {
            return Result.failure(e);
        }

        return mapTransactionResult(jet.write(UPDATE_SEATS, seats, ride.dates().lastUpdated(), ride.id()));
    }

    @Override
    public Result<Integer, Throwable> updateStatus(Ride ride) {
        return mapTransactionResult(jet.write(UPDATE_STATUS, ride.status(), ride.dates().lastUpdated(), ride.id()));
    }

    @Override
    public Result<Integer, Throwable> updateRules(Ride ride) {
        String rules;
        try {
            rules = objectMapper.writeValueAsString(ride.rideRules());
        } catch (JsonProcessingException e) {
            return Result.failure(e);
        }

        return mapTransactionResult(jet.write(UPDATE_RULES, rules, ride.dates().lastUpdated(), ride.id()));
    }

    @Override
    public Result<Ride, Throwable> findBy(RideID rideID) {
        return mapRideResult(jet.read(FIND_BY_ID, this::mapRide, rideID));
    }

    @Override
    public Result<List<RideDTO>, Throwable> pageOf(UserID userID, Pageable page) {
        return mapPageRideResult(
                jet.readListOf(FIND_BY_USER_ID, this::mapRideDTO, userID, page.limit(), page.offset())
        );
    }

    @Override
    public Result<List<RideDTO>, Throwable> pageOf(OwnerID ownerID, Pageable page) {
        return mapPageRideResult(
                jet.readListOf(FIND_BY_OWNER_ID, this::mapRideDTO, ownerID, page.limit(), page.offset())
        );
    }

    @Override
    public Result<List<RideDTO>, Throwable> pageOf(DriverID driverID, Pageable page) {
        return mapPageRideResult(
                jet.readListOf(FIND_BY_DRIVER_ID, this::mapRideDTO, driverID, page.limit(), page.offset())
        );
    }

    @Override
    public Result<List<RideDTO>, Throwable> pageOf(LocalDate localDate, Pageable page) {
        return mapPageRideResult(
                jet.readListOf(FIND_BY_DATE, this::mapRideDTO, localDate, page.limit(), page.offset())
        );
    }

    @Override
    public Result<List<RideDTO>, Throwable> actualFor(Location startPoint, Location destination, Pageable page) {
        return mapPageRideResult(
                jet.readListOf(ACTUAL_FOR, this::mapRideDTO,
                        startPoint.latitude(), startPoint.longitude(),
                        destination.latitude(), destination.longitude(),
                        LocalDate.now().plus(Duration.ofMinutes(1)),
                        startPoint.latitude(), startPoint.longitude(),
                        destination.latitude(), destination.longitude(),
                        page.limit(), page.offset())
        );
    }

    private Ride mapRide(ResultSet rs) throws SQLException {
        try {
            SeatStatus[][] seatMatrix = objectMapper.readValue(
                    rs.getString("seats"),
                    SeatStatus[][].class
            );

            Set<RideRule> rules = objectMapper.readValue(
                    rs.getString("rules"),
                    new TypeReference<>() {}
            );

            String ownerID = rs.getString("owner_id");
            RideOwner rideOwner = new RideOwner(
                    DriverID.fromString(rs.getString("driver_id")),
                    ownerID == null ? null : OwnerID.fromString(ownerID)
            );

            Route route = new Route(
                    new Location(rs.getString("from_location_desc"), rs.getDouble("from_latitude"), rs.getDouble("from_longitude")),
                    new Location(rs.getString("to_location_desc"), rs.getDouble("to_latitude"), rs.getDouble("to_longitude"))
            );

            RideTime rideTime = new RideTime(
                    rs.getTimestamp("start_time").toLocalDateTime(),
                    rs.getTimestamp("end_time").toLocalDateTime()
            );

            Dates dates = new Dates(
                    rs.getTimestamp("creation_date").toLocalDateTime(),
                    rs.getTimestamp("last_updated").toLocalDateTime()
            );

            return Ride.fromRepository(
                    RideID.fromString(rs.getString("id")),
                    rideOwner,
                    route,
                    rideTime,
                    new Price(rs.getBigDecimal("price")),
                    new SeatMap(seatMatrix),
                    RideStatus.valueOf(rs.getString("status")),
                    new RideDesc(rs.getString("description")),
                    rules,
                    dates
            );
        } catch (JsonProcessingException e) {
            throw new SQLException(e);
        }
    }

    private RideDTO mapRideDTO(ResultSet rs) throws SQLException {
        return new RideDTO(
                rs.getString("id"),
                rs.getString("driver_id"),
                rs.getString("owner_id"),
                rs.getString("from_location_desc"),
                rs.getDouble("from_latitude"),
                rs.getDouble("from_longitude"),
                rs.getString("to_location_desc"),
                rs.getDouble("to_latitude"),
                rs.getDouble("to_longitude"),
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime(),
                rs.getBigDecimal("price"),
                RideStatus.valueOf(rs.getString("status"))
        );
    }

    private Result<Ride, Throwable> mapRideResult(com.hadzhy.jetquerious.util.Result<Ride, Throwable> read) {
        return new Result<>(read.value(), read.throwable(), read.success());
    }

    private Result<List<RideDTO>, Throwable> mapPageRideResult(com.hadzhy.jetquerious.util.Result<List<RideDTO>, Throwable> read) {
        return new Result<>(read.value(), read.throwable(), read.success());
    }
}
