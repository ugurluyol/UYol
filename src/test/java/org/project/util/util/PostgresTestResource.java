package org.project.util.util;

import java.util.Map;

import org.testcontainers.containers.PostgreSQLContainer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

	private PostgreSQLContainer<?> postgresContainer;

	@Override
	public Map<String, String> start() {
		postgresContainer = new PostgreSQLContainer<>("postgres:latest").withDatabaseName("mydb")
				.withUsername("postgres").withPassword("root");

		postgresContainer.start();

		String jdbcUrl = postgresContainer.getJdbcUrl();

		return Map.of("quarkus.datasource.jdbc.url", jdbcUrl, "quarkus.datasource.username",
				postgresContainer.getUsername(), "quarkus.datasource.password", postgresContainer.getPassword(),
				"quarkus.flyway.url", jdbcUrl, "quarkus.flyway.user", postgresContainer.getUsername(),
				"quarkus.flyway.password", postgresContainer.getPassword());
	}

	@Override
	public void stop() {
		if (postgresContainer != null) {
			postgresContainer.stop();
			postgresContainer = null;
		}
	}
}