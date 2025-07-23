package org.project.domain

import org.project.domain.shared.exceptions.IllegalDomainStateException
import org.project.domain.user.entities.User
import org.project.domain.user.exceptions.BannedUserException
import org.project.infrastructure.security.HOTPGenerator
import org.project.util.TestDataGenerator
import spock.lang.Specification

class UserTest extends Specification {

    def "should create user with valida properties"() {
        given:
        def personalData = TestDataGenerator.personalData()
        def secretKey = HOTPGenerator.generateSecretKey()

        when:
        def user = User.of(personalData, secretKey)

        then:
        notThrown(Throwable)
        user.personalData() != null
        user.id() != null
        user.keyAndCounter() != null
        user.keyAndCounter().counter() == 0
        user.accountDates() != null
        !user.isVerified()
        !user.isBanned()
    }

    def "should successfully verify user"() {
        given:
        def user = TestDataGenerator.user()

        when:
        user.incrementCounter()

        then:
        !user.isVerified()
        user.keyAndCounter().counter() == 1

        when:
        user.enable()

        then:
        user.isVerified()
        user.keyAndCounter().counter() == 1
    }

    def "should throw an exception when trying ti verify user without generated OTP(mean incremented counter)"() {
        given:
        def user = TestDataGenerator.user()

        when:
        user.enable()

        then:
        def e = thrown(IllegalDomainStateException)
        e.getMessage() == "It is prohibited to activate an account that has not been verified."
    }

    def "should throw an exception when trying to verify user twice"() {
        given:
        def user = TestDataGenerator.user()
        user.incrementCounter()
        user.enable()

        when:
        user.enable()

        then:
        def e = thrown(IllegalDomainStateException)
        e.getMessage() == "You can`t active already verified user."
    }

    def "should throw an exception when trying to verify banned user"() {
        given:
        def user = TestDataGenerator.user()
        user.ban()

        when:
        user.enable()

        then:
        def e = thrown(BannedUserException)
        e.getMessage() == "Access denied: this user account has been banned due to a violation of platform rules. Contact support for further assistance."
    }
}
