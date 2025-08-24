package org.project.domain.fleet;

import org.junit.jupiter.api.Test;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.enumerations.DriverStatus;
import org.project.domain.fleet.value_objects.NoRating;
import org.project.domain.fleet.value_objects.TotalRides;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import org.project.domain.shared.value_objects.Dates;
import org.project.domain.shared.value_objects.DriverID;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.project.features.util.TestDataGenerator.driver;
import static org.project.features.util.TestDataGenerator.driverLicense;

class DriverTest {

    @Test
    void shouldCreateDriverWithOfMethod() {
        var userID = UserID.newID();
        var license = driverLicense();

        var driver = Driver.of(userID, license);

        assertNotNull(driver.id());
        assertEquals(userID, driver.userID());
        assertEquals(license, driver.license());
        assertNotNull(driver.dates());
    }

    @Test
    void shouldCreateDriverFromRepository() {
        var id = new DriverID(UUID.randomUUID());
        var userID = UserID.newID();
        var license = driverLicense();
        var dates = Dates.defaultDates();

        var driver = Driver.fromRepository(id, userID, license, dates, DriverStatus.AVAILABLE,
                TotalRides.defaultRides(), new NoRating());

        assertEquals(id, driver.id());
        assertEquals(userID, driver.userID());
        assertEquals(license, driver.license());
        assertEquals(dates, driver.dates());
    }

    @Test
    void shouldChangeLicenseSuccessfully() {
        var driver = driver();
        var newLicense = driverLicense();

        driver.changeLicense(newLicense);

        assertEquals(newLicense, driver.license());
    }

    @Test
    void shouldThrowWhenUserIdIsNull() {
        assertThrows(IllegalDomainArgumentException.class,
                () -> Driver.of(null, driverLicense()));
    }

    @Test
    void shouldThrowWhenLicenseIsNull() {
        assertThrows(IllegalDomainArgumentException.class,
                () -> Driver.of(UserID.newID(), null));
    }

    @Test
    void shouldThrowWhenChangingLicenseToNull() {
        var driver = driver();
        assertThrows(IllegalDomainArgumentException.class,
                () -> driver.changeLicense(null));
    }

    @Test
    void shouldBeEqualWhenIdIsSame() {
        var id = new DriverID(UUID.randomUUID());
        var d1 = Driver.fromRepository(id, UserID.newID(), driverLicense(), Dates.defaultDates(),
                DriverStatus.AVAILABLE, TotalRides.defaultRides(), new NoRating());
        var d2 = Driver.fromRepository(id, UserID.newID(), driverLicense(), Dates.defaultDates(),
                DriverStatus.AVAILABLE, TotalRides.defaultRides(), new NoRating());

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenIdIsDifferent() {
        var d1 = driver();
        var d2 = driver();

        assertNotEquals(d1, d2);
    }
}
