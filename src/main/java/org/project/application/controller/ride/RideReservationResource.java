package org.project.application.controller.ride;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.application.dto.ride.BookingForm;
import org.project.application.dto.ride.RideContractDTO;
import org.project.application.service.RideReservationService;

@Path("/ride/reservation")
@RolesAllowed("USER")
public class RideReservationResource {

    private final JsonWebToken jwt;

    private final RideReservationService rideReservation;

    RideReservationResource(Instance<JsonWebToken> jwt, RideReservationService rideReservation) {
        this.jwt = jwt.get();
        this.rideReservation = rideReservation;
    }

    @POST
    @Path("/book")
    public RideContractDTO book(BookingForm bookingForm) {
        return rideReservation.book(jwt.getName(), bookingForm);
    }
}
