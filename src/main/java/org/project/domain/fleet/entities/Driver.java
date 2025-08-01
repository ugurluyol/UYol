package org.project.domain.fleet.entities;

import java.util.Objects;
import java.util.UUID;

import org.project.domain.fleet.value_objects.DriverID;
import org.project.domain.fleet.value_objects.DriverLicense;
import org.project.domain.fleet.value_objects.UserID;

import static org.project.domain.shared.util.Utils.required;

public class Driver {
  private final DriverID id;
  private final UserID userID;
  private DriverLicense license;

  private Driver(DriverID id, UserID userID, DriverLicense license) {
    this.id = id;
    this.userID = userID;
    this.license = license;
  }

  public static Driver of(UserID userID, DriverLicense license) {
    required("userID", userID);
    required("licenseNumber", license);

    return new Driver(new DriverID(UUID.randomUUID()), userID, license);
  }

  public static Driver fromRepository(
          DriverID id,
          UserID userID,
          DriverLicense license) {

    return new Driver(id, userID, license);
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

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Driver driver = (Driver) o;
    return Objects.equals(id, driver.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
