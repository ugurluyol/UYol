package org.project.domain.ride.value_object;

import static org.project.domain.shared.util.Utils.required;

import java.math.BigDecimal;

public record Price(BigDecimal amount) {
  public Price {
    required("priceAmount", amount);

    if (amount.compareTo(BigDecimal.ZERO) < 0)
      throw new IllegalArgumentException("Invalid price: cannot be below zero");
  }
}
