package org.project.domain.ride.value_object;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

import java.math.BigDecimal;

import static org.project.domain.shared.util.Utils.required;

public record Fee(BigDecimal value) {
    public static final BigDecimal FEE_RATE = BigDecimal.valueOf(0.02);

    public Fee {
        required("fee", value);
        if (value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalDomainArgumentException("Fee amount cannot be negative");
    }

    public static Fee zero() {
        return new Fee(BigDecimal.ZERO);
    }

    public Fee calculateFeeForBooking(Price bookingPrice) {
        required("bookingPrice", bookingPrice);
        return new Fee(bookingPrice.amount().multiply(FEE_RATE));
    }
}
