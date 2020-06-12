package org.s1ck.gdl.model.comparables.time;

import org.s1ck.gdl.model.comparables.ComparableExpression;
import org.s1ck.gdl.model.predicates.Predicate;
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
}
