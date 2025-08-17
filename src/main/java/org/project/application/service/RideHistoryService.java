package org.project.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.project.application.dto.ride.RideDTO;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.repositories.OwnerRepository;
import org.project.domain.ride.repositories.RideRepository;
import org.project.domain.shared.value_objects.Pageable;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.factories.IdentifierFactory;
import org.project.domain.user.repositories.UserRepository;

import java.util.List;

import static org.project.application.util.RestUtil.required;
import static org.project.application.util.RestUtil.responseException;

@ApplicationScoped
public class RideHistoryService {

    private final RideRepository repo;

    private final UserRepository userRepo;

    private final OwnerRepository ownerRepo;

    private final DriverRepository driverRepo;

    RideHistoryService(
            RideRepository repo,
            UserRepository userRepo,
            DriverRepository driverRepo,
            OwnerRepository ownerRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.driverRepo = driverRepo;
        this.ownerRepo = ownerRepo;
    }

    public List<RideDTO> userRides(String identifier, Pageable page) {
        required("page", page);
        User user = userRepo.findBy(IdentifierFactory.from(identifier)).orElseThrow();

        return repo.pageOf(new UserID(user.id()), page)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Ride history not found"));
    }

    public List<RideDTO> driverRides(String identifier, Pageable page) {
        required("page", page);
        User user = userRepo.findBy(IdentifierFactory.from(identifier)).orElseThrow();
        Driver driver = driverRepo.findBy(new UserID(user.id()))
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Driver account not found"));

        return repo.pageOf(driver.id(), page)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Ride history not found"));
    }

    public List<RideDTO> ownerRides(String identifier, Pageable page) {
        required("page", page);
        User user = userRepo.findBy(IdentifierFactory.from(identifier)).orElseThrow();
        Owner owner = ownerRepo.findBy(new UserID(user.id()))
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Owner account not found"));

        return repo.pageOf(owner.id(), page)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Owner history not found"));
    }
}
