package org.s1ck.gdl.model.comparables.time;

import org.s1ck.gdl.model.comparables.ComparableExpression;

import java.util.Optional;

/**
 * Represents a timestamp
 */
public abstract class TimePoint implements ComparableExpression {

    /**
     * calculates the value of the timestamp (UNIX epoch long), if possible.
     * E.g., a timestamp like v.VAL_FROM can not be assigned a unique long value
     * @return UNIX epoch long, -1 if it can not be determined
     */
    public abstract Optional<Long> evaluate();

}
