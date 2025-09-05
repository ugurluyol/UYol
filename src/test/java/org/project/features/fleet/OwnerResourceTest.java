package org.project.features.fleet;

import static io.restassured.RestAssured.given;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.project.application.dto.fleet.CarDTO;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.repositories.OwnerRepository;
import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.enumerations.RideRule;
import org.project.domain.ride.enumerations.RideStatus;
import org.project.domain.ride.enumerations.SeatStatus;
import org.project.domain.ride.repositories.RideRepository;
import org.project.domain.ride.value_object.Fee;
import org.project.domain.ride.value_object.Location;
import org.project.domain.ride.value_object.RideDesc;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.ride.value_object.RideOwner;
import org.project.domain.ride.value_object.RideTime;
import org.project.domain.ride.value_object.Route;
import org.project.domain.ride.value_object.SeatMap;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.repositories.UserRepository;
import org.project.features.util.PostgresTestResource;
import org.project.features.util.TestDataGenerator;
import org.project.infrastructure.security.JWTUtility;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class OwnerResourceTest {

    @Inject
	UserRepository userRepository;

	@Inject
	DriverRepository driverRepository;

	@Inject
	CarRepository carRepository;

	@Inject
	OwnerRepository ownerRepository;

	@Inject
	RideRepository rideRepository;

	@Inject
	JWTUtility jwtUtility;

    @Test
    void successfulOwnerRegistration() {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		String voen = TestDataGenerator.voen().value();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    void invalidVoen() {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		String voen = "";

		given().header("Authorization", "Bearer " + jwtToken).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(400);
    }

    @Test
    void userAccountDontExists() {
		User fakeUser = TestDataGenerator.user();
		String jwtToken = jwtUtility.generateToken(fakeUser);

		String voen = TestDataGenerator.voen().value();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(404);
    }

    @Test
    void ownerAlreadyExists() {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		String voen = TestDataGenerator.voen().value();


		given().header("Authorization", "Bearer " + jwtToken).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(Response.Status.ACCEPTED.getStatusCode());


		given().header("Authorization", "Bearer " + jwtToken).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(409);
    }

    @Test
    void voenAlreadyExists() {

		User user1 = TestDataGenerator.user();
		userRepository.save(user1);
		String jwtToken1 = jwtUtility.generateToken(user1);

		String voen = TestDataGenerator.voen().value();

		given().header("Authorization", "Bearer " + jwtToken1).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(Response.Status.ACCEPTED.getStatusCode());


		User user2 = TestDataGenerator.user();
		userRepository.save(user2);
		String jwtToken2 = jwtUtility.generateToken(user2);

		given().header("Authorization", "Bearer " + jwtToken2).queryParam("voen", voen).when()
				.post("uyol/owner/register").then().statusCode(409);
    }

	@Test
	void successfullySaveCar() {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		CarDTO carDTO = TestDataGenerator.carForm();

		given().header("Authorization", "Bearer " + jwtToken).contentType("application/json").body(carDTO).when()
				.post("uyol/owner/car/save").then().statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void emptyCarForm() {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		given().header("Authorization", "Bearer " + jwtToken).contentType("application/json").body("{}").when()
				.post("uyol/owner/car/save").then().statusCode(400);
	}

	@Test
	void ownerNotExists() {
		User fakeUser = TestDataGenerator.user();
		String jwtToken = jwtUtility.generateToken(fakeUser);

		CarDTO carDTO = TestDataGenerator.carForm();

		given().header("Authorization", "Bearer " + jwtToken).contentType("application/json").body(carDTO).when()
				.post("uyol/owner/car/save").then().statusCode(404);
	}

	@Test
	void invalidLicensePlate() {
		User user = TestDataGenerator.user();
		userRepository.save(user);
		String jwtToken = jwtUtility.generateToken(user);

		CarDTO carDTO = TestDataGenerator.carForm();
		carDTO = new CarDTO("INVALID!", carDTO.carBrand(), carDTO.carModel(), carDTO.carColor(), carDTO.carYear(),
				carDTO.seatCount());

		given().header("Authorization", "Bearer " + jwtToken).contentType("application/json").body(carDTO).when()
				.post("uyol/owner/car/save").then().statusCode(400);
	}


	@Test
	void successfullyAddRideRule() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Owner owner = Owner.of(new UserID(user.id()), TestDataGenerator.voen());
		ownerRepository.save(owner);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		Car car = Car.of(new UserID(user.id()), TestDataGenerator.generateLicensePlate(),
				TestDataGenerator.generateCarBrand(), TestDataGenerator.generateCarModel(),
				TestDataGenerator.generateCarColor(), TestDataGenerator.generateCarYear(),
				TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		String jwtToken = jwtUtility.generateToken(user);

		Ride ride = Ride.fromRepository(RideID.newID(), car.id(), new RideOwner(driver.id(), owner.id()),
				new Route(new Location("From place", 40.123, 49.123), new Location("To place", 40.321, 49.321)),
				new RideTime(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)),
				TestDataGenerator.generatePrice(),
				new SeatMap(new SeatStatus[][] { { SeatStatus.DRIVER, SeatStatus.EMPTY },
						{ SeatStatus.EMPTY, SeatStatus.EMPTY } }),
				RideStatus.PENDING, new RideDesc("Test ride"), Set.of(RideRule.NO_SMOKING), Dates.defaultDates(), false,
				Fee.zero());

		rideRepository.save(ride);

		UUID rideID = ride.id().value();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("ride-rule", RideRule.NO_PETS)
				.queryParam("rideID", rideID).when().patch("/uyol/owner/add/ride-rule").then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void successfullyRemoveRideRule() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Owner owner = Owner.of(new UserID(user.id()), TestDataGenerator.voen());
		ownerRepository.save(owner);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		Car car = Car.of(new UserID(user.id()), TestDataGenerator.generateLicensePlate(),
				TestDataGenerator.generateCarBrand(), TestDataGenerator.generateCarModel(),
				TestDataGenerator.generateCarColor(), TestDataGenerator.generateCarYear(),
				TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		String jwtToken = jwtUtility.generateToken(user);

		Ride ride = Ride.fromRepository(RideID.newID(), car.id(), new RideOwner(driver.id(), owner.id()),
				new Route(new Location("From place", 40.123, 49.123), new Location("To place", 40.321, 49.321)),
				new RideTime(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)),
				TestDataGenerator.generatePrice(),
				new SeatMap(new SeatStatus[][] { { SeatStatus.DRIVER, SeatStatus.EMPTY },
						{ SeatStatus.EMPTY, SeatStatus.EMPTY } }),
				RideStatus.PENDING, new RideDesc("Test ride"), Set.of(RideRule.NO_SMOKING), Dates.defaultDates(), false,
				Fee.zero());

		rideRepository.save(ride);
		UUID rideID = ride.id().value();


		given().header("Authorization", "Bearer " + jwtToken).queryParam("ride-rule", RideRule.NO_PETS)
				.queryParam("rideID", rideID).when().patch("/uyol/owner/add/ride-rule").then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());

		given().header("Authorization", "Bearer " + jwtToken).queryParam("ride-rule", RideRule.NO_PETS)
				.queryParam("rideID", rideID).when().patch("/uyol/owner/remove/ride-rule").then()
				.statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void successfullyStartRide() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Owner owner = Owner.of(new UserID(user.id()), TestDataGenerator.voen());
		ownerRepository.save(owner);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		Car car = Car.of(new UserID(user.id()), TestDataGenerator.generateLicensePlate(),
				TestDataGenerator.generateCarBrand(), TestDataGenerator.generateCarModel(),
				TestDataGenerator.generateCarColor(), TestDataGenerator.generateCarYear(),
				TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		String jwtToken = jwtUtility.generateToken(user);

		Ride ride = Ride.fromRepository(RideID.newID(), car.id(), new RideOwner(driver.id(), owner.id()),
				new Route(new Location("From place", 40.123, 49.123), new Location("To place", 40.321, 49.321)),
				new RideTime(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)),
				TestDataGenerator.generatePrice(),
				new SeatMap(new SeatStatus[][] { { SeatStatus.DRIVER, SeatStatus.EMPTY },
						{ SeatStatus.EMPTY, SeatStatus.EMPTY } }),
				RideStatus.PENDING, new RideDesc("Test ride"), Set.of(RideRule.NO_SMOKING), Dates.defaultDates(), false,
				Fee.zero());

		rideRepository.save(ride);
		UUID rideID = ride.id().value();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/owner/start/ride").then().statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void successfullyCancelRide() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Owner owner = Owner.of(new UserID(user.id()), TestDataGenerator.voen());
		ownerRepository.save(owner);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		Car car = Car.of(new UserID(user.id()), TestDataGenerator.generateLicensePlate(),
				TestDataGenerator.generateCarBrand(), TestDataGenerator.generateCarModel(),
				TestDataGenerator.generateCarColor(), TestDataGenerator.generateCarYear(),
				TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		String jwtToken = jwtUtility.generateToken(user);

		Ride ride = Ride.fromRepository(RideID.newID(), car.id(), new RideOwner(driver.id(), owner.id()),
				new Route(new Location("From place", 40.123, 49.123), new Location("To place", 40.321, 49.321)),
				new RideTime(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)),
				TestDataGenerator.generatePrice(),
				new SeatMap(new SeatStatus[][] { { SeatStatus.DRIVER, SeatStatus.EMPTY },
						{ SeatStatus.EMPTY, SeatStatus.EMPTY } }),
				RideStatus.PENDING, new RideDesc("Test ride"), Set.of(RideRule.NO_SMOKING), Dates.defaultDates(), false,
				Fee.zero());

		rideRepository.save(ride);
		UUID rideID = ride.id().value();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/owner/cancel/ride").then().statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void successfullyFinishRide() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Owner owner = Owner.of(new UserID(user.id()), TestDataGenerator.voen());
		ownerRepository.save(owner);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		Car car = Car.of(new UserID(user.id()), TestDataGenerator.generateLicensePlate(),
				TestDataGenerator.generateCarBrand(), TestDataGenerator.generateCarModel(),
				TestDataGenerator.generateCarColor(), TestDataGenerator.generateCarYear(),
				TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		String jwtToken = jwtUtility.generateToken(user);

		Ride ride = Ride.fromRepository(RideID.newID(), car.id(), new RideOwner(driver.id(), owner.id()),
				new Route(new Location("From place", 40.123, 49.123), new Location("To place", 40.321, 49.321)),
				new RideTime(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)),
				TestDataGenerator.generatePrice(),
				new SeatMap(new SeatStatus[][] { { SeatStatus.DRIVER, SeatStatus.EMPTY },
						{ SeatStatus.EMPTY, SeatStatus.EMPTY } }),
				RideStatus.PENDING, new RideDesc("Test ride"), Set.of(RideRule.NO_SMOKING), Dates.defaultDates(), false,
				Fee.zero());

		rideRepository.save(ride);
		UUID rideID = ride.id().value();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/owner/start/ride").then().statusCode(Response.Status.ACCEPTED.getStatusCode());

		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/owner/finish/ride").then().statusCode(Response.Status.ACCEPTED.getStatusCode());
	}

	@Test
	void cannotStartRideTwiceOwner() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Owner owner = Owner.of(new UserID(user.id()), TestDataGenerator.voen());
		ownerRepository.save(owner);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		Car car = Car.of(new UserID(user.id()), TestDataGenerator.generateLicensePlate(),
				TestDataGenerator.generateCarBrand(), TestDataGenerator.generateCarModel(),
				TestDataGenerator.generateCarColor(), TestDataGenerator.generateCarYear(),
				TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		String jwtToken = jwtUtility.generateToken(user);

		Ride ride = Ride.fromRepository(RideID.newID(), car.id(), new RideOwner(driver.id(), owner.id()),
				new Route(new Location("From", 40.0, 50.0), new Location("To", 41.0, 51.0)),
				new RideTime(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)),
				TestDataGenerator.generatePrice(),
				new SeatMap(new SeatStatus[][] { { SeatStatus.DRIVER, SeatStatus.EMPTY },
						{ SeatStatus.EMPTY, SeatStatus.EMPTY } }),
				RideStatus.PENDING, new RideDesc("Test ride"), Set.of(RideRule.NO_SMOKING), Dates.defaultDates(), false,
				Fee.zero());
		rideRepository.save(ride);
		UUID rideID = ride.id().value();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/owner/start/ride").then().statusCode(Response.Status.ACCEPTED.getStatusCode());

		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/owner/start/ride").then().statusCode(Response.Status.BAD_REQUEST.getStatusCode());
	}

	@Test
	void cannotFinishRideWithoutStartOwner() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Owner owner = Owner.of(new UserID(user.id()), TestDataGenerator.voen());
		ownerRepository.save(owner);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		Car car = Car.of(new UserID(user.id()), TestDataGenerator.generateLicensePlate(),
				TestDataGenerator.generateCarBrand(), TestDataGenerator.generateCarModel(),
				TestDataGenerator.generateCarColor(), TestDataGenerator.generateCarYear(),
				TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		String jwtToken = jwtUtility.generateToken(user);

		Ride ride = Ride.fromRepository(RideID.newID(), car.id(), new RideOwner(driver.id(), owner.id()),
				new Route(new Location("From", 40.0, 50.0), new Location("To", 41.0, 51.0)),
				new RideTime(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)),
				TestDataGenerator.generatePrice(),
				new SeatMap(new SeatStatus[][] { { SeatStatus.DRIVER, SeatStatus.EMPTY },
						{ SeatStatus.EMPTY, SeatStatus.EMPTY } }),
				RideStatus.PENDING, new RideDesc("Test ride"), Set.of(RideRule.NO_SMOKING), Dates.defaultDates(), false,
				Fee.zero());
		rideRepository.save(ride);
		UUID rideID = ride.id().value();

		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/owner/finish/ride").then().statusCode(Response.Status.BAD_REQUEST.getStatusCode());
	}

	@Test
	void cannotCancelRideAfterStartOwner() {
		User user = TestDataGenerator.user();
		userRepository.save(user);

		Owner owner = Owner.of(new UserID(user.id()), TestDataGenerator.voen());
		ownerRepository.save(owner);

		Driver driver = Driver.of(new UserID(user.id()), TestDataGenerator.driverLicense());
		driverRepository.save(driver);

		Car car = Car.of(new UserID(user.id()), TestDataGenerator.generateLicensePlate(),
				TestDataGenerator.generateCarBrand(), TestDataGenerator.generateCarModel(),
				TestDataGenerator.generateCarColor(), TestDataGenerator.generateCarYear(),
				TestDataGenerator.generateSeatCount());
		carRepository.save(car);

		String jwtToken = jwtUtility.generateToken(user);

		Ride ride = Ride.fromRepository(RideID.newID(), car.id(), new RideOwner(driver.id(), owner.id()),
				new Route(new Location("From", 40.0, 50.0), new Location("To", 41.0, 51.0)),
				new RideTime(LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2)),
				TestDataGenerator.generatePrice(),
				new SeatMap(new SeatStatus[][] { { SeatStatus.DRIVER, SeatStatus.EMPTY },
						{ SeatStatus.EMPTY, SeatStatus.EMPTY } }),
				RideStatus.PENDING, new RideDesc("Test ride"), Set.of(RideRule.NO_SMOKING), Dates.defaultDates(), false,
				Fee.zero());
		rideRepository.save(ride);
		UUID rideID = ride.id().value();
		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/owner/start/ride").then().statusCode(Response.Status.ACCEPTED.getStatusCode());

		given().header("Authorization", "Bearer " + jwtToken).queryParam("rideID", rideID).when()
				.post("/uyol/owner/cancel/ride").then().statusCode(Response.Status.BAD_REQUEST.getStatusCode());
	}


}