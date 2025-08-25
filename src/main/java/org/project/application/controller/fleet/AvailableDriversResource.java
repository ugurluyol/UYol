package org.project.application.controller.fleet;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.application.dto.fleet.DriverDTO;
import org.project.application.pagination.PageRequest;
import org.project.application.service.AvailableDriversService;

import java.util.List;

@Path("/available/drivers")
@RolesAllowed("USER")
public class AvailableDriversResource {

    private final JsonWebToken jwt;

    private final AvailableDriversService availableDrivers;

    AvailableDriversResource(Instance<JsonWebToken> jwt, AvailableDriversService availableDrivers) {
        this.jwt = jwt.get();
        this.availableDrivers = availableDrivers;
    }

    @GET
    public List<DriverDTO> availableDrivers(
            @QueryParam("page") int page,
            @QueryParam("size") int size) {

        return availableDrivers.page(jwt.getName(), new PageRequest(size, page));
    }
}
