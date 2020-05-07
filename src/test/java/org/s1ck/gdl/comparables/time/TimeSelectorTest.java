package org.s1ck.gdl.comparables.time;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.MaxTimePoint;
import org.s1ck.gdl.model.comparables.time.MinTimePoint;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;
import org.s1ck.gdl.model.comparables.time.TimeSelector;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.s1ck.gdl.utils.Comparator.*;

public class TimeSelectorTest {

    @Test
    public void selectorTest(){
        TimeSelector selector = new TimeSelector("var", TimeSelector.TimeField.TX_FROM);
        assertEquals(new ArrayList<String>(selector.getVariables()).get(0), "var");
        assertEquals(selector.getVariables().size(), 1);
        assertEquals(new ArrayList<String>(selector.getVariables()).get(0), "var");
        assertEquals(selector.getLowerBound(), Long.MIN_VALUE);
        assertEquals(selector.getUpperBound(), Long.MAX_VALUE);
        assertFalse(selector.evaluate().isPresent());
        assertEquals(selector.getTimeProp(), TimeSelector.TimeField.TX_FROM);
    }

    @Test
    public void unfoldPredicateTest(){
        TimeSelector selector = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeSelector s2 = new TimeSelector("x", TimeSelector.TimeField.VAL_TO);
        //expected values
        Comparison cEq = new Comparison(selector, EQ, s2);
        Comparison cNeq = new Comparison(selector, Comparator.NEQ, s2);
        Comparison cGt = new Comparison(selector, Comparator.GT, s2);
        Comparison cGte = new Comparison(selector, Comparator.GTE, s2);
        Comparison cLt = new Comparison(selector, Comparator.LT, s2);
        Comparison cLte = new Comparison(selector, Comparator.LTE, s2);

        assertEquals(selector.unfoldComparison(EQ, s2), cEq);
        assertEquals(selector.unfoldComparison(Comparator.NEQ, s2), cNeq);
        assertEquals(selector.unfoldComparison(Comparator.GT, s2), cGt);
        assertEquals(selector.unfoldComparison(Comparator.GTE, s2), cGte);
        assertEquals(selector.unfoldComparison(Comparator.LT, s2), cLt);
        assertEquals(selector.unfoldComparison(Comparator.LTE, s2), cLte);

    }

    @Test
    public void unfoldGlobalTest(){
        TimeSelector globalFrom = new TimeSelector(TimeSelector.TimeField.VAL_FROM);
        TimeSelector globalTo = new TimeSelector(TimeSelector.TimeField.TX_TO);
        ArrayList<String> variables = new ArrayList<>();
        variables.add("a");
        variables.add("b");
        variables.add("c");
        TimeSelector aFrom = new TimeSelector("a", TimeSelector.TimeField.VAL_FROM);
        TimeSelector aTo = new TimeSelector("a", TimeSelector.TimeField.TX_TO);
        TimeSelector bFrom = new TimeSelector("b", TimeSelector.TimeField.VAL_FROM);
        TimeSelector bTo = new TimeSelector("b", TimeSelector.TimeField.TX_TO);
        TimeSelector cFrom = new TimeSelector("c", TimeSelector.TimeField.VAL_FROM);
        TimeSelector cTo = new TimeSelector("c", TimeSelector.TimeField.TX_TO);
        TimeLiteral rhs = new TimeLiteral();

        //expected values
        And expectedEqFrom = new And(
                new Or(
                        new Or(
                            new Comparison(aFrom, EQ, rhs),
                            new Comparison(bFrom, EQ, rhs)
                        ),
                        new Comparison(cFrom, EQ, rhs)
                ),
                new And(
                        new And(
                            new Comparison(aFrom, LTE, rhs),
                            new Comparison(bFrom, LTE, rhs)
                        ),
                        new Comparison(cFrom, LTE, rhs)
                )
        );
        And expectedEqTo = new And(
                new Or(
                        new Or(
                            new Comparison(aTo, EQ, rhs),
                            new Comparison(bTo, EQ, rhs)
                        ),
                        new Comparison(cTo, EQ, rhs)
                ),
                new And(
                        new And(
                            new Comparison(aTo, GTE, rhs),
                            new Comparison(bTo, GTE, rhs)
                        ),
                        new Comparison(cTo, GTE, rhs)
                )
        );

        And expectedNeqFrom = new And(
                new And(
                        new Comparison(aFrom, NEQ, rhs),
                        new Comparison(bFrom, NEQ, rhs)
                ),
                new Comparison(cFrom, NEQ, rhs)
        );

        And expectedNeqTo = new And(
                new And(
                        new Comparison(aTo, NEQ, rhs),
                        new Comparison(bTo, NEQ, rhs)
                ),
                new Comparison(cTo, NEQ, rhs)
        );

        Or expectedGtFrom = new Or(
                new Or(
                        new Comparison(aFrom, GT, rhs),
                        new Comparison(bFrom, GT, rhs)
                ),
                new Comparison(cFrom, GT, rhs)
        );

        And expectedGtTo = new And(
                new And(
                        new Comparison(aTo, GT, rhs),
                        new Comparison(bTo, GT, rhs)
                ),
                new Comparison(cTo, GT, rhs)
        );

        Or expectedGteFrom = new Or(
                new Or(
                        new Comparison(aFrom, GTE, rhs),
                        new Comparison(bFrom, GTE, rhs)
                ),
                new Comparison(cFrom, GTE, rhs)
        );

        And expectedGteTo = new And(
                new And(
                        new Comparison(aTo, GTE, rhs),
                        new Comparison(bTo, GTE, rhs)
                ),
                new Comparison(cTo, GTE, rhs)
        );

        And expectedLtFrom = new And(
                new And(
                        new Comparison(aFrom, LT, rhs),
                        new Comparison(bFrom, LT, rhs)
                ),
                new Comparison(cFrom, LT, rhs)
        );

        Or expectedLtTo = new Or(
                new Or(
                        new Comparison(aTo, LT, rhs),
                        new Comparison(bTo, LT, rhs)
                ),
                new Comparison(cTo, LT, rhs)
        );

        And expectedLteFrom = new And(
                new And(
                        new Comparison(aFrom, LTE, rhs),
                        new Comparison(bFrom, LTE, rhs)
                ),
                new Comparison(cFrom, LTE, rhs)
        );

        Or expectedLteTo = new Or(
                new Or(
                        new Comparison(aTo, LTE, rhs),
                        new Comparison(bTo, LTE, rhs)
                ),
                new Comparison(cTo, LTE, rhs)
        );



        Predicate resultEqFrom = globalFrom.unfoldGlobal(EQ, rhs, variables);
        Predicate resultEqTo = globalTo.unfoldGlobal(EQ, rhs, variables);
        Predicate resultNeqFrom = globalFrom.unfoldGlobal(NEQ, rhs, variables);
        Predicate resultNeqTo = globalTo.unfoldGlobal(NEQ, rhs, variables);
        Predicate resultGtFrom = globalFrom.unfoldGlobal(GT, rhs, variables);
        Predicate resultGtTo = globalTo.unfoldGlobal(GT, rhs, variables);
        Predicate resultGteFrom = globalFrom.unfoldGlobal(GTE, rhs, variables);
        Predicate resultGteTo = globalTo.unfoldGlobal(GTE, rhs, variables);
        Predicate resultLtFrom = globalFrom.unfoldGlobal(LT, rhs, variables);
        Predicate resultLtTo = globalTo.unfoldGlobal(LT, rhs, variables);
        Predicate resultLteFrom = globalFrom.unfoldGlobal(LTE, rhs, variables);
        Predicate resultLteTo = globalTo.unfoldGlobal(LTE, rhs, variables);


        assertEquals(expectedEqFrom, resultEqFrom);
        assertEquals(expectedEqTo, resultEqTo);
        assertEquals(expectedNeqFrom, resultNeqFrom);
        assertEquals(expectedNeqTo, resultNeqTo);
        assertEquals(expectedGtFrom, resultGtFrom);
        assertEquals(expectedGtTo, resultGtTo);
        assertEquals(expectedGteFrom, resultGteFrom);
        assertEquals(expectedGteTo, resultGteTo);
        assertEquals(expectedLtFrom, resultLtFrom);
        assertEquals(expectedLtTo, resultLtTo);
        assertEquals(expectedLteFrom, resultLteFrom);
        assertEquals(expectedLteTo, resultLteTo);

    }

