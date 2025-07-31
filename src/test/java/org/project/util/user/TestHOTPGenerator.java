package org.project.util.user;

import org.project.infrastructure.security.HOTPGenerator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

@Alternative
@ApplicationScoped
public class TestHOTPGenerator extends HOTPGenerator {
	@Override
	public String generateHOTP(String key, long counter) {
		return "123456";
	}
}
