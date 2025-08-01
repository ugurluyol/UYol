package org.project.domain.driver.value_objects;

import java.util.regex.Pattern;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import static org.project.domain.shared.util.Utils.required;

public record DriverLicense(String licenseNumber) {

  private static final Pattern LICENSE_NUMBER_PATTERN = Pattern.compile("^[0-9]{2}\\s[0-9]{2}\\s[0-9]{6}$");

  public DriverLicense {
    licenseNumber = validateLicenseNumber(licenseNumber);
  }

  public static String validateLicenseNumber(String licenseNumber) {
    required("licenseNumber", licenseNumber);

    if (licenseNumber.trim().isEmpty())
      throw new IllegalDomainArgumentException("License number cannot be empty or blank.");

    String trimmed = licenseNumber.trim();
    if (!LICENSE_NUMBER_PATTERN.matcher(trimmed).matches())
      throw new IllegalDomainArgumentException("Invalid license number format. Expected: XX XX XXXXXX");

    return trimmed;
  }
}
