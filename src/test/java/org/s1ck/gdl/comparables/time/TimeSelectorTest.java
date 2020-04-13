package org.s1ck.gdl.comparables.time;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.TimeSelector;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import static org.junit.Assert.assertEquals;

public class TimeSelectorTest {

    @Test
    public void selectorTest(){
        TimeSelector selector = new TimeSelector("var", TimeSelector.TimeField.TX_FROM);
        assertEquals(selector.getVariable(), "var");
        assertEquals(selector.getVariables().size(), 1);
        assertEquals(selector.getVariables().get(0), "var");
        assertEquals(selector.getLowerBound(), 0);
        assertEquals(selector.getUpperBound(), Long.MAX_VALUE);
        assertEquals(selector.getTimeProp(), TimeSelector.TimeField.TX_FROM);
    }

    @Test
    public void unfoldPredicateTest(){
        TimeSelector selector = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeSelector s2 = new TimeSelector("x", TimeSelector.TimeField.VAL_TO);
        //expected values
        Comparison cEq = new Comparison(selector, Comparator.EQ, s2);
        Comparison cNeq = new Comparison(selector, Comparator.NEQ, s2);
        Comparison cGt = new Comparison(selector, Comparator.GT, s2);
        Comparison cGte = new Comparison(selector, Comparator.GTE, s2);
        Comparison cLt = new Comparison(selector, Comparator.LT, s2);
        Comparison cLte = new Comparison(selector, Comparator.LTE, s2);

        assertEquals(selector.unfoldComparison(Comparator.EQ, s2), cEq);
        assertEquals(selector.unfoldComparison(Comparator.NEQ, s2), cNeq);
        assertEquals(selector.unfoldComparison(Comparator.GT, s2), cGt);
        assertEquals(selector.unfoldComparison(Comparator.GTE, s2), cGte);
        assertEquals(selector.unfoldComparison(Comparator.LT, s2), cLt);
        assertEquals(selector.unfoldComparison(Comparator.LTE, s2), cLte);

    }
}
