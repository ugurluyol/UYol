package org.project.domain.fleet.value_objects;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record Rated(int totalReviews, int sumOfScores) implements DriverRating {
    public Rated {
        if (totalReviews < 0)
            throw new IllegalDomainArgumentException("Total Rides should not be bellow zero");

        if (sumOfScores < 0)
            throw new IllegalDomainArgumentException("Sum of scores should not be bellow zero");
    }

    public static Rated firstRate(int rate) {
        validateRate(rate);

        return new Rated(1, rate);
    }

    public Rated newRate(int rate) {
        validateRate(rate);

        return new Rated(totalReviews + 1, sumOfScores + rate);
    }

    private static void validateRate(int rate) {
        if (rate < 1)
            throw new IllegalDomainArgumentException("Rate should not be bellow 1");
        if (rate > 5)
            throw new IllegalDomainArgumentException("Rate should be less than 5");
    }

    public double average() {
        return (double) sumOfScores / totalReviews;
    }
}