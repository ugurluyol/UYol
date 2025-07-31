package org.project.domain.user.factories;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;
import org.project.domain.user.value_objects.Email;
import org.project.domain.user.value_objects.Identifier;
import org.project.domain.user.value_objects.Phone;

public final class IdentifierFactory {

  public static Identifier from(String raw) {
    try {
      return new Email(raw);
    } catch (IllegalDomainArgumentException ignored) {
    }

    try {
      return new Phone(raw);
    } catch (IllegalDomainArgumentException ignored) {
    }

    throw new IllegalDomainArgumentException("Unknown identifier format");

  }
}
