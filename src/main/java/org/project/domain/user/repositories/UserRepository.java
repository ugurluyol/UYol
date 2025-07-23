package org.project.domain.user.repositories;

import org.project.domain.shared.containers.Result;
import org.project.domain.user.entities.User;
import org.project.domain.user.value_objects.Email;
import org.project.domain.user.value_objects.Phone;
import org.project.domain.user.value_objects.RefreshToken;

import java.util.UUID;

public interface UserRepository {

    Result<Integer, Throwable> save(User user);

    Result<Integer, Throwable> saveRefreshToken(RefreshToken refreshToken);

    Result<Integer, Throwable> updatePhone(User user);

    Result<Integer, Throwable> updateCounter(User user);

    Result<Integer, Throwable> updateVerification(User user);

    Result<Integer, Throwable> updateBan(User user);

    boolean isEmailExists(Email email);

    boolean isPhoneExists(Phone phone);

    Result<User, Throwable> findBy(UUID id);

    Result<User, Throwable> findBy(Email email);

    Result<User, Throwable> findBy(Phone phone);

    Result<RefreshToken, Throwable> findRefreshToken(String refreshToken);
}
