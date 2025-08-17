package org.project.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.project.application.dto.fleet.CarDTO;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.value_objects.*;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.factories.IdentifierFactory;
import org.project.domain.user.repositories.UserRepository;

import static org.project.application.util.RestUtil.required;
import static org.project.application.util.RestUtil.responseException;

@ApplicationScoped
public class DriverService {

    private final UserRepository userRepository;

    private final DriverRepository driverRepository;

    private final CarRepository carRepository;

    DriverService(UserRepository userRepository, DriverRepository driverRepository, CarRepository carRepository) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.carRepository = carRepository;
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
}
