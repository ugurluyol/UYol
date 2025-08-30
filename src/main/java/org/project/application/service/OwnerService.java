package org.project.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.project.application.dto.fleet.CarDTO;
import org.project.application.dto.ride.RideRequestToDriver;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.repositories.OwnerRepository;
import org.project.domain.fleet.value_objects.*;
import org.project.domain.ride.entities.RideRequest;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.factories.IdentifierFactory;
import org.project.domain.user.repositories.UserRepository;
import org.project.infrastructure.cache.RideRequests;

import static org.project.application.util.RestUtil.required;
import static org.project.application.util.RestUtil.responseException;

@ApplicationScoped
public class OwnerService {

    private final RideRequests rideRequests;

    private final CarRepository carRepository;

    private final UserRepository userRepository;

    private final OwnerRepository ownerRepository;

    private final DriverRepository driverRepository;

    OwnerService(
            RideRequests rideRequests,
            UserRepository userRepository,
            OwnerRepository ownerRepository,
            CarRepository carRepository,
            DriverRepository driverRepository) {

        this.rideRequests = rideRequests;
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.carRepository = carRepository;
        this.driverRepository = driverRepository;
    }

    public void register(String identifier, String voenRaw) {
        Voen voen = new Voen(voenRaw);
        User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();
        UserID userID = new UserID(user.id());

        if (ownerRepository.isOwnerExists(userID))
            throw responseException(Response.Status.CONFLICT, "Owner is already registered to this account");

        if (ownerRepository.isVoenExists(voen))
            throw responseException(Response.Status.CONFLICT, "Voen is already used");

        Owner owner = Owner.of(userID, voen);
        ownerRepository.save(owner)
                .orElseThrow(() -> responseException(Response.Status.CONFLICT,
                        "Unable to process your request at the moment. Please try again."));
    }

    public void saveCar(String identifier, CarDTO carDTO) {
        required("carForm", carDTO);

        User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();
        UserID userID = new UserID(user.id());

        if (ownerRepository.isOwnerExists(userID))
            throw responseException(Response.Status.NOT_FOUND, "Owner account is not found.");

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

    public void request(String identifier, RideRequestToDriver rideForm) {
        required("rideForm", rideForm);
        RideRequest rideRequest = rideForm.toRideRequest();

        User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();
        UserID userID = new UserID(user.id());

        if (!ownerRepository.isOwnerExists(userID))
            throw responseException(Response.Status.NOT_FOUND, "Owner account is not found.");

        Car car = carRepository.findBy(new LicensePlate(rideForm.licensePlate()))
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Car by this driver account is not found."));

        if (!car.owner().equals(userID))
            throw responseException(Response.Status.FORBIDDEN, "You are not the owner of the car or license plate is wrong.");

        if (!car.isAvailableForTheTrip())
            throw responseException(Response.Status.CONFLICT, "Selected car is already on the road.");

        Driver driver = driverRepository.findBy(new DriverID(rideForm.driverID()))
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Driver account is not found."));

        if (!driver.isAvailable())
            throw responseException(Response.Status.CONFLICT, "Driver is not available.");

        rideRequests.put(driver.id(), rideRequest);
    }
}
