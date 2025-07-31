package org.project.infrastructure.security;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.domain.shared.containers.Result;
import org.project.domain.user.entities.User;

import io.quarkus.logging.Log;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Singleton;

@Singleton
public class JWTUtility {

    private final JWTParser jwtParser;

    private static final RSAPublicKey keycloackPublicKey = readX509PublicKey();

    public JWTUtility(JWTParser jwtParser) {
        this.jwtParser = jwtParser;
    }

    public String generateToken(User user) {
        Duration oneDayAndSecond = Duration.ofDays(1).plusSeconds(1);

        return Jwt.issuer("UYol")
                .upn(retrieveUPN(user))
                .groups(user.role().name())
                .claim("firstname", user.personalData().firstname())
                .claim("surname", user.personalData().surname())
                .claim("isVerified", user.isVerified())
                .expiresIn(oneDayAndSecond)
                .sign();
    }

    public String generateRefreshToken(User user) {
        Duration year = Duration.ofDays(365);

        return Jwt.issuer("UYol")
                .upn(retrieveUPN(user))
                .groups(user.role().name())
                .expiresIn(year)
                .sign();
    }

    public Result<JsonWebToken, Throwable> parse(String token) {
        try {
            return Result.success(jwtParser.parse(token));
        } catch (ParseException e) {
            Log.error("Can`t parse jwt.", e);
            return Result.failure(e);
        }
    }

    public Result<JsonWebToken, Throwable> verifyAndParse(String jwt) {
        try {
            JsonWebToken verified = jwtParser.verify(jwt, keycloackPublicKey);
            return Result.success(verified);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    private static String retrieveUPN(User user) {
        return user.personalData().email().isPresent() ?
                user.personalData().email().get() :
                user.personalData().phone().orElseThrow();
    }

    private static RSAPublicKey readX509PublicKey() {
		try (InputStream is = JWTUtility.class.getClassLoader().getResourceAsStream("keycloackPublicKey.pem")) {
			if (is == null) {
				throw new IllegalStateException("Public key file not found in resources");
			}
			String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
					.replace("-----END PUBLIC KEY-----", "").replaceAll("\\s+", "");

            byte[] encoded = Base64.decodeBase64(key);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }
}