package org.project.domain.driver.entities;

import java.util.Objects;
import java.util.UUID;

import org.project.domain.driver.value_objects.DriverLicense;

import static org.project.domain.shared.util.Utils.required;

public class Driver {
  private final UUID id;
  private final UUID userID;
  private DriverLicense license;

  private Driver(UUID id, UUID userID, DriverLicense license) {
    this.id = id;
    this.userID = userID;
    this.license = license;
  }

  public static Driver of(UUID userID, DriverLicense license) {
    required("userID", userID);
    required("licenseNumber", license);

    return new Driver(UUID.randomUUID(), userID, license);
  }

  public static Driver fromRepository(
          UUID id,
          UUID userID,
          DriverLicense license) {

    return new Driver(id, userID, license);
  }

  public UUID id() {
    return id;
  }

  public UUID userID() {
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
