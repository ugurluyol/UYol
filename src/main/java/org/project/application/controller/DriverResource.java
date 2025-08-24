package org.project.application.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.application.dto.fleet.CarDTO;
import org.project.application.dto.ride.DriverRideForm;
import org.project.application.dto.ride.RideDTO;
import org.project.application.service.DriverService;
import org.project.domain.ride.enumerations.RideRule;

import java.util.UUID;

@Path("/driver")
@RolesAllowed("USER")
public class DriverResource {

    private final JsonWebToken jwt;

    private final DriverService service;

    DriverResource(Instance<JsonWebToken> jwt, DriverService service) {
        this.jwt = jwt.get();
        this.service = service;
    }

    @POST
    @Path("/registration")
    public Response registerDriver(@QueryParam("driver_license") String driverLicense) {
        service.register(jwt.getName(), driverLicense);
        return Response.accepted().build();
    }

    @POST
    @Path("/car/save")
    public Response saveCar(CarDTO carDTO) {
        service.saveCar(jwt.getName(), carDTO);
        return Response.accepted().build();
    }

    @POST
    @Path("/create/ride")
    public RideDTO createRide(DriverRideForm rideForm) {
        return service.createRide(jwt.getName(), rideForm);
    }

    @PATCH
    @Path("/add/ride-rule")
    public Response addRideRule(@QueryParam("ride-rule") RideRule rideRule, @QueryParam("rideID") UUID rideID) {
        service.addRideRule(jwt.getName(), rideRule, rideID);
        return Response.accepted().build();
    }
}
