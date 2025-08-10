package org.project.domain;

import org.junit.jupiter.api.Test;
import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.enumerations.RideStatus;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import org.project.features.TestDataGenerator;

import static org.junit.jupiter.api.Assertions.*;

class RideTest {
    
    @Test
    void validCreationOfRideWithoutDelivery() {
        assertDoesNotThrow(() -> assertNotNull(TestDataGenerator.rideWithoutDelivery()));
    }

    @Test
    void validCreationOfRideWithDelivery() {
        assertDoesNotThrow(() -> assertNotNull(TestDataGenerator.rideWithDelivery()));
    }

    @Test
    void shouldStartTheRoadSuccessfully() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();
        assertDoesNotThrow(ride::start);
        assertEquals(RideStatus.ON_THE_ROAD, ride.status());
    }

    @Test
    void invalidRideStartOnCanceledRide() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();
        ride.cancel();

        assertThrows(IllegalDomainArgumentException.class, ride::start, "Ride already started or canceled/finished");
    }

    @Test
    void invalidRideStartOnFinishedRide() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();
        ride.start();
        ride.finish();
        assertThrows(IllegalDomainArgumentException.class, ride::start, "Ride already started or canceled/finished");
    }

    @Test
    void shouldCancelRideSuccessfully() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();
        assertDoesNotThrow(ride::cancel);
        assertEquals(RideStatus.CANCELED, ride.status());
    }

    @Test
    void shouldNotAllowToCancelRideThatAlreadyOnTheRoad() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();
        ride.start();
        assertThrows(IllegalDomainArgumentException.class, ride::cancel,
                "Ride cancellation is not possible if it`s already on the road or finished");
    }

    @Test
    void shouldNotAllowToCancelRideThatFinished() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();
        ride.start();
        ride.finish();
        assertThrows(IllegalDomainArgumentException.class, ride::cancel,
                "Ride cancellation is not possible if it`s already on the road or finished");
    }

    @Test
    void shouldFinishRideSuccessfully() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();
        ride.start();
        assertDoesNotThrow(ride::finish);
        assertEquals(RideStatus.ENDED_SUCCESSFULLY, ride.status());
    }

    @Test
    void shouldNotFinishRideThatNotStarter() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();
        assertThrows(IllegalDomainArgumentException.class, ride::finish, "You can`t finish the ride which was not going");
    }

    @Test
    void shouldNotFinishRideThatAlreadyCanceled() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();
        ride.cancel();
        assertThrows(IllegalDomainArgumentException.class, ride::finish, "You can`t finish the ride which was not going");
    }
}
