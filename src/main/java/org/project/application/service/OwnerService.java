package org.project.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.project.application.dto.fleet.CarForm;
import org.project.domain.fleet.entities.Car;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.fleet.repositories.OwnerRepository;
import org.project.domain.fleet.value_objects.*;
import org.project.domain.user.entities.User;
import org.project.domain.user.factories.IdentifierFactory;
import org.project.domain.user.repositories.UserRepository;

import static org.project.application.util.RestUtil.required;
import static org.project.application.util.RestUtil.responseException;

@ApplicationScoped
public class OwnerService {

    private final CarRepository carRepository;

    private final UserRepository userRepository;

    private final OwnerRepository ownerRepository;

    OwnerService(UserRepository userRepository, OwnerRepository ownerRepository, CarRepository carRepository) {
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.carRepository = carRepository;
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

    public void saveCar(String identifier, CarForm carForm) {
        required("carForm", carForm);

        User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();
        UserID userID = new UserID(user.id());

        if (ownerRepository.isOwnerExists(userID))
            throw responseException(Response.Status.NOT_FOUND, "Owner account is not found.");

        Car car = Car.of(
                userID,
                new LicensePlate(carForm.licensePlate()),
                new CarBrand(carForm.carBrand()),
                new CarModel(carForm.carModel()),
                new CarColor(carForm.carColor()),
                new CarYear(carForm.carYear()),
                new SeatCount(carForm.seatCount())
        );

        carRepository.save(car)
                .orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Unable to process your request at the moment. Please try again."));
    }
}
