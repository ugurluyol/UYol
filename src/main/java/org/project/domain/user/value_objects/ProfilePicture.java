package org.project.domain.user.value_objects;

import static org.project.domain.shared.util.Utils.required;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import org.project.domain.user.entities.User;

public final class ProfilePicture {
  private final String path;
  private final String imageType;
  private final byte[] profilePicture;

  public static final int MAX_SIZE = 2_097_152;
  public static final String PATH_FORMAT = "src/main/resources/static/profile/photos/%s";
  public static final String DEFAULT_PROFILE_PICTURE_PATH = "src/main/resources/static/profile/photos/default-profile-picture.png";

  private static final byte[][] IMAGE_SIGNATURES = {
      // JPEG (starts with FF D8)
      { (byte) 0xFF, (byte) 0xD8 },
      // PNG (starts with 89 50 4E 47)
      { (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47 }
  };

  private static final String[] IMAGE_EXTENSIONS = { "jpeg", "png" };

  private ProfilePicture(String path, byte[] profilePicture, String imageType) {
    this.path = path;
    this.profilePicture = profilePicture.clone();
    this.imageType = imageType;
  }

  public static ProfilePicture of(byte[] profilePicture, User user) {
    required("profilePicture", profilePicture);
    required("user", user);

    String path = profilePicturePath(user);
    String typeOfImage = validate(profilePicture)
        .orElseThrow(() -> new IllegalDomainArgumentException("Invalid profile picture type."));

    return new ProfilePicture(path, profilePicture, typeOfImage);
  }

  public static ProfilePicture fromRepository(String path, byte[] profilePicture) {
    return new ProfilePicture(path, profilePicture, checkImageExtension(profilePicture).orElseThrow());
  }

  public static String profilePicturePath(User user) {
    return String.format(PATH_FORMAT, user.id().toString());
  }

  public String path() {
    return path;
  }

  public byte[] profilePicture() {
    return profilePicture.clone();
  }

  public String imageType() {
    return imageType;
  }

  private static Optional<String> validate(byte[] profilePicture) {
    required("profilePicture", profilePicture);

    if (profilePicture.length > MAX_SIZE)
      return Optional.empty();

    return checkImageExtension(profilePicture);
  }

  private static Optional<String> checkImageExtension(byte[] profilePicture) {
    for (int i = 0, imageSignaturesLength = IMAGE_SIGNATURES.length; i < imageSignaturesLength; i++) {
      byte[] imageSignature = IMAGE_SIGNATURES[i];

      if (matchesSignature(profilePicture, imageSignature))
        return Optional.of(IMAGE_EXTENSIONS[i]);
    }

    return Optional.empty();
  }

  private static boolean matchesSignature(byte[] file, byte[] signature) {
    if (file.length < signature.length)
      return false;

    for (int i = 0; i < signature.length; i++) {
      if (file[i] != signature[i])
        return false;
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ProfilePicture that))
      return false;
    return Objects.equals(path, that.path) &&
        Objects.equals(imageType, that.imageType) &&
        Arrays.equals(profilePicture, that.profilePicture);
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(path);
    result = 31 * result + Objects.hashCode(imageType);
    result = 31 * result + Arrays.hashCode(profilePicture);
    return result;
  }
}
