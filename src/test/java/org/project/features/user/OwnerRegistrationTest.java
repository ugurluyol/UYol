package org.project.features.user;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.project.features.PostgresTestResource;
import org.project.features.util.DBManagementUtils;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class OwnerRegistrationTest {

    @Inject
    DBManagementUtils dbManagement;

    @Test
    void successfullOwnerRegistration() {

    }

    @Test
    void invalidVoen() {

    }

    @Test
    void invalidIdentifier() {

    }

    @Test
    void userAccountDontExists() {

    }

    @Test
    void ownerAlreadyExists() {

    }

    @Test
    void voenAlreadyExists() {

    }
}
