package org.project.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.project.application.dto.ride.RideDTO;
import org.project.application.pagination.PageRequest;
import org.project.domain.ride.repositories.RideRepository;
import org.project.domain.ride.value_object.Location;
import org.project.domain.shared.containers.Result;

import java.time.LocalDate;
import java.util.List;

import static org.project.application.util.RestUtil.responseException;

@ApplicationScoped
public class ActiveRidesService {

    private final RideRepository rideRepository;

    ActiveRidesService(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    public List<RideDTO> pageBy(String date, PageRequest pageRequest) {
        LocalDate localDate = Result.ofThrowable(() -> LocalDate.parse(date))
                .orElseThrow(() -> responseException(Response.Status.BAD_REQUEST, "Invalid date format"));

        return rideRepository.pageOf(localDate, pageRequest)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "No data found for this page"));
    }

    public List<RideDTO> actualFor(String date, Location startLocation, Location endLocation, PageRequest pageRequest) {
        LocalDate localDate = Result.ofThrowable(() -> LocalDate.parse(date))
                .orElseThrow(() -> responseException(Response.Status.BAD_REQUEST, "Invalid date format"));

        return rideRepository.actualFor(startLocation, endLocation, localDate, pageRequest)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "No data found for this page"));
    }
}
