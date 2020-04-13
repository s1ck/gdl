package org.s1ck.gdl.model.comparables.time;

import org.s1ck.gdl.model.comparables.ComparableExpression;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.utils.Comparator;

import java.util.ArrayList;

/**
 * Represents a timestamp
 */
public abstract class TimePoint implements ComparableExpression {

    /**
     * Used in evaluation methods to indicate that the value (in millis) of a TimePoint can
     * not be determined (yet).
     */
    public static final long UNDEFINED = -1L;

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

    /**
     * Translates a term "this comparator arg" into a Predicate over comparisons.
     * E.g. "Max(p1,p2) > x" would translate to "(p1 > x) AND (p2 > x)"
     * Used to replace complex TimePoints like MAX, MIN by Comparisons of simple TimePoints
     * @param comparator the comparator of the term to be translated
     * @param arg the second argument of the term to be translated (first argument is this)
     * @return the translated term
     */
    public Predicate unfoldComparison(Comparator comparator, TimePoint arg){
        if(comparator == Comparator.EQ){
            return unfoldEQ(arg);
        }
        else if(comparator == Comparator.NEQ){
            return unfoldNEQ(arg);
        }
        else if(comparator == Comparator.GT){
            return unfoldGT(arg);
        }
        else if(comparator == Comparator.GTE){
            return unfoldGTE(arg);
        }
        else if(comparator == Comparator.LT){
            return unfoldLT(arg);
        }
        else if(comparator == Comparator.LTE){
            return unfoldLTE(arg);
        }
        return null;
    }

    /**
     * Unfolds a comparison "this == arg":
     * @param arg the second argument in the comparison to unfold
     * @return unfolded comparison
     */
    protected abstract Predicate unfoldEQ(TimePoint arg);

    /**
     * Unfolds a comparison "this <> arg":
     * @param arg the second argument in the comparison to unfold
     * @return unfolded comparison
     */
    protected abstract Predicate unfoldNEQ(TimePoint arg);

    /**
     * Unfolds a comparison "this > arg":
     * @param arg the second argument in the comparison to unfold
     * @return unfolded comparison
     */
    protected abstract Predicate unfoldGT(TimePoint arg);

    /**
     * Unfolds a comparison "this >= arg":
     * @param arg the second argument in the comparison to unfold
     * @return unfolded comparison
     */
    protected abstract Predicate unfoldGTE(TimePoint arg);

    /**
     * Unfolds a comparison "this < arg":
     * @param arg the second argument in the comparison to unfold
     * @return unfolded comparison
     */
    protected abstract Predicate unfoldLT(TimePoint arg);

    /**
     * Unfolds a comparison "this <= arg":
     * @param arg the second argument in the comparison to unfold
     * @return unfolded comparison
     */
    protected abstract Predicate unfoldLTE(TimePoint arg);

}
