package org.project.application.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.project.application.dto.auth.LoginForm;
import org.project.application.dto.auth.PasswordChangeForm;
import org.project.application.dto.auth.RegistrationForm;
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
	@Path("/login")
	public Response login(LoginForm loginForm) {
		return Response.ok(authService.login(loginForm)).build();
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
	public Response refresh(@HeaderParam("Refresh-Token") String refreshToken) {
		return Response.ok(authService.refreshToken(refreshToken)).build();
	}

	@POST
	@Path("/2FA")
	public Response initiate2FA(LoginForm loginForm) {
		authService.enable2FA(loginForm);
		return Response.accepted("OTP sent. Please verify.").build();
	}

	@PATCH
	@Path("/2FA/verification")
	public Response verify2FA(@QueryParam("otp") String otp) {
		return Response.ok(authService.twoFactorAuth(otp)).build();
	}

	@POST
	@Path("/start/password/change")
	public Response startPasswordChange(@QueryParam("identifier") String identifier) {
		authService.startPasswordChange(identifier);
		return Response.ok("Confirm OTP.").build();
	}

	@PATCH
	@Path("/apply/password/change")
	public Response applyPasswordChange(PasswordChangeForm passwordChangeForm) {
		authService.applyPasswordChange(passwordChangeForm);
		return Response.accepted().build();
	}
}
