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
import org.project.domain.ride.value_object.BookedSeats;
import org.project.domain.ride.value_object.PassengerSeat;
import org.project.domain.ride.value_object.RideOwner;
import org.project.domain.shared.value_objects.UserID;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;
import org.project.infrastructure.repository.JetDriverRepository;
import org.project.infrastructure.repository.JetOwnerRepository;
import org.project.infrastructure.repository.JetRideRepository;
import org.project.infrastructure.repository.JetUserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RideRepoTest {

    @Inject
    JetRideRepository rideRepo;

    @Inject
    JetUserRepository userRepo;

    @Inject
    JetOwnerRepository ownerRepo;

    @Inject
    JetDriverRepository driverRepo;

    @Test
    void save_ride() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var ride = Ride.of(new RideOwner(driver.id(), null),
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

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();
    }

    @Test
    void update_seats() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var ride = Ride.of(new RideOwner(driver.id(), null),
                TestDataGenerator.generateRoute(),
                TestDataGenerator.generateRideTime(),
                TestDataGenerator.generatePrice(),
                TestDataGenerator.generateSeatMap(),
                TestDataGenerator.generateRideDesc(),
                TestDataGenerator.generateRideRules());


        while (!ride.canAcceptPassenger()) {
            ride = Ride.of(new RideOwner(driver.id(), null),
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

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();

        List<Integer> availableSeats = new ArrayList<>();
        for (int i = 0; i < ride.seatMap().size(); i++) {
            if (ride.seatMap().isAvailable(i)) {
                availableSeats.add(i);
            }
        }

        var bookedSeats = new BookedSeats(availableSeats.stream().map(seat -> new PassengerSeat(seat, SeatStatus.MALE_OCCUPIED)).toList());
        ride.occupy(UserID.newID(), bookedSeats);

        var updateSeatsResult = rideRepo.updateSeats(ride);
        assertThat(updateSeatsResult.success()).isTrue();
        assertThat(updateSeatsResult.value()).isEqualTo(1);
    }

    @Test
    void update_ride_status_from_pending_to_on_the_road() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());
        var ride = Ride.of(new RideOwner(driver.id(), null),
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
        var ride = Ride.of(new RideOwner(driver.id(), null),
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
        var ride = Ride.of(new RideOwner(driver.id(), null),
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
        var ride = Ride.of(new RideOwner(driver.id(), null),
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
        var ride = Ride.of(new RideOwner(driver.id(), null),
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

        var rideSaveResult = rideRepo.save(ride);
        assertThat(rideSaveResult.success()).isTrue();

        var findResult = rideRepo.findBy(ride.id());
        assertThat(findResult.success()).isTrue();
    }

    @Test
    void find_by_user_id() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        for (int i = 0; i < 10; i++) {
            var ride = Ride.of(new RideOwner(driver.id(), null),
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
    void find_by_driver_id() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        for (int i = 0; i < 10; i++) {
            var ride = Ride.of(new RideOwner(driver.id(), null),
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
    void find_by_owner_id() {
        var ownerUser = TestDataGenerator.user();
        var owner = Owner.of(UserID.fromString(ownerUser.id().toString()), TestDataGenerator.voen());
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();

        var ownerUserSaveResult = userRepo.save(ownerUser);
        assertThat(ownerUserSaveResult.success()).isTrue();

        var ownerSaveResult = ownerRepo.save(owner);
        assertThat(ownerSaveResult.success()).isTrue();

        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        for (int i = 0; i < 10; i++) {
            var ride = Ride.of(new RideOwner(driver.id(), owner.id()),
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
    void find_by_date() {
        var driverUser = TestDataGenerator.user();
        var driver = Driver.of(UserID.fromString(driverUser.id().toString()), TestDataGenerator.driverLicense());

        var driverUserSaveResult = userRepo.save(driverUser);
        assertThat(driverUserSaveResult.success()).isTrue();


        var driverSaveResult = driverRepo.save(driver);
        assertThat(driverSaveResult.success()).isTrue();

        for (int i = 0; i < 10; i++) {
            var ride = Ride.of(new RideOwner(driver.id(), null),
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
}
