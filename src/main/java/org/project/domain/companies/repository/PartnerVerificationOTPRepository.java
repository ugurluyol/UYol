package org.project.domain.companies.repository;

import java.util.UUID;

import org.project.domain.companies.entities.PartnerVerificationOTP;
import org.project.domain.shared.containers.Result;

public interface PartnerVerificationOTPRepository {

	Result<Integer, Throwable> save(PartnerVerificationOTP otp);

	Result<Integer, Throwable> remove(PartnerVerificationOTP otp);

	Result<Integer, Throwable> updateConfirmation(PartnerVerificationOTP otp);

	Result<PartnerVerificationOTP, Throwable> findBy(PartnerVerificationOTP otp);

	Result<PartnerVerificationOTP, Throwable> findBy(UUID companyID);

	Result<PartnerVerificationOTP, Throwable> findBy(String otp);
}
