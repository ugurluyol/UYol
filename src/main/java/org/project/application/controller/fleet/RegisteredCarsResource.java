package org.project.application.controller.fleet;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.application.dto.fleet.CarDTO;
import org.project.application.pagination.PageRequest;
import org.project.application.service.RegisteredCarsService;

import java.util.List;

@Path("/registered/cars")
@RolesAllowed("USER")
public class RegisteredCarsResource {

    private final JsonWebToken jwt;

    private final RegisteredCarsService carsService;

    RegisteredCarsResource(Instance<JsonWebToken> jwt, RegisteredCarsService carsService) {
        this.jwt = jwt.get();
        this.carsService = carsService;
    }

    @GET
    public List<CarDTO> registeredCars(@QueryParam("pageNumber") int pageNumber, @QueryParam("pageSize") int pageSize) {
        return carsService.registeredCars(jwt.getName(), new PageRequest(pageSize, pageNumber));
    }
}
