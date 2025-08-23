package org.project.domain.fleet.entities;

import java.util.Objects;
import java.util.UUID;

import org.project.domain.fleet.enumerations.DriverStatus;
import org.project.domain.shared.exceptions.IllegalDomainStateException;
import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.fleet.value_objects.DriverLicense;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.shared.value_objects.Dates;

import static org.project.domain.shared.util.Utils.required;

public class Driver {
  private final DriverID id;
  private final UserID userID;
  private DriverLicense license;
  private Dates dates;
  private DriverStatus status;

  private Driver(DriverID id, UserID userID, DriverLicense license, Dates dates, DriverStatus status) {
    this.id = id;
    this.userID = userID;
    this.license = license;
    this.dates = dates;
    this.status = status;
  }

  public static Driver of(UserID userID, DriverLicense license) {
    required("userID", userID);
    required("licenseNumber", license);

    return new Driver(new DriverID(UUID.randomUUID()), userID, license, Dates.defaultDates(), DriverStatus.AVAILABLE);
  }

  public static Driver fromRepository(
      DriverID id,
      UserID userID,
      DriverLicense license,
      Dates dates,
      DriverStatus status) {

    return new Driver(id, userID, license, dates, status);
  }

  public DriverID id() {
    return id;
  }

  public UserID userID() {
    return userID;
  }

  public DriverLicense license() {
    return license;
  }

  public void changeLicense(DriverLicense license) {
    required("licenseNumber", license);
    this.license = license;
  }

  public DriverStatus status() {
    return status;
  }

  public void startedRide() {
    if (status == DriverStatus.ON_THE_ROAD)
      throw new IllegalDomainStateException("Driver is already on the road");
    this.status = DriverStatus.ON_THE_ROAD;
    touch();
  }

  public void finishedRide() {
    if (status == DriverStatus.AVAILABLE)
      throw new IllegalDomainStateException("Driver is already off the road");
    this.status = DriverStatus.AVAILABLE;
    touch();
  }

  public Dates dates() {
    return dates;
  }

  private void touch() {
    this.dates = dates.updated();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass())
      return false;
    Driver driver = (Driver) o;
    return Objects.equals(id, driver.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
