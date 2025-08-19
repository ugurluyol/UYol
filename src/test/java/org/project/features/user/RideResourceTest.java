package org.project.features.user;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.project.application.service.ActiveRidesService;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.repositories.OwnerRepository;
import org.project.domain.fleet.value_objects.DriverLicense;
import org.project.domain.fleet.value_objects.Voen;
import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.repositories.RideRepository;
import org.project.domain.ride.value_object.Location;
import org.project.domain.ride.value_object.RideOwner;
import org.project.domain.ride.value_object.RideTime;
import org.project.domain.ride.value_object.Route;
import org.project.domain.shared.containers.Result;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class RideResourceTest {

	@Inject
	UserRepository userRepository;

	@Inject
	RideRepository rideRepository;

	@Inject
	DriverRepository driverRepository;

	@Inject
	ActiveRidesService ridesService;

	@Inject
	OwnerRepository ownerRepository;


	@Test
	void successfullyGetPageOfRides() {
		Ride ride = TestDataGenerator.rideWithoutDelivery();
		rideRepository.save(ride);

		given().queryParam("date", "2025-08-19").queryParam("limit", 10).queryParam("offset", 0).when()
				.get("/uyol/ride/date").then().statusCode(200);
	}

	@Test
	void invalidDateFormatShouldReturn400() {
		given().queryParam("date", "19-08-2025").queryParam("limit", 10).queryParam("offset", 0).when()
				.get("/uyol/ride/date").then().statusCode(400);
	}

	@Test
	void successfullyGetActualRides() {
		User user = TestDataGenerator.user();
		Result<Integer, Throwable> userSaveResult = userRepository.save(user);

		UserID userID = new UserID(user.id());

		DriverLicense license = TestDataGenerator.driverLicense();
		Driver driver = Driver.of(userID, license);
		Result<Integer, Throwable> driverSaveResult = driverRepository.save(driver);

		Voen voen = TestDataGenerator.voen();
		Owner owner = Owner.of(userID, voen);
		Result<Integer, Throwable> ownerSaveResult = ownerRepository.save(owner);

		RideOwner rideOwner = new RideOwner(driver.id(), owner.id());

		Location start = new Location("Baku", 40.4093, 49.8671);
		Location end = new Location("Sumqayit", 40.5897, 49.6686);
		Route route = new Route(start, end);

		LocalDateTime now = LocalDateTime.now();
		RideTime rideTime = new RideTime(now.plusMinutes(5), now.plusHours(1));
		Ride ride = Ride.of(rideOwner, route, rideTime, TestDataGenerator.generatePrice(),
				TestDataGenerator.generateSeatMap(), TestDataGenerator.generateRideDesc(),
				TestDataGenerator.generateRideRules());

		Result<Integer, Throwable> rideSaveResult = rideRepository.save(ride);
		if (!rideSaveResult.success()) {
			fail("Ride save failed: " + rideSaveResult.throwable().getMessage());
		}


		String rideDate = rideTime.startOfTheTrip().toLocalDate().toString();

		given().queryParam("date", rideDate).queryParam("startDesc", start.description())
				.queryParam("startLat", start.latitude()).queryParam("startLon", start.longitude())
				.queryParam("endDesc", end.description()).queryParam("endLat", end.latitude())
				.queryParam("endLon", end.longitude()).queryParam("limit", 5).queryParam("offset", 0).when()
				.get("/uyol/ride/actual").then().statusCode(200);
	}
}