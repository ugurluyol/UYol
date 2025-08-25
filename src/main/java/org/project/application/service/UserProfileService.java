package org.project.application.service;

import java.io.InputStream;

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

  private final ProfilePictureRepository pictureRepository;

  UserProfileService(UserRepository userRepository, ProfilePictureRepository pictureRepository) {
    this.userRepository = userRepository;
    this.pictureRepository = pictureRepository;
  }

  public UserProfileDTO of(String identifier) {
    return UserProfileDTO.from(userRepository.findBy(IdentifierFactory.from(identifier)).orElseThrow());
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
