package org.project.application.controller.fleet;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.application.dto.fleet.CarDTO;
import org.project.application.dto.ride.RideRequestToDriver;
import org.project.application.service.OwnerService;

@Path("/owner")
@RolesAllowed("USER")
public class OwnerResource {

    private final JsonWebToken jwt;

    private final OwnerService service;

    OwnerResource(Instance<JsonWebToken> jwt, OwnerService service) {
        this.jwt = jwt.get();
        this.service = service;
    }

    @POST
    @Path("/register")
    public Response registerOwner(@QueryParam("voen") String voen) {
        service.register(jwt.getName(), voen);
        return Response.accepted().build();
    }

    @POST
    @Path("/car/save")
    public Response saveCar(CarDTO carDTO) {
        service.saveCar(jwt.getName(), carDTO);
        return Response.accepted().build();
    }

    @POST
    @Path("/ride/request")
    public Response request(RideRequestToDriver rideForm) {
        service.request(jwt.getName(), rideForm);
        return Response.accepted().build();
    }
}