    @Test
    public void containsTxToTest(){
        TimeSelector valF = new TimeSelector("a", "val_from");
        assertFalse(valF.containsSelectorType(TimeSelector.TimeField.TX_TO));
        TimeSelector valT = new TimeSelector("a", "val_to");
        assertFalse(valT.containsSelectorType(TimeSelector.TimeField.TX_TO));
        TimeSelector txF = new TimeSelector("a", "tx_from");
        assertFalse(txF.containsSelectorType(TimeSelector.TimeField.TX_TO));
        TimeSelector txT = new TimeSelector("a", "tx_to");
        assertTrue(txT.containsSelectorType(TimeSelector.TimeField.TX_TO));
    }

    @Test
    public void globalTest(){
        TimeSelector valF = new TimeSelector("a", "val_from");
        assertFalse(valF.isGlobal());
        assertEquals(valF.replaceGlobalByLocal(new ArrayList<>(Arrays.asList("a"))),
                valF);
        TimeSelector global = new TimeSelector(TimeSelector.GLOBAL_SELECTOR, "val_to");
        ArrayList<String> vars = new ArrayList<>(Arrays.asList("a", "b", "e"));
        MinTimePoint expected = new MinTimePoint(
                new TimeSelector("a", TimeSelector.TimeField.VAL_TO),
                new TimeSelector("b", TimeSelector.TimeField.VAL_TO),
                new TimeSelector("e", TimeSelector.TimeField.VAL_TO)
        );
        assertEquals(expected, global.replaceGlobalByLocal(vars));

        TimeSelector global2 = new TimeSelector(TimeSelector.GLOBAL_SELECTOR, "tx_from");
        MaxTimePoint expected2 = new MaxTimePoint(
                new TimeSelector("a", TimeSelector.TimeField.TX_FROM),
                new TimeSelector("b", TimeSelector.TimeField.TX_FROM),
                new TimeSelector("e", TimeSelector.TimeField.TX_FROM)
        );
        assertEquals(expected2, global2.replaceGlobalByLocal(vars));
        // only one variable -> no max/min, but single selector
        ArrayList<String> singleVar = new ArrayList<>(Arrays.asList("a"));
        TimeSelector expectedSingle = new TimeSelector("a", "tx_from");
        assertEquals(global2.replaceGlobalByLocal(singleVar), expectedSingle);
        assertTrue(global.isGlobal());
    }

}
