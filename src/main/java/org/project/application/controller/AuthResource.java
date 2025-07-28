package org.project.application.controller;

import org.project.application.dto.auth.LoginForm;
import org.project.application.dto.auth.RegistrationForm;
import org.project.application.service.AuthService;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
		authService.login(loginForm);
		return Response.ok().build();
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
}