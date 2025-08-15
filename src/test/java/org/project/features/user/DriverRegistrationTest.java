package org.project.features.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.project.application.dto.auth.RegistrationForm;
import org.project.features.PostgresTestResource;
import org.project.features.TestDataGenerator;
import org.project.features.util.DBManagementUtils;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class DriverRegistrationTest {

    @Inject
    DBManagementUtils dbManagement;

    @Test
    void successfullyRegisterDriver() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagement.saveAndVerifyUser(form);


    }

    @Test
    void invalidDriverLicense() {

    }

    @Test
    void invalidIdentifier() {

    }

    @Test
    void userAccountDontExists() {

    }

    @Test
    void driverAlreadyExists() {

    }

    @Test
    void driverLicenseAlreadyExists() {

    }
}
