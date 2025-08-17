package org.project.application.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.application.pagination.PageRequest;
import org.project.application.service.RideHistoryService;

@Path("/ride/history")
@RolesAllowed("USER")
public class RideHistoryResource {

    private final JsonWebToken jwt;

    private final RideHistoryService historyService;

    RideHistoryResource(Instance<JsonWebToken> jwt, RideHistoryService historyService) {
        this.jwt = jwt.get();
        this.historyService = historyService;
    }

    @GET
    @Path("/user-rides")
    public Response userRides(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        return Response.ok(historyService.userRides(jwt.getName(), new PageRequest(pageSize, pageNumber))).build();
    }

    @GET
    @Path("/driver-rides")
    public Response driverRides(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        return Response.ok(historyService.driverRides(jwt.getName(), new PageRequest(pageSize, pageNumber))).build();
    }

    @GET
    @Path("/owner-rides")
    public Response ownerRides(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        return Response.ok(historyService.ownerRides(jwt.getName(), new PageRequest(pageSize, pageNumber))).build();
    }
}
