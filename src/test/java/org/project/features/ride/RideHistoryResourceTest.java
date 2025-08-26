package org.project.features.ride;

import static io.restassured.RestAssured.given;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.repositories.OwnerRepository;
import org.project.domain.fleet.value_objects.DriverLicense;
import org.project.domain.fleet.value_objects.Voen;
import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.enumerations.RideRule;
import org.project.domain.ride.repositories.RideRepository;
import org.project.domain.ride.value_object.Location;
import org.project.domain.ride.value_object.Price;
import org.project.domain.ride.value_object.RideDesc;
import org.project.domain.ride.value_object.RideOwner;
import org.project.domain.ride.value_object.RideTime;
import org.project.domain.ride.value_object.Route;
import org.project.domain.ride.value_object.SeatMap;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;
import org.project.infrastructure.repository.JetCarRepository;
import org.project.infrastructure.security.JWTUtility;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class RideHistoryResourceTest {

	@Inject
	UserRepository userRepository;

	@Inject
	RideRepository rideRepository;

	@Inject
	DriverRepository driverRepository;

	@Inject
	OwnerRepository ownerRepository;

	@Inject
	JWTUtility jwtUtility;

    @Inject
    JetCarRepository jetCarRepository;

	@Test
	void successfullyGetUserRides() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		UUID id = user.id();
		UserID userID = new UserID(id);
		DriverLicense driverLicense = TestDataGenerator.driverLicense();
		Driver driver = Driver.of(userID, driverLicense);
		driverRepository.save(driver);

		Car car = TestDataGenerator.car(userID);
		jetCarRepository.save(car);

		Location startLocation = new Location("Baku", 40.4093, 49.8671);
		Location endLocation = new Location("Sumqayit", 40.5897, 49.6686);

		RideOwner rideOwner = new RideOwner(driver.id(), null);
		Route route = new Route(startLocation, endLocation);
		RideTime rideTime = new RideTime(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusHours(1));
		Price price = TestDataGenerator.generatePrice();
		SeatMap seatMap = TestDataGenerator.generateSeatMap();
		RideDesc rideDesc = TestDataGenerator.generateRideDesc();
		Set<RideRule> rideRules = TestDataGenerator.generateRideRules();

		Ride ride = Ride.of(car.id(), rideOwner, route, rideTime, price, seatMap, rideDesc, rideRules);
		rideRepository.save(ride);

		String jwtToken = jwtUtility.generateToken(user);


		given().header("Authorization", "Bearer " + jwtToken).queryParam("pageNumber", 0).queryParam("pageSize", 10)
				.when().get("/uyol/ride/history/user-rides").then().statusCode(200);
	}

	@Test
	void successfullyGetDriverRides() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		UUID id = user.id();
		UserID userID = new UserID(id);
		DriverLicense driverLicense = TestDataGenerator.driverLicense();
		Driver driver = Driver.of(userID, driverLicense);
		driverRepository.save(driver);

		Car car = TestDataGenerator.car(userID);
		jetCarRepository.save(car);

		Location startLocation = new Location("Baku", 40.4093, 49.8671);
		Location endLocation = new Location("Sumqayit", 40.5897, 49.6686);
		RideOwner rideOwner = new RideOwner(driver.id(), null); // ownerID null ola bilər
		Route route = new Route(startLocation, endLocation);
		RideTime rideTime = new RideTime(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusHours(1));
		Price price = TestDataGenerator.generatePrice();
		SeatMap seatMap = TestDataGenerator.generateSeatMap();
		RideDesc rideDesc = TestDataGenerator.generateRideDesc();
		Set<RideRule> rideRules = TestDataGenerator.generateRideRules();

		Ride ride = Ride.of(car.id(), rideOwner, route, rideTime, price, seatMap, rideDesc, rideRules);
		rideRepository.save(ride);

		String jwtToken = jwtUtility.generateToken(user);

		given().header("Authorization", "Bearer " + jwtToken).queryParam("pageNumber", 0).queryParam("pageSize", 10)
				.when().get("/uyol/ride/history/driver-rides").then().statusCode(200);
	}

	@Test
	void successfullyGetOwnerRides() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		UUID id = user.id();
		UserID userID = new UserID(id);
		Voen voen = TestDataGenerator.voen();
		Owner owner = Owner.of(userID, voen);
		ownerRepository.save(owner);

		DriverLicense driverLicense = TestDataGenerator.driverLicense();
		Driver driver = Driver.of(userID, driverLicense);
		driverRepository.save(driver);

		Car car = TestDataGenerator.car(userID);
		jetCarRepository.save(car);

		Location startLocation = new Location("Baku", 40.4093, 49.8671);
		Location endLocation = new Location("Sumqayit", 40.5897, 49.6686);
		RideOwner rideOwner = new RideOwner(driver.id(), owner.id());
		Route route = new Route(startLocation, endLocation);
		RideTime rideTime = new RideTime(LocalDateTime.now().plusMinutes(5), LocalDateTime.now().plusHours(1));
		Price price = TestDataGenerator.generatePrice();
		SeatMap seatMap = TestDataGenerator.generateSeatMap();
		RideDesc rideDesc = TestDataGenerator.generateRideDesc();
		Set<RideRule> rideRules = TestDataGenerator.generateRideRules();

		Ride ride = Ride.of(car.id(), rideOwner, route, rideTime, price, seatMap, rideDesc, rideRules);
		rideRepository.save(ride);

		String jwtToken = jwtUtility.generateToken(user);

		given().header("Authorization", "Bearer " + jwtToken).queryParam("pageNumber", 0).queryParam("pageSize", 10)
				.when().get("/uyol/ride/history/owner-rides").then().statusCode(200);
	}
}
