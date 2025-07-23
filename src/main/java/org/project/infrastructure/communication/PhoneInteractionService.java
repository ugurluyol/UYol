package org.project.infrastructure.communication;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.entities.PartnerVerificationOTP;
import org.project.karto.domain.user.entities.OTP;

@ApplicationScoped
public class PhoneInteractionService {

    private static final String KARTO_PHONE = "+15005550006";

    public void sendOTP(Phone phone, OTP otp) {
        Message.creator(new PhoneNumber(phone.phoneNumber()), new PhoneNumber(KARTO_PHONE), otp.otp()).create();
    }

    public void sendOTP(Phone phone, PartnerVerificationOTP otp) {
        Message.creator(new PhoneNumber(phone.phoneNumber()), new PhoneNumber(KARTO_PHONE), otp.otp()).create();
    }

    public void sendMessage(Phone phone, String message) {
        Message.creator(new PhoneNumber(phone.phoneNumber()), new PhoneNumber(KARTO_PHONE), message).create();
    }
}
