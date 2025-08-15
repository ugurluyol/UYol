package org.project.infrastructure.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hadzhy.jetquerious.jdbc.JetQuerious;
import org.project.domain.ride.entities.RideContract;
import org.project.domain.ride.repositories.RideContractRepository;
import org.project.domain.ride.value_object.*;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.Pageable;
import org.project.domain.shared.value_objects.UserID;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;
import static org.project.infrastructure.repository.JetOTPRepository.mapTransactionResult;

public class JetRideContractRepository implements RideContractRepository {

    private final JetQuerious jet;

    private final ObjectMapper objectMapper = new ObjectMapper();

    static final String RIDE_CONTRACT = insert()
            .into("ride_contract")
            .column("id")
            .column("user_id")
            .column("ride_id")
            .column("price_per_seat")
            .column("booked_seats")
            .values()
            .build()
            .sql();

    static final String FIND_BY_ID = select()
            .all()
            .from("ride_contract")
            .where("id = ?")
            .build()
            .sql();

    static final String FIND_BY_RIDE_ID = select()
            .all()
            .from("ride_contract")
            .where("ride_id = ?")
            .limitAndOffset()
            .sql();

    JetRideContractRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public Result<Integer, Throwable> save(RideContract rideContract) {
        String bookedSeats;
        try {
            bookedSeats = objectMapper.writeValueAsString(rideContract.bookedSeats().bookedSeats());
        } catch (JsonProcessingException e) {
            return Result.failure(e);
        }

        return mapTransactionResult(jet.write(RIDE_CONTRACT,
                rideContract.id(),
                rideContract.userID(),
                rideContract.rideID(),
                rideContract.pricePerSeat(),
                bookedSeats
        ));
    }

    @Override
    public Result<RideContract, Throwable> findBy(RideContractID rideContractID) {
        return mapResult(jet.read(FIND_BY_ID, this::mapRideContract, rideContractID));
    }

    @Override
    public Result<List<RideContract>, Throwable> findBy(RideID rideID, Pageable page) {
        return mapPageResult(jet.readListOf(FIND_BY_RIDE_ID, this::mapRideContract, rideID, page.limit(), page.offset()));
    }

    private RideContract mapRideContract(ResultSet rs) throws SQLException {
        try {
            List<PassengerSeat> bookedSeats = objectMapper.readValue(
                    rs.getString("booked_seats"),
                    new TypeReference<>() {}
            );

            return RideContract.fromRepository(
                    RideContractID.fromString(rs.getString("id")),
                    UserID.fromString(rs.getString("user_id")),
                    RideID.fromString(rs.getString("ride_id")),
                    new Price(rs.getBigDecimal("price_per_seat")),
                    new BookedSeats(bookedSeats)
            );
        } catch (JsonProcessingException e) {
            throw new SQLException(e);
        }
    }

    private static Result<RideContract, Throwable> mapResult(com.hadzhy.jetquerious.util.Result<RideContract, Throwable> res) {
        return new Result<>(res.value(), res.throwable(), res.success());
    }

    private Result<List<RideContract>, Throwable> mapPageResult(com.hadzhy.jetquerious.util.Result<List<RideContract>, Throwable> read) {
        return new Result<>(read.value(), read.throwable(), read.success());
    }
}
