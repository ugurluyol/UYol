package org.project.application.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.application.dto.ride.RideDTO;
import org.project.application.pagination.PageRequest;
import org.project.application.service.RideHistoryService;

import java.util.List;

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
    public List<RideDTO> userRides(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        return historyService.userRides(jwt.getName(), new PageRequest(pageSize, pageNumber));
    }

    @GET
    @Path("/driver-rides")
    public List<RideDTO> driverRides(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        return historyService.driverRides(jwt.getName(), new PageRequest(pageSize, pageNumber));
    }

    @GET
    @Path("/owner-rides")
    public List<RideDTO> ownerRides(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        return historyService.ownerRides(jwt.getName(), new PageRequest(pageSize, pageNumber));
    }
}
