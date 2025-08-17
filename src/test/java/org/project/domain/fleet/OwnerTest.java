package org.project.domain.fleet;

import org.junit.jupiter.api.Test;
import org.project.domain.fleet.entities.Owner;
import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import org.project.domain.shared.value_objects.OwnerID;
import org.project.domain.shared.value_objects.UserID;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.project.features.util.TestDataGenerator.*;

class OwnerTest {

    @Test
    void shouldCreateOwnerWithOfMethod() {
        var userID = UserID.newID();
        var voen = voen();

        var owner = Owner.of(userID, voen);

        assertNotNull(owner.id());
        assertEquals(userID, owner.userID());
        assertEquals(voen, owner.voen());
        assertNotNull(owner.createdAt());
    }

    @Test
    void shouldCreateOwnerFromRepository() {
        var id = new OwnerID(UUID.randomUUID());
        var userID = UserID.newID();
        var voen = voen();
        var createdAt = LocalDateTime.now();

        var owner = Owner.fromRepository(id, userID, voen, createdAt);

        assertEquals(id, owner.id());
        assertEquals(userID, owner.userID());
        assertEquals(voen, owner.voen());
        assertEquals(createdAt, owner.createdAt());
    }

    @Test
    void shouldThrowWhenUserIdIsNull() {
        assertThrows(IllegalDomainArgumentException.class,
                () -> Owner.of(null, voen()));
    }

    @Test
    void shouldThrowWhenVoenIsNull() {
        assertThrows(IllegalDomainArgumentException.class,
                () -> Owner.of(UserID.newID(), null));
    }

    @Test
    void shouldBeEqualWhenIdIsSame() {
        var id = new OwnerID(UUID.randomUUID());
        var o1 = Owner.fromRepository(id, UserID.newID(), voen(), LocalDateTime.now());
        var o2 = Owner.fromRepository(id, UserID.newID(), voen(), LocalDateTime.now().minusDays(1));

        assertEquals(o1, o2);
        assertEquals(o1.hashCode(), o2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenIdIsDifferent() {
        var o1 = owner();
        var o2 = owner();

        assertNotEquals(o1, o2);
    }
}
