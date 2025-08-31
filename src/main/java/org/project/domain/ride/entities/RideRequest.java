package org.project.domain.ride.entities;

import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.value_objects.LicensePlate;
import org.project.domain.ride.enumerations.RideRule;
import org.project.domain.ride.value_object.*;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.shared.value_objects.OwnerID;

import java.time.LocalDateTime;
import java.util.Set;

import static org.project.domain.ride.entities.Ride.MAX_RIDE_RULES;
import static org.project.domain.shared.util.Utils.required;

public record RideRequest(
        RideRequestID id,
        DriverID driverID,
        OwnerID ownerID,
        LicensePlate licensePlate,
        Route route,
        RideTime rideTime,
        Price price,
        SeatMap seatMap,
        RideDesc rideDesc,
        Set<RideRule> rideRules,
        LocalDateTime createdAt
) {

    public RideRequest {
        required("id", id);
        required("driverID", driverID);
        required("ownerID", ownerID);
        required("licensePlate", licensePlate);
        required("route", route);
        required("rideTime", rideTime);
        required("price", price);
        required("seatMap", seatMap);
        required("rideRules", rideRules);
        required("createdAt", createdAt);

        if (rideRules.size() > MAX_RIDE_RULES)
            throw new IllegalDomainArgumentException("Too many ride rules");
    }
}
