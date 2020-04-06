package org.s1ck.gdl.model.comparables.time;

import org.s1ck.gdl.model.comparables.ComparableExpression;

import java.util.ArrayList;

/**
 * Represents a timestamp
 */
public abstract class TimePoint implements ComparableExpression {

    /**
     * Returns null, no variable involved in a timestamp by default
     * Not often used in implementations, as typically more than one variable can be involved
     * @return null
     */
    @Override
    public String getVariable() {
        return null;
    }

    /**
     * all variables involved in the expression
     * @return ArrayList of variable names (potentially empty)
     */
    public abstract ArrayList<String> getVariables();

    /**
     * calculates the value of the timestamp (UNIX epoch long), if possible.
     * E.g., a timestamp like v.VAL_FROM can not be assigned a unique long value
     * @return UNIX epoch long, -1 if it can not be determined
     */
    public abstract long evaluate();

    /**
     * calculates a lower bound for the timestamp (UNIX epoch long)
     * @return lower bound as UNIX epoch long
     */
    public abstract long getLowerBound();

    /**
     * calculates a upper bound for the timestamp (UNIX epoch long)
     * @return upper bound as UNIX epoch long
     */
    public abstract long getUpperBound();

}
