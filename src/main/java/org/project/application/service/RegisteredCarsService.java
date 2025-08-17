package org.project.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.project.application.dto.fleet.CarDTO;
import org.project.domain.fleet.repositories.CarRepository;
import org.project.domain.shared.value_objects.Pageable;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.factories.IdentifierFactory;
import org.project.domain.user.repositories.UserRepository;

import java.util.List;

import static org.project.application.util.RestUtil.responseException;

@ApplicationScoped
public class RegisteredCarsService {

    private final CarRepository carRepository;

    private final UserRepository userRepository;

    RegisteredCarsService(CarRepository carRepository, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    public List<CarDTO> registeredCars(String identifier, Pageable page) {
        User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();
        return carRepository.pageOf(page, new UserID(user.id()))
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "No found cars"));
    }
}
