package org.project.domain.ride.value_object;

import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.shared.value_objects.OwnerID;

import static org.project.domain.shared.util.Utils.required;

public record RideOwner(DriverID driverID, OwnerID ownerID) {
    public RideOwner {
        required("driverID", driverID);
        required("ownerID", ownerID);
    }
}
