package org.project.infrastructure.repository;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.project.application.pagination.PageRequest;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.enumerations.RideRule;
import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.ride.value_object.*;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.shared.value_objects.OwnerID;
import org.project.domain.shared.value_objects.UserID;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RideRepoTest {

    @Inject
    JetRideRepository rideRepo;

    @Inject
    JetUserRepository userRepo;

    @Inject
    JetOwnerRepository ownerRepo;

    @Inject
    JetDriverRepository driverRepo;

    @Inject
    JetCarRepository carRepo;

    @Test
    void save_user() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules());

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();
    }

    @Test
    void fail_saving_ride_without_driver() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules());

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isFalse();
    }

    @Test
    void fail_saving_without_driver_user() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules());

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isFalse();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isFalse();

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isFalse();
    }

    @Test
    void update_seats() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules());


        while (!ride.canAcceptPassenger()) {
            ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                    TestDataGenerator.generateRoute(),
                    TestDataGenerator.generateRideTime(),
                    TestDataGenerator.generatePrice(),
                    TestDataGenerator.generateSeatMap(),
                    TestDataGenerator.generateRideDesc(),
                    TestDataGenerator.generateRideRules());
        }

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();

        List<Integer> availableSeats = new ArrayList<>();
        for (int i = 0; i < ride.seatMap().size(); i++) {
            if (ride.seatMap().isAvailable(i)) {
                availableSeats.add(i);
            }
        }

        var bookedSeats = new BookedSeats(availableSeats.stream().map(seat -> new PassengerSeat(seat, SeatStatus.MALE_OCCUPIED)).toList());
        ride.book(UserID.newID(), bookedSeats);

        var updateSeatsResult = rideRepo.updateSeats(ride);
        assertThat(updateSeatsResult.success()).isTrue();
        assertThat(updateSeatsResult.value()).isEqualTo(1);
    }

    @Test
    void update_ride_status_from_pending_to_on_the_road() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules());

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();

        ride.start();

        var statusResult = rideRepo.updateStatus(ride);
        assertThat(statusResult.success()).isTrue();
    }

    @Test
    void update_ride_status_from_pending_to_cancelled() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules());

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();

        ride.cancel();

        var statusResult = rideRepo.updateStatus(ride);
        assertThat(statusResult.success()).isTrue();
    }

    @Test
    void update_ride_status_from_on_the_road_to_finished() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules());

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();

        ride.start();

        var startStatusResult = rideRepo.updateStatus(ride);
        assertThat(startStatusResult.success()).isTrue();

        ride.finish();

        var finishStatusResult = rideRepo.updateStatus(ride);
        assertThat(finishStatusResult.success()).isTrue();
    }

    @Test
    void update_ride_rules() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                new HashSet<>());

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();

        ride.addRideRule(RideRule.LIMITED_LUGGAGE);

        var rideRuleResult = rideRepo.updateRules(ride);
        assertThat(rideRuleResult.success()).isTrue();
    }

    @Test
    void find_by_ride_id() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules());

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();

        var findResult = rideRepo.findBy(ride.id());
        assertThat(findResult.success()).isTrue();
    }

    @Test
    void find_by_invalid_ride_id() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules());

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();

        var findResult = rideRepo.findBy(RideID.newID());
        assertThat(findResult.success()).isFalse();
    }

    @Test
    void find_by_user_id() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        for (int i = 0; i < 10; i++) {
            var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                    TestDataGenerator.generateRoute(),
                    TestDataGenerator.generateRideTime(),
                    TestDataGenerator.generatePrice(),
                    TestDataGenerator.generateSeatMap(),
                    TestDataGenerator.generateRideDesc(),
                    TestDataGenerator.generateRideRules());
            var rideSaveResult = rideRepo.save(ride);
            assertThat(rideSaveResult.success()).isTrue();
        }

        var findByOwnerIdResult = rideRepo.pageOf(driver.userID(), new PageRequest(5, 0));
        assertThat(findByOwnerIdResult.success()).isTrue();
    }

    @Test
    void find_by_invalid_user_id() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        for (int i = 0; i < 10; i++) {
            var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                    TestDataGenerator.generateRoute(),
                    TestDataGenerator.generateRideTime(),
                    TestDataGenerator.generatePrice(),
                    TestDataGenerator.generateSeatMap(),
                    TestDataGenerator.generateRideDesc(),
                    TestDataGenerator.generateRideRules());
            var rideSaveResult = rideRepo.save(ride);
            assertThat(rideSaveResult.success()).isTrue();
        }

        var findByUserIdResult = rideRepo.pageOf(UserID.newID(), new PageRequest(5, 0));
        if (findByUserIdResult.success()) {
            assertThat(findByUserIdResult.value().isEmpty()).isTrue();
        }
    }

    @Test
    void find_by_driver_id() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        for (int i = 0; i < 10; i++) {
            var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                    TestDataGenerator.generateRoute(),
                    TestDataGenerator.generateRideTime(),
                    TestDataGenerator.generatePrice(),
                    TestDataGenerator.generateSeatMap(),
                    TestDataGenerator.generateRideDesc(),
                    TestDataGenerator.generateRideRules());
            var rideSaveResult = rideRepo.save(ride);
            assertThat(rideSaveResult.success()).isTrue();
        }

        var findByOwnerIdResult = rideRepo.pageOf(driver.id(), new PageRequest(5, 0));
        assertThat(findByOwnerIdResult.success()).isTrue();
    }

    @Test
    void find_by_invalid_driver_id() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        for (int i = 0; i < 10; i++) {
            var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                    TestDataGenerator.generateRoute(),
                    TestDataGenerator.generateRideTime(),
                    TestDataGenerator.generatePrice(),
                    TestDataGenerator.generateSeatMap(),
                    TestDataGenerator.generateRideDesc(),
                    TestDataGenerator.generateRideRules());
            var rideSaveResult = rideRepo.save(ride);
            assertThat(rideSaveResult.success()).isTrue();
        }

        var findByDriverIdResult = rideRepo.pageOf(DriverID.newID(), new PageRequest(5, 0));

        if (findByDriverIdResult.success()) {
            assertThat(findByDriverIdResult.value().isEmpty()).isTrue();
        }
    }

    @Test
    void find_by_owner_id() {
        var ownerUser = TestDataGenerator.user();
        var owner = Owner.of(UserID.fromString(ownerUser.id().toString()), TestDataGenerator.voen());
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var ownerUserSaveResult = userRepo.save(ownerUser);
        assertThat(ownerUserSaveResult.success()).isTrue();

        var ownerSaveResult = ownerRepo.save(owner);
        assertThat(ownerSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        for (int i = 0; i < 10; i++) {
            var ride = Ride.of(car.id(), new RideOwner(driver.id(), owner.id()),
                    TestDataGenerator.generateRoute(),
                    TestDataGenerator.generateRideTime(),
                    TestDataGenerator.generatePrice(),
                    TestDataGenerator.generateSeatMap(),
                    TestDataGenerator.generateRideDesc(),
                    TestDataGenerator.generateRideRules());
            var rideSaveResult = rideRepo.save(ride);
            assertThat(rideSaveResult.success()).isTrue();
        }

        var findByOwnerIdResult = rideRepo.pageOf(owner.id(), new PageRequest(5, 0));
        assertThat(findByOwnerIdResult.success()).isTrue();
    }

    @Test
    void find_by_invalid_owner_id() {
        var ownerUser = TestDataGenerator.user();
        var owner = Owner.of(UserID.fromString(ownerUser.id().toString()), TestDataGenerator.voen());
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var ownerUserSaveResult = userRepo.save(ownerUser);
        assertThat(ownerUserSaveResult.success()).isTrue();

        var ownerSaveResult = ownerRepo.save(owner);
        assertThat(ownerSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        for (int i = 0; i < 10; i++) {
            var ride = Ride.of(car.id(), new RideOwner(driver.id(), owner.id()),
                    TestDataGenerator.generateRoute(),
                    TestDataGenerator.generateRideTime(),
                    TestDataGenerator.generatePrice(),
                    TestDataGenerator.generateSeatMap(),
                    TestDataGenerator.generateRideDesc(),
                    TestDataGenerator.generateRideRules());
            var rideSaveResult = rideRepo.save(ride);
            assertThat(rideSaveResult.success()).isTrue();
        }

        var findByOwnerIdResult = rideRepo.pageOf(OwnerID.newID(), new PageRequest(5, 0));
        if (findByOwnerIdResult.success()) {
            assertThat(findByOwnerIdResult.value().isEmpty()).isTrue();
        }
    }

    @Test
    void find_by_date() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        for (int i = 0; i < 10; i++) {
            var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                    TestDataGenerator.generateRoute(),
                    TestDataGenerator.generateRideTime(),
                    TestDataGenerator.generatePrice(),
                    TestDataGenerator.generateSeatMap(),
                    TestDataGenerator.generateRideDesc(),
                    TestDataGenerator.generateRideRules());
            var rideSaveResult = rideRepo.save(ride);
            assertThat(rideSaveResult.success()).isTrue();
        }

        var findByOwnerIdResult = rideRepo.pageOf(LocalDate.now(), new PageRequest(5, 0));
        assertThat(findByOwnerIdResult.success()).isTrue();
    }

    @Test
    void find_by_invalid_date() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        for (int i = 0; i < 10; i++) {
            var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                    TestDataGenerator.generateRoute(),
                    TestDataGenerator.generateRideTime(),
                    TestDataGenerator.generatePrice(),
                    TestDataGenerator.generateSeatMap(),
                    TestDataGenerator.generateRideDesc(),
                    TestDataGenerator.generateRideRules());
            var rideSaveResult = rideRepo.save(ride);
            assertThat(rideSaveResult.success()).isTrue();
        }

        var findByDateResult = rideRepo.pageOf(LocalDate.now().minusDays(5000), new PageRequest(5, 0));
        if (findByDateResult.success()) {
            assertThat(findByDateResult.value().isEmpty()).isTrue();
        }
    }

    @Test
    void find_by_location() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        var rideRoute = new Route(TestDataGenerator.generateLocation(), TestDataGenerator.generateLocation());
        var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                rideRoute,
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules());

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();

        var findByLocation = rideRepo.actualFor(ride.route().from(), ride.route().to(), ride.dates().createdAt().toLocalDate(), new PageRequest(5, 0));
        assertThat(findByLocation.success()).isTrue();
    }

    @Test
    void fail_find_by_non_existent_location() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var car = TestDataGenerator.car(new UserID(driverUser.id()));

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        var carSaveResult = carRepo.save(car);
        assertThat(carSaveResult.success()).isTrue();

        var rideRoute = new Route(TestDataGenerator.generateLocation(), TestDataGenerator.generateLocation());
        var ride = Ride.of(car.id(), new RideOwner(driver.id(), null),
                rideRoute,
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules());

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();

        var findByLocationResult = rideRepo.actualFor(TestDataGenerator.generateLocation(), TestDataGenerator.generateLocation(), ride.dates().createdAt().toLocalDate(), new PageRequest(5, 0));
        if (findByLocationResult.success()) {
            assertThat(findByLocationResult.value().isEmpty()).isTrue();
        }
    }
}
