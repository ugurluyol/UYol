package org.project.application.service;

import java.io.InputStream;

import org.project.application.dto.profile.UserProfileDTO;
import org.project.domain.fleet.entities.Driver;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.repositories.DriverRepository;
import org.project.domain.fleet.repositories.OwnerRepository;
import org.project.domain.user.entities.User;
import org.project.domain.user.factories.IdentifierFactory;
import org.project.domain.user.repositories.UserRepository;
import org.project.domain.user.value_objects.ProfilePicture;
import org.project.infrastructure.files.ProfilePictureRepository;
import org.project.infrastructure.files.StreamUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response.Status;

import static org.project.application.util.RestUtil.required;
import static org.project.application.util.RestUtil.responseException;

@ApplicationScoped
public class UserProfileService {

  private final UserRepository userRepository;

  private final OwnerRepository ownerRepository;

  private final DriverRepository driverRepository;

  private final ProfilePictureRepository pictureRepository;

  UserProfileService(
          UserRepository userRepository,
          OwnerRepository ownerRepository,
          DriverRepository driverRepository,
          ProfilePictureRepository pictureRepository) {

    this.userRepository = userRepository;
    this.ownerRepository = ownerRepository;
    this.driverRepository = driverRepository;
    this.pictureRepository = pictureRepository;
  }

  public UserProfileDTO of(String identifier) {
    User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();
    Driver driver = driverRepository.findBy(user.userID()).orElse(null);
    Owner owner = ownerRepository.findBy(user.userID()).orElse(null);
    return UserProfileDTO.from(user, driver, owner);
  }

  public void changeProfilePictureOf(String identifier, InputStream inputStream) {
    required("Picture", inputStream);
    User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();

    byte[] pictureBytes = StreamUtils.toByteArray(inputStream)
        .orElseThrow(() -> responseException(Status.BAD_REQUEST, "Invalid picture provided."));
   
    user.profilePicture(ProfilePicture.of(pictureBytes, user));
    pictureRepository.put(user);
  }

  public ProfilePicture profilePictureOf(String identifier) {
    User user = userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow();
    return pictureRepository.load(ProfilePicture.profilePicturePath(user))
        .orElseThrow(() -> responseException(Status.NOT_FOUND, "Profile picture not found."));
  }
}
