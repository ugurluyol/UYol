package org.project.application.controller;

import org.project.application.dto.auth.RegistrationForm;
import org.project.application.service.AuthService;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/auth")
public class AuthResource {

	private final AuthService authService;

	AuthResource(AuthService authService) {
		this.authService = authService;
	}

	@POST
	@Path("/registration")
	public Response registration(RegistrationForm registrationForm) {
		authService.registration(registrationForm);
		return Response.accepted().build();
	}
}
