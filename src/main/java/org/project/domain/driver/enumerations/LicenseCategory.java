package org.project.domain.driver.enumerations;

public enum LicenseCategory {
  A("Motorcycles"),
  B("Passenger cars"),
  C("Trucks"),
  D("Buses"),
  E("Trailers");

  private final String description;

  LicenseCategory(String description) {
    this.description = description;
  }

  public String description() {
    return description;
  }
}
