package org.project.features.user;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
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
		if (!userSaveResult.success()) {
			fail("User save failed: " + userSaveResult.throwable().getMessage());
		}
		UserID userID = new UserID(user.id());

		// 2. Driver yaradılır və save olunur
		DriverLicense license = TestDataGenerator.driverLicense();
		Driver driver = Driver.of(userID, license);
		Result<Integer, Throwable> driverSaveResult = driverRepository.save(driver);
		if (!driverSaveResult.success()) {
			fail("Driver save failed: " + driverSaveResult.throwable().getMessage());
		}

		// 3. RideOwner yaradılır (owner nullable)
		RideOwner rideOwner = new RideOwner(driver.id(), null);

		// 4. Location və Route
		Location start = new Location("Baku", 40.4093, 49.8671);
		Location end = new Location("Sumqayit", 40.5897, 49.6686);
		Route route = new Route(start, end);

		// 5. RideTime gələcəkdə olmalıdır
		LocalDateTime now = LocalDateTime.now();
		RideTime rideTime = new RideTime(now.plusMinutes(5), now.plusHours(1));

		// 6. Ride yaradılır və save olunur
		Ride ride = Ride.of(rideOwner, route, rideTime, TestDataGenerator.generatePrice(),
				TestDataGenerator.generateSeatMap(), TestDataGenerator.generateRideDesc(),
				TestDataGenerator.generateRideRules());
		Result<Integer, Throwable> rideSaveResult = rideRepository.save(ride);
		if (!rideSaveResult.success()) {
			fail("Ride save failed: " + rideSaveResult.throwable().getMessage());
		}

		// 7. Sorğu parametrləri ride ilə tam uyğun olmalıdır
		String rideDate = rideTime.startOfTheTrip().toLocalDate().toString();

		given().queryParam("date", rideDate).queryParam("startDesc", start.description())
				.queryParam("startLat", start.latitude()).queryParam("startLon", start.longitude())
				.queryParam("endDesc", end.description()).queryParam("endLat", end.latitude())
				.queryParam("endLon", end.longitude()).queryParam("limit", 5).queryParam("offset", 0).when()
				.get("/uyol/ride/actual").then().statusCode(200);
	}
	@Test
	void rideSaveFailsWhenDriverMissing() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Voen voen = TestDataGenerator.voen();
		Owner owner = Owner.of(new UserID(user.id()), voen);
		ownerRepository.save(owner);

		assertThrows(IllegalDomainArgumentException.class, () -> {
			new RideOwner(null, owner.id());
		});
	}

	@Test
	void rideTimeValidation() {
		LocalDateTime past = LocalDateTime.now().minusHours(1);
		LocalDateTime future = LocalDateTime.now().plusHours(1);

		try {
			RideTime rideTime = new RideTime(past, future);
			fail("Should throw IllegalDomainArgumentException for past start time");
		} catch (IllegalDomainArgumentException e) {
		}
	}
}