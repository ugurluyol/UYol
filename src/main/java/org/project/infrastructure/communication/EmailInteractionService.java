package org.project.infrastructure.communication;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import org.project.domain.user.entities.OTP;
import org.project.domain.user.value_objects.Email;

@ApplicationScoped
public class EmailInteractionService {

    private final Mailer mailer;

    public static final String SOFT_VERIFICATION_SUBJECT = "You’ve been signed up on UYol";

    public static final String SOFT_VERIFICATION_BODY = """
            Hello,

            This email address was used to sign up for an account on Karto.
            If this was you, no further action is required.

            If you did not create this account, please contact our support team immediately so we can investigate and secure your information.

            Thank you,
            The UYol Team
            """;

    public static final String subject = "Your One-Time Password (OTP)";

    public static final String body = """
            Hello,

            Your one-time password (OTP) for account verification is:

            %s

            This code is valid for the next 5 minutes.
            If you did not request this, please ignore this email.

            Best regards,
            UYol Support Team
            """;

    EmailInteractionService(Instance<Mailer> mailer) {
        this.mailer = mailer.get();
    }

    public void sendOTP(OTP otp, Email email) {
        mailer.send(Mail.withText(email.email(), subject, body.formatted(otp.otp())));
    }

    public void sendSoftVerificationMessage(Email email) {
        mailer.send(Mail.withText(email.email(), SOFT_VERIFICATION_SUBJECT, SOFT_VERIFICATION_BODY));
    }

    public void sendMessage(Email email, String subject, String body) {
        mailer.send(Mail.withText(email.email(), subject, body));
    }
}
