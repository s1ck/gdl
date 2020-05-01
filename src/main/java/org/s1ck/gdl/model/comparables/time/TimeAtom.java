package org.s1ck.gdl.model.comparables.time;

import org.s1ck.gdl.model.comparables.ComparableExpression;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import java.util.List;

/**
 * Base class for atoms in a {@link TimeTerm}, e.g. simple timestamps
 */
public abstract class TimeAtom extends TimePoint {


    /**
     * Translates a simple comparison described by {@code this comp rhs} to a equivalent
     * comparison that does not contain global time selectors / intervals anymore.
     * @param comp the comparator of the comparison
     * @param rhs the right hand side of the comparison
     * @param variables all the variables in the query
     * @return equivalent containing only local selectors/intervals
     */
    public abstract Predicate unfoldGlobal(Comparator comp, ComparableExpression rhs, List<String> variables);


    @Override
    public Predicate unfoldComparison(Comparator comparator, TimePoint arg){
        // nothing to unfold here
        return new Comparison(this, comparator, arg);
    }

    @Override
    protected Predicate unfoldEQ(TimePoint arg){
        // nothing to unfold here
        return new Comparison(this, Comparator.EQ, arg);
    }

    @Override
    protected Predicate unfoldNEQ(TimePoint arg){
        // nothing to unfold here
        return new Comparison(this, Comparator.NEQ, arg);
    }

    @Override
    protected Predicate unfoldGT(TimePoint arg){
        // nothing to unfold here
        return new Comparison(this, Comparator.GT, arg);
    }

    @Override
    protected Predicate unfoldGTE(TimePoint arg){
        // nothing to unfold here
        return new Comparison(this, Comparator.GTE, arg);
    }

    @Override
    protected Predicate unfoldLT(TimePoint arg){
        // nothing to unfold here
        return new Comparison(this, Comparator.LT, arg);
    }

    @Override
    protected Predicate unfoldLTE(TimePoint arg){
        // nothing to unfold here
        return new Comparison(this, Comparator.LTE, arg);
    }
}
