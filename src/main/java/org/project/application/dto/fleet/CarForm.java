package org.project.application.dto.fleet;

public record CarForm(
        String licensePlate,
        String carBrand,
        String carModel,
        String carColor,
        int carYear,
        int seatCount
) {}
