package org.project.domain.driver.value_objects;

import java.time.LocalDate;
import java.util.regex.Pattern;
import org.project.domain.driver.enumerations.LicenseCategory;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import static org.project.domain.shared.util.Utils.required;

public record DriverLicense(
    String licenseNumber,
    LocalDate issueDate,
    LocalDate expirationDate,
    String issuingAuthority,
    LicenseCategory category) {

  private static final Pattern LICENSE_NUMBER_PATTERN = Pattern.compile("^[0-9]{2}\\s[0-9]{2}\\s[0-9]{6}$");

  public DriverLicense {
    required("category", category);
    licenseNumber = validateLicenseNumber(licenseNumber);
    issueDate = validateIssueDate(issueDate);
    expirationDate = validateExpirationDate(issueDate, expirationDate);
    issuingAuthority = validateIssuingAuthority(issuingAuthority);
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

  public static LocalDate validateIssueDate(LocalDate issueDate) {
    required("issueDate", issueDate);

    LocalDate now = LocalDate.now();
    if (issueDate.isAfter(now))
      throw new IllegalDomainArgumentException("Issue date cannot be in the future");

    return issueDate;
  }

  public static LocalDate validateExpirationDate(LocalDate issueDate, LocalDate expirationDate) {
    required("expirationDate", expirationDate);

    if (expirationDate.isBefore(issueDate) || expirationDate.isEqual(issueDate))
      throw new IllegalDomainArgumentException("Expiration date must be after issue date");

    return expirationDate;
  }

  public static String validateIssuingAuthority(String issuingAuthority) {
    required("issuingAuthority", issuingAuthority);

    if (issuingAuthority.trim().isEmpty())
      throw new IllegalDomainArgumentException("Issuing authority cannot be empty or blank");

    return issuingAuthority.trim();
  }

  public boolean isExpired() {
    return LocalDate.now().isAfter(expirationDate);
  }

  public boolean isValid() {
    return !isExpired();
  }
}
