package org.project.domain.fleet.repositories;

import org.project.domain.fleet.entities.Owner;
import org.project.domain.fleet.value_objects.Voen;
import org.project.domain.shared.value_objects.OwnerID;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.shared.containers.Result;

public interface OwnerRepository {

    Result<Integer, Throwable> save(Owner owner);

    Result<Owner, Throwable> findBy(OwnerID ownerID);

    Result<UserID, Throwable> findBy(UserID userID);

    boolean isOwnerExists(UserID userID);

    boolean isVoenExists(Voen voen);
}
