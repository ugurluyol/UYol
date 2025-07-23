package org.project.domain

import org.project.domain.shared.exceptions.IllegalDomainArgumentException
import org.project.domain.shared.exceptions.IllegalDomainStateException
import org.project.domain.user.entities.OTP
import org.project.util.TestDataGenerator
import spock.lang.Specification

import java.time.LocalDateTime

class OTPTest extends Specification {

    def "should create OTP and confirm it successfully"() {
        given:
        def user = TestDataGenerator.user()
        def otpCode = TestDataGenerator.otp()
        def otp = OTP.of(user, otpCode)

        expect:
        !otp.isConfirmed()
        !otp.isExpired()

        when:
        otp.confirm()

        then:
        otp.isConfirmed()
    }

    def "should not confirm already confirmed OTP"() {
        given:
        def user = TestDataGenerator.user()
        def otp = OTP.of(user, TestDataGenerator.otp())
        otp.confirm()

        when:
        otp.confirm()

        then:
        thrown(IllegalDomainArgumentException)
    }

    def "should not allow to create invalid OTP with letters"() {
        when:
        OTP.of(TestDataGenerator.user(), "12a4b6")

        then:
        thrown(IllegalDomainArgumentException)
    }

    def "should be expired if expiration time has passed"() {
        given:
        def otp = OTP.fromRepository(
                "123456",
                UUID.randomUUID(),
                false,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().minusMinutes(1)
        )

        expect:
        otp.isExpired()
    }

    def "should throw when confirming expired OTP"() {
        given:
        def otp = OTP.fromRepository(
                "123456",
                UUID.randomUUID(),
                false,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().minusMinutes(1)
        )

        when:
        otp.confirm()

        then:
        thrown(IllegalDomainStateException)
    }
}
