package org.project.domain.ride.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hadzhy.jetquerious.jdbc.JetQuerious;
import org.project.domain.ride.entities.RideContract;
import org.project.domain.ride.value_object.Price;
import org.project.domain.ride.value_object.RideContractID;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.Pageable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;

public class JetRideContractRepository implements RideContractRepository {

    private final JetQuerious jet;

    private final ObjectMapper objectMapper = new ObjectMapper();

    static final String RIDE_CONTRACT = insert()
            .into("ride_contract")
            .column("id")
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
        return null;
    }

    @Override
    public Result<RideContract, Throwable> findBy(RideContractID rideContractID) {
        return null;
    }

    @Override
    public Result<List<RideContract>, Throwable> findBy(RideID rideID, Pageable page) {
        return null;
    }

    private RideContract mapRideContract(ResultSet rs) throws SQLException {
        try {
            return RideContract.fromRepository(
                    RideContractID.fromString(rs.getString("id")),
                    RideID.fromString(rs.getString("ride_id")),
                    new Price(rs.getBigDecimal("price_per_seat")),
                    );
        } catch (JsonProcessingException e) {

        }
    }
}
