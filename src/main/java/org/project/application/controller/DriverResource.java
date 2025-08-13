package org.project.application.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.application.service.DriverService;

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
}
