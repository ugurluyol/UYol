package org.project.application.controller.user;

import java.io.InputStream;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.application.dto.profile.ProfilePictureDTO;
import org.project.application.service.UserProfileService;
import org.project.domain.user.value_objects.ProfilePicture;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/user/profile")
@RolesAllowed("USER")
public class UserProfileResource {

  private final JsonWebToken jwt;

  private final UserProfileService profile;

  UserProfileResource(UserProfileService profile, JsonWebToken jwt) {
    this.profile = profile;
    this.jwt = jwt;
  }

  @GET
  @Path("/picture")
  public ProfilePictureDTO picture() {
    ProfilePicture profilePicture = profile.profilePictureOf(jwt.getName());
    return new ProfilePictureDTO(profilePicture.profilePicture(), profilePicture.imageType());
  }

  @PUT
  @Path("/picture/change")
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  public Response changePicture(InputStream inputStream) {
    profile.changeProfilePictureOf(jwt.getName(), inputStream);
    return Response.accepted().build();
  }
}
