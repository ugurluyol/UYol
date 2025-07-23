package org.project.domain.user.value_objects;

import org.project.domain.shared.exceptions.IllegalDomainArgumentException;

public record KeyAndCounter(String key, int counter) {
    public static final int MAX_SIZE = 28;

    public KeyAndCounter {
        if (key == null) throw new IllegalDomainArgumentException("Key can`t be null");
        if (key.length() > MAX_SIZE) throw new IllegalDomainArgumentException("Key is too long");
        if (counter < 0) throw new IllegalDomainArgumentException("Counter can`t be below zero");
    }
}