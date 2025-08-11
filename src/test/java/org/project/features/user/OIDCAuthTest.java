package org.project.features.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.test.common.QuarkusTestResource;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.project.features.KeycloakTestResource;
import org.project.features.util.DBManagementUtils;

import static io.restassured.RestAssured.given;

@Disabled("""
Temporarily disabled. Until realm is loaded into the test container for OIDC automatically.
""")
@QuarkusTestResource(KeycloakTestResource.class)
class OIDCAuthTest {

    private final DBManagementUtils dbManagementUtils;

    static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    OIDCAuthTest(DBManagementUtils dbManagementUtils) {
        this.dbManagementUtils = dbManagementUtils;
    }

    @Test
    void validOpenIDTest() {
        oidcRegistration();
        removeUser();
    }

    private static void oidcRegistration() {
        final String idToken = idToken();

        given()
                .header("X-ID-TOKEN", idToken)
                .when()
                .post("/uyol/auth/oidc")
                .then()
                .assertThat()
                .statusCode(jakarta.ws.rs.core.Response.Status.OK.getStatusCode());
    }

    private void removeUser() {
        dbManagementUtils.removeUser("alice@keycloak.org");
    }

    private static String idToken() {
        final String tokenEndpoint = String
                .format("%s/realms/%s/protocol/openid-connect/token", "http://localhost:7080", "uyol-realm");
        final Response response = given()
                .contentType(ContentType.URLENC)
                .formParam("grant_type", "password")
                .formParam("client_id", "uyol")
                .formParam("username", "alice")
                .formParam("password", "alice")
                .formParam("client_secret", "secret")
                .formParam("scope", "openid")
                .post(tokenEndpoint);

        if (response.statusCode() != 200)
            throw new IllegalStateException("Failed to get idToken: %s.".formatted(response.getBody().asString()));
        return response.jsonPath().getString("id_token");
    }
}
