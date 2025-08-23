package org.project.application.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.project.application.dto.auth.*;
import org.project.application.dto.common.Info;
import org.project.application.service.AuthService;

import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
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

	@POST
	@Path("/oidc")
	public Tokens oidcAuth(@HeaderParam("X-ID-TOKEN") String idToken) {
		return authService.oidcAuth(idToken);
	}

	@POST
	@Path("/login")
	public Tokens login(LoginForm loginForm) {
		return authService.login(loginForm);
	}

	@GET
	@Path("/resend-otp")
	public Response resendOTP(@QueryParam("identifier") String identifier) {
		authService.resendOTP(identifier);
		return Response.ok().build();
	}

	@PATCH
	@Path("/verification")
	public Response verification(@QueryParam("otp") String otp) {
		authService.verification(otp);
		return Response.accepted().build();
	}

	@PATCH
	@Path("/refresh-token")
	public Token refresh(@HeaderParam("Refresh-Token") String refreshToken) {
		return authService.refreshToken(refreshToken);
	}

	@POST
	@Path("/2FA")
	public Info initiate2FA(LoginForm loginForm) {
		authService.enable2FA(loginForm);
		return new Info("OTP sent. Please verify.");
	}

	@PATCH
	@Path("/2FA/verification")
	public Tokens verify2FA(@QueryParam("otp") String otp) {
		return authService.twoFactorAuth(otp);
	}

	@POST
	@Path("/start/password/change")
	public Info startPasswordChange(@QueryParam("identifier") String identifier) {
		authService.startPasswordChange(identifier);
		return new Info("Confirm OTP.");
	}

	@PATCH
	@Path("/apply/password/change")
	public Response applyPasswordChange(PasswordChangeForm passwordChangeForm) {
		authService.applyPasswordChange(passwordChangeForm);
		return Response.accepted().build();
	}
}
