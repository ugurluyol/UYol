package org.project.application.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.project.application.pagination.PageRequest;
import org.project.application.service.ActiveRidesService;
import org.project.domain.ride.value_object.Location;

@Path("/ride")
public class RideResource {

    private final ActiveRidesService ridesService;

    RideResource(ActiveRidesService ridesService) {
        this.ridesService = ridesService;
    }

    @GET
    @Path("/date")
    public Response pageOf(
            @QueryParam("date") String date,
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset) {

        return Response.ok(ridesService.pageBy(date, new PageRequest(limit, offset))).build();
    }

    @GET
    @Path("/actual")
    public Response actualFor(
            @QueryParam("date") String date,
            @QueryParam("startDesc") String startDesc,
            @QueryParam("startLat") double startLat,
            @QueryParam("startLon") double startLon,
            @QueryParam("endDesc") String endDesc,
            @QueryParam("endLat") double endLat,
            @QueryParam("endLon") double endLon,
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset) {

        return Response.ok(ridesService.actualFor(date,
                new Location(startDesc, startLat, startLon),
                new Location(endDesc, endLat, endLon),
                new PageRequest(limit, offset)))
                .build();
    }
}
