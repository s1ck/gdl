package org.s1ck.gdl.utils;

import java.util.function.Supplier;

/**
 * Generates identifiers in a continuous fashion.
 */
public class ContinuousId implements Supplier<Long> {
    private long nextId = 0L;

    @Override
    public Long get() {
        return nextId++;
    }
}
