package org.s1ck.gdl.utils;

import java.util.Optional;
import java.util.function.Function;

/**
 * Generates identifiers in a continuous fashion.
 */
public class ContinuousId implements Function<Optional<String>, Long> {
    private long nextId = 0L;

    @Override
    public Long apply(Optional<String> s) {
        return nextId++;
    }
}
