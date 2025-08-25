package org.project.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.project.application.dto.fleet.DriverDTO;
import org.project.application.pagination.PageRequest;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.repositories.OwnerRepository;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.factories.IdentifierFactory;
import org.project.domain.user.repositories.UserRepository;

import java.util.List;

import static org.project.application.util.RestUtil.responseException;

@ApplicationScoped
public class AvailableDriversService {

    private final UserRepository userRepository;

    private final OwnerRepository ownerRepository;

    private final DriverRepository driverRepository;

    AvailableDriversService(UserRepository userRepository, OwnerRepository ownerRepository, DriverRepository driverRepository) {
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.driverRepository = driverRepository;
    }

    public List<DriverDTO> page(String identifier, PageRequest pageRequest) {
        User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();
        if (!ownerRepository.isOwnerExists(new UserID(user.id())))
            throw responseException(Response.Status.FORBIDDEN, "Owner account is not registered");

        return driverRepository.page(pageRequest)
                .orElseThrow(()  -> responseException(Response.Status.NOT_FOUND, "Cannot found available drivers"));
    }
}
