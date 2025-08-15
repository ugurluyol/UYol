package org.project.domain.fleet.entities;

import org.project.domain.shared.value_objects.OwnerID;
import org.project.domain.shared.value_objects.UserID;
import org.project.domain.fleet.value_objects.Voen;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static org.project.domain.shared.util.Utils.required;

public class Owner {
    private final OwnerID id;
    private final UserID userID;
    private final Voen voen;
    private final LocalDateTime createdAt;

    private Owner(OwnerID id, UserID userID, Voen voen, LocalDateTime createdAt) {
        this.id = id;
        this.userID = userID;
        this.voen = voen;
        this.createdAt = createdAt;
    }

    public static Owner of(UserID userID, Voen voen) {
        required("userID", userID);
        required("voen", voen);
        return new Owner(new OwnerID(UUID.randomUUID()), userID, voen, LocalDateTime.now());
    }

    public static Owner fromRepository(OwnerID ownerID, UserID userID, Voen voen, LocalDateTime createdAt) {
        return new Owner(ownerID, userID, voen, createdAt);
    }

    public OwnerID id() {
        return id;
    }

    public UserID userID() {
        return userID;
    }

    public Voen voen() {
        return voen;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Owner owner = (Owner) o;
        return Objects.equals(id, owner.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
