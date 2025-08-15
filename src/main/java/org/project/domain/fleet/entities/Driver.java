package org.project.domain.fleet.entities;

import java.util.Objects;
import java.util.UUID;

import org.project.domain.shared.value_objects.DriverID;
import org.project.domain.fleet.value_objects.DriverLicense;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.shared.value_objects.Dates;

import static org.project.domain.shared.util.Utils.required;

public class Driver {
  private final DriverID id;
  private final UserID userID;
  private DriverLicense license;
  private final Dates dates;

  private Driver(DriverID id, UserID userID, DriverLicense license, Dates dates) {
    this.id = id;
    this.userID = userID;
    this.license = license;
    this.dates = dates;
  }

  public static Driver of(UserID userID, DriverLicense license) {
    required("userID", userID);
    required("licenseNumber", license);

    return new Driver(new DriverID(UUID.randomUUID()), userID, license, Dates.defaultDates());
  }

  public static Driver fromRepository(
      DriverID id,
      UserID userID,
      DriverLicense license,
      Dates dates) {

    return new Driver(id, userID, license, dates);
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

  public Dates dates() {
    return dates;
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
