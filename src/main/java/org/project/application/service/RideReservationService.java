package org.project.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.project.application.dto.ride.BookingForm;
import org.project.application.dto.ride.RideContractDTO;
import org.project.application.util.RestUtil;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.ride.entities.Ride;
import org.project.domain.ride.entities.RideContract;
import org.project.domain.ride.repositories.RideContractRepository;
import org.project.domain.ride.repositories.RideRepository;
import org.project.domain.ride.value_object.BookedSeats;
import org.project.domain.ride.value_object.RideID;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.user.entities.User;
import org.project.domain.user.factories.IdentifierFactory;
import org.project.domain.user.repositories.UserRepository;

import static org.project.application.util.RestUtil.*;

@ApplicationScoped
public class RideReservationService {

    private final UserRepository userRepository;

    private final RideRepository rideRepository;

    private final DriverRepository driverRepository;

    private final RideContractRepository rideContractRepository;

    RideReservationService(
            UserRepository userRepository,
            RideRepository rideRepository,
            DriverRepository driverRepository,
            RideContractRepository rideContractRepository) {

        this.userRepository = userRepository;
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
        this.rideContractRepository = rideContractRepository;
    }

    public RideContractDTO book(String identifier, BookingForm bookingForm) {
        required("bookingForm", bookingForm);

        User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();

        RideID rideID = new RideID(bookingForm.rideID());
        Ride ride = rideRepository.findBy(rideID)
                .orElseThrow(() -> responseException(Response.Status.BAD_REQUEST, "This ride do not exists."));

        RideContract rideContract = ride.book(new UserID(user.id()), new BookedSeats(bookingForm.bookedSeats()));

        rideContractRepository.save(rideContract).orElseThrow(RestUtil::unableToProcessRequestException);
        rideRepository.updateSeats(ride).orElseThrow(RestUtil::unableToProcessRequestException);
        return RideContractDTO.from(rideContract);
    }

    public void rateDriver(String identifier, RideID rideID, int score) {
        User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();
        Ride ride = rideRepository.findBy(rideID)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "This ride do not exists."));

        if (!ride.isFinished())
            throw responseException(Response.Status.BAD_REQUEST, "You can`t rate driver until ride is not finished.");

        if (!rideContractRepository.isExists(new UserID(user.id()), ride.id()))
            throw responseException(Response.Status.FORBIDDEN, "You were`t a part of this ride");

        Driver driver = driverRepository.findBy(ride.rideOwner().driverID()).orElseThrow(RestUtil::unableToProcessRequestException);

        boolean selfRating = driver.id().value().equals(user.id());
        if (selfRating)
            throw responseException(Response.Status.BAD_REQUEST, "You cannot rate yourself");

        driver.rate(score);
        driverRepository.updateRating(driver).orElseThrow(RestUtil::unableToProcessRequestException);
    }
}
