package org.project.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.project.application.dto.fleet.CarDTO;
import org.project.application.dto.ride.DriverRideForm;
import org.project.application.dto.ride.RideDTO;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.value_objects.*;
import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.repositories.RideRepository;
import org.project.domain.ride.value_object.*;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.factories.IdentifierFactory;
import org.project.domain.user.repositories.UserRepository;

import java.util.Arrays;
import java.util.HashSet;

import static org.project.application.util.RestUtil.required;
import static org.project.application.util.RestUtil.responseException;

@ApplicationScoped
public class DriverService {

    private final CarRepository carRepository;

    private final UserRepository userRepository;

    private final RideRepository rideRepository;

    private final DriverRepository driverRepository;

    DriverService(UserRepository userRepository, DriverRepository driverRepository,
                  CarRepository carRepository, RideRepository rideRepository) {

        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.carRepository = carRepository;
        this.rideRepository = rideRepository;
    }

    public void register(String identifier, String driverLicense) {
        DriverLicense license = new DriverLicense(driverLicense);
        User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();

        if (driverRepository.isDriverExists(new UserID(user.id())))
            throw responseException(Response.Status.CONFLICT, "Driver is already registered on this user account.");

        if (driverRepository.isLicenseExists(license))
            throw responseException(Response.Status.CONFLICT, "This driver license is already registered.");

        Driver driver = Driver.of(new UserID(user.id()), license);
        driverRepository.save(driver)
                .orElseThrow(()  -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Unable to process your request at the moment. Please try again."));
    }

    public void saveCar(String identifier, CarDTO carDTO) {
        required("carForm", carDTO);

        User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();
        UserID userID = new UserID(user.id());

        if (!driverRepository.isDriverExists(userID))
            throw responseException(Response.Status.NOT_FOUND, "Driver account is not found.");

        Car car = Car.of(
                userID,
                new LicensePlate(carDTO.licensePlate()),
                new CarBrand(carDTO.carBrand()),
                new CarModel(carDTO.carModel()),
                new CarColor(carDTO.carColor()),
                new CarYear(carDTO.carYear()),
                new SeatCount(carDTO.seatCount())
        );

        carRepository.save(car)
                .orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Unable to process your request at the moment. Please try again."));
    }

    public RideDTO createRide(String identified, DriverRideForm rideForm) {
        required("rideForm", rideForm);

        User user = userRepository.findBy(IdentifierFactory.from(identified)).orElseThrow();
        UserID userID = new UserID(user.id());
        Driver driver = driverRepository.findBy(userID)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Driver account is not found."));

        Car car = carRepository.findBy(new LicensePlate(rideForm.licensePlate()))
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Car by this driver account is not found."));

        if (!car.owner().equals(userID))
            throw responseException(Response.Status.FORBIDDEN, "You are not the owner of the car or license plate is wrong.");

        if (!car.isAvailableForTheTrip())
            throw responseException(Response.Status.CONFLICT, "Selected car is already on the road.");

        Location from = new Location(rideForm.fromLocationDesc(), rideForm.fromLatitude(), rideForm.fromLongitude());
        Location to = new Location(rideForm.toLocationDesc(), rideForm.toLatitude(), rideForm.toLongitude());

        Ride ride = Ride.of(
                car.id(),
                new RideOwner(driver.id(), null),
                new Route(from, to),
                new RideTime(rideForm.startTime(), rideForm.endTime()),
                new Price(rideForm.price()),
                new SeatMap(rideForm.seatMap()),
                new RideDesc(rideForm.rideDesc()),
                new HashSet<>(Arrays.asList(rideForm.rideRules()))
        );
        car.startedRide();

        rideRepository.save(ride)
                .orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Unable to process your request at the moment. Please try again."));

        carRepository.update(car)
                .orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Unable to process your request at the moment. Please try again."));

        return RideDTO.from(ride);
    }
}
