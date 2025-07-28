package org.project.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.domain.user.entities.User
import org.project.domain.user.value_objects.Email
import org.project.domain.user.value_objects.Phone
import org.project.domain.user.value_objects.RefreshToken
import org.project.infrastructure.repository.JetUserRepository
import org.project.infrastructure.security.JWTUtility
import org.project.util.PostgresTestResource
import org.project.util.TestDataGenerator
import spock.lang.Specification

import io.quarkus.test.junit.QuarkusTest

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class UserRepoTest extends Specification {

    @Inject
    JetUserRepository repo

    @Inject
    JWTUtility jwtUtility

    void "successfully save user"() {
        when:
        def result = repo.save(user)

        then:
        result.success()
        result.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "successfully save refresh token"() {
        given:
        def token = new RefreshToken(user.id(), jwtUtility.generateRefreshToken(user))

        when:
        def userSaveResult = repo.save(user)

        then:
        userSaveResult.success()
        userSaveResult.value() == 1

        when:
        def refreshTokenSaveResult = repo.saveRefreshToken(token)

        then:
        refreshTokenSaveResult.success()
        refreshTokenSaveResult.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "successfully update counter"() {
        when:
        def result = repo.save(user)

        then:
        result.success()
        result.value() == 1

        when:
        user.incrementCounter()
        def updateCounterResult = repo.updateCounter(user)

        then:
        notThrown(Exception)
        updateCounterResult.success()
        updateCounterResult.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "successfully update verification"() {
        when:
        def result = repo.save(user)

        then:
        result.success()
        result.value() == 1

        when:
        user.incrementCounter()
        user.enable()
        def verificationResult = repo.updateVerification(user)

        then:
        notThrown(Exception)
        verificationResult.success()
        verificationResult.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "successfully update ban"() {
        given:
        User user = TestDataGenerator.user()

        when:
        user.ban()

        then:
        repo.updateBan(user)
    }

    void "successfully update 2fa"() {
        when:
        def result = repo.save(user)

        then:
        result.success()
        result.value() == 1

        when:
        user.incrementCounter()
        user.enable()
        user.incrementCounter()
        user.enable2FA()
        def _2faResult = repo.update2FA(user)

        then:
        notThrown(Exception)
        _2faResult.success()
        _2faResult.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successful is email exists"() {
        when:
        def result = repo.save(user)

        then:
        result.success()
        result.value() == 1

        when:
        def isExistsResult = repo.isEmailExists(new Email(user.personalData().email().orElseThrow()))

        then:
        isExistsResult

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "fail is exists by non existent email"() {
        when:
        def isExistsResult = repo.isEmailExists(new Email(user.personalData().email().orElseThrow()))

        then:
        !isExistsResult

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "successful is phone number exists"() {
        when:
        def result = repo.save(user)

        then:
        result.success()

        when:
        def isExistsResult = repo.isPhoneExists(new Phone(user.personalData().phone().orElseThrow()))

        then:
        isExistsResult

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "fail is exists by non existent phone number"() {
        when:
        def isExistsResult = repo.isPhoneExists(new Phone(user.personalData().phone().orElseThrow()))

        then:
        !isExistsResult

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "successful find by ID"() {
        when:
        def result = repo.save(user)

        then:
        result.success()

        when:
        def findResult = repo.findBy(user.id())

        then:
        findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "fail find by non existent ID"() {
        when:
        def findResult = repo.findBy(user.id())

        then:
        !findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "successful find by email"() {
        when:
        def result = repo.save(user)

        then:
        result.success()

        when:
        def findResult = repo.findBy(new Email(user.personalData().email().orElseThrow()))

        then:
        findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "fail find by non existent email"() {
        when:
        def findResult = repo.findBy(new Email(user.personalData().email().orElseThrow()))

        then:
        !findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "successful find by phone number"() {
        when:
        def result = repo.save(user)

        then:
        result.success()

        when:
        def findResult = repo.findBy(new Phone(user.personalData().phone().orElseThrow()))

        then:
        findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }

    void "fail find by non existent phone number"() {
        when:
        def findResult = repo.findBy(new Phone(user.personalData().phone().orElseThrow()))

        then:
        !findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.user()})
    }
}
