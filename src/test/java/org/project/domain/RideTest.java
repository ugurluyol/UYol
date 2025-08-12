package org.project.domain;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.enumerations.RideStatus;
import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.ride.value_object.BookedSeats;
import org.project.domain.ride.value_object.PassengerSeat;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import org.project.features.TestDataGenerator;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

    @Test
    void shouldSuccessfullyOccupy() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();
        int index = generateIndex(ride);
        SeatStatus seatStatus = getRandomNonDriverOccupiedStatus();

        assertDoesNotThrow(() -> ride.occupy(getBookedSeats(index, seatStatus)));
    }

    @Test
    void shouldNotAllowChangingPassengerOnNonPendingRide() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();
        ride.start();
        int index = generateIndex(ride);
        SeatStatus seatStatus = getRandomNonDriverOccupiedStatus();

        IllegalDomainArgumentException e =
                assertThrows(IllegalDomainArgumentException.class, () ->
                        ride.occupy(getBookedSeats(index, seatStatus)));
        assertEquals("Cannot add passenger when ride is already on the road", e.getMessage());
    }

    @Test
    void shouldNotAllowToOccupyOnNonOccupiedOne() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();
        int index = generateIndex(ride);
        SeatStatus seatStatus = SeatStatus.DRIVER;
        SeatStatus seatStatus1 = SeatStatus.EMPTY;

        IllegalDomainArgumentException e =
                assertThrows(IllegalDomainArgumentException.class, () ->
                        ride.occupy(getBookedSeats(index, seatStatus)));
        assertEquals("Seat must be occupied with valid occupant", e.getMessage());

        IllegalDomainArgumentException e1 =
                assertThrows(IllegalDomainArgumentException.class, () ->
                        ride.occupy(getBookedSeats(index, seatStatus1)));
        assertEquals("Seat must be occupied with valid occupant", e1.getMessage());
    }

    private static @NotNull BookedSeats getBookedSeats(int index, SeatStatus seatStatus) {
        return new BookedSeats(List.of(new PassengerSeat(index, seatStatus)));
    }

    private static SeatStatus getRandomNonDriverOccupiedStatus() {
        while (true) {
            SeatStatus randomNonDriverStatus = TestDataGenerator.getRandomNonDriverStatus(TestDataGenerator.getNonDriverStatuses());
            if (randomNonDriverStatus.isOccupied()) return randomNonDriverStatus;
        }
    }

    private int generateIndex(Ride ride) {
        while (true) {
            int i = ThreadLocalRandom.current().nextInt(1, ride.seatMap().size());
            if (ride.seatMap().status(i) == SeatStatus.EMPTY) return i;
        }
    }

    @Test
    void shouldSuccessfullyEnableDelivery() {
        Ride ride = TestDataGenerator.rideWithoutDelivery();

        assertDoesNotThrow(() -> ride.enableDelivery(TestDataGenerator.generatePrice()));
        assertTrue(ride.isDeliveryAvailable());
    }
}
