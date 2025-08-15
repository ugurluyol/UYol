package org.project.application.dto.fleet;

import java.time.LocalDateTime;

public record CarForm(
        String licensePlate,
        String carBrand,
        String carModel,
        String carColor,
        int carYear,
        int seatCount,
        LocalDateTime createdAt
) {}
