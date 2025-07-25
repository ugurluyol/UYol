package org.project.util.user;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;
//import io.quarkus.test.junit.TestProfile

public class MyTestProf implements QuarkusTestProfile {

	@Override
	public Map<String, String> getConfigOverrides() {
		return Map.of("quarkus.oidc.auth-server-url", "http://localhost:9999/auth/realms/test",
				"quarkus.oidc.client-id", "test-client", "quarkus.oidc.credentials.secret", "secret",
				"quarkus.oidc.devservices.enabled", "false");
	}
}
