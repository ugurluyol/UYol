package org.project.domain.communication.entities;

import org.project.domain.communication.enumerations.CallStatus;
import org.project.domain.communication.value_objects.CallID;
import org.project.domain.communication.value_objects.Participant;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.shared.value_objects.DriverID;

import java.time.LocalDateTime;

import static org.project.domain.shared.util.Utils.required;

public record Call(
        CallID callID,
        RideID rideID,
        Participant participant,
        DriverID driverID,
        Dates dates,
        CallStatus status) {

    public Call {
        required("callID", callID);
        required("rideID", rideID);
        required("participant", participant);
        required("driverID", driverID);
        required("dates", dates);
        required("status", status);
    }

    public static Call start(RideID rideID, Participant participant, DriverID driverID) {
        return new Call(CallID.newID(), rideID, participant, driverID, Dates.defaultDates(), CallStatus.ONGOING);
    }

    public Call end() {
        return new Call(callID, rideID, participant, driverID, dates.updated(), CallStatus.ENDED);
    }

    public Call cancel() {
        return new Call(callID, rideID, participant, driverID, dates.updated(), CallStatus.CANCELLED);
    }

    public Call missed() {
        return new Call(callID, rideID, participant, driverID, dates.updated(), CallStatus.MISSED);
    }
}
