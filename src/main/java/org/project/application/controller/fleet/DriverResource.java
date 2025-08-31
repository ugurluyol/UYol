package org.project.application.controller.fleet;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.application.dto.fleet.CarDTO;
import org.project.application.dto.ride.DriverRideForm;
import org.project.application.dto.ride.RideDTO;
import org.project.application.dto.ride.RideRequestToDriver;
import org.project.application.service.DriverService;
import org.project.domain.ride.enumerations.RideRule;
import org.project.domain.ride.value_object.RideRequestID;

import java.util.List;
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

    @PATCH
    @Path("/remove/ride-rule")
    public Response removeRideRule(@QueryParam("ride-rule") RideRule rideRule, @QueryParam("rideID") UUID rideID) {
        service.removeRideRule(jwt.getName(), rideRule, rideID);
        return Response.accepted().build();
    }

    @POST
    @Path("/start/ride")
    public Response startRide(@QueryParam("rideID") UUID rideID) {
        service.startRide(jwt.getName(), rideID);
        return Response.accepted().build();
    }

    @POST
    @Path("/cancel/ride")
    public Response cancelRide(@QueryParam("rideID") UUID rideID) {
        service.cancelRide(jwt.getName(), rideID);
        return Response.accepted().build();
    }

    @POST
    @Path("/finish/ride")
    public Response finishRide(@QueryParam("rideID") UUID rideID) {
        service.finishRide(jwt.getName(), rideID);
        return Response.accepted().build();
    }

    @GET
    @Path("/ride-requests")
    public List<RideRequestToDriver> rideRequests() {
        return service.rideRequests(jwt.getName());
    }

    @POST
    @Path("/accept/ride-request")
    public RideDTO acceptRideRequest(@QueryParam("rideRequestID") UUID rideRequestID) {
        return service.acceptRideRequest(jwt.getName(), new RideRequestID(rideRequestID));
    }
}
