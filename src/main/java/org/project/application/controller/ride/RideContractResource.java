package org.project.application.controller.ride;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.application.dto.ride.RideContractDTO;
import org.project.application.pagination.PageRequest;
import org.project.application.service.RideContractService;

import java.util.List;
import java.util.UUID;

@Path("/ride/contract")
@RolesAllowed("USER")
public class RideContractResource {

    private final JsonWebToken jwt;

    private final RideContractService rideContract;

    RideContractResource(Instance<JsonWebToken> jwt, RideContractService rideContract) {
        this.jwt = jwt.get();
        this.rideContract = rideContract;
    }

    @GET
    public RideContractDTO rideContract(@QueryParam("rideContractID") UUID rideContractID) {
        return rideContract.of(jwt.getName(), rideContractID);
    }

    @GET
    @Path("/of/ride")
    public List<RideContractDTO> rideContracts(
            @QueryParam("rideID") UUID rideID,
            @QueryParam("page") int page,
            @QueryParam("size") int size) {

        return rideContract.ofRide(jwt.getName(), rideID, new PageRequest(size, page));
    }

    @GET
    @Path("/all")
    public List<RideContractDTO> rideContracts(
            @QueryParam("page") int page,
            @QueryParam("size") int size) {

        return rideContract.ofUser(jwt.getName(), new PageRequest(size, page));
    }
}
