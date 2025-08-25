package org.project.application.controller.ride;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.application.dto.ride.BookingForm;
import org.project.application.dto.ride.RideContractDTO;
import org.project.application.service.RideReservationService;
import org.project.domain.ride.value_object.RideID;

import java.util.UUID;

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

    @POST
    @Path("/rate/driver")
    public Response rateDriver(@QueryParam("rideID") UUID rideID, @QueryParam("score") int score) {
        rideReservation.rateDriver(jwt.getName(), new RideID(rideID), score);
        return Response.accepted().build();
    }
}
