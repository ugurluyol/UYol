package org.project.domain.ride.value_object;

import org.project.domain.shared.annotations.Nullable;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.shared.value_objects.OwnerID;

import java.util.Objects;
import java.util.Optional;

import static org.project.domain.shared.util.Utils.required;

public final class RideOwner {
    private final DriverID driverID;
    private final @Nullable OwnerID ownerID;

    public RideOwner(DriverID driverID, @Nullable OwnerID ownerID) {
        required("driverID", driverID);
        this.driverID = driverID;
        this.ownerID = ownerID;
    }

    public DriverID driverID() {
        return driverID;
    }

    public Optional<OwnerID> ownerID() {
        return Optional.ofNullable(ownerID);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RideOwner) obj;
        return Objects.equals(this.driverID, that.driverID) &&
                Objects.equals(this.ownerID, that.ownerID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverID, ownerID);
    }

    @Override
    public String toString() {
        return "RideOwner[" +
                "driverID=" + driverID + ", " +
                "ownerID=" + ownerID + ']';
    }

}
