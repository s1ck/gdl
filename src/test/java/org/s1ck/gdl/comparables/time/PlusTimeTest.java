//package org.s1ck.gdl.comparables.time;
//
//import org.junit.Test;
//import org.s1ck.gdl.model.comparables.time.*;
//import org.s1ck.gdl.model.comparables.time.TimeConstant;
//import org.s1ck.gdl.model.predicates.expressions.Comparison;
//import org.s1ck.gdl.utils.Comparator;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//
//import static org.junit.Assert.*;
//
//public class PlusTimeTest {
//
//    @Test
//    public void simplePlusTest(){
//        TimeLiteral l1 = new TimeLiteral("2017-01-12T08:00");
//        TimeConstant c = new TimeConstant(1000);
//
//        PlusTimePoint p = new PlusTimePoint(l1,c);
//        long sum = l1.getMilliseconds() + c.getMillis();
//        assertEquals(p.evaluate(), sum);
//        assertEquals(p.getLowerBound(), p.evaluate());
//        assertEquals(p.getUpperBound(), p.evaluate());
//
//        assertEquals(p.getVariables().size(),0);
//    }
//
//    @Test
//    public void selectorPlusTest(){
//        TimeSelector s = new TimeSelector("p", TimeSelector.TimeField.VAL_FROM);
//        TimeConstant c = new TimeConstant(1234);
//        PlusTimePoint p = new PlusTimePoint(s,c);
//
//        assertEquals(p.evaluate(), TimePoint.UNDEFINED);
//        assertEquals(p.getLowerBound(), 1234);
//        assertEquals(p.getUpperBound(), Long.MAX_VALUE);
//
//        assertEquals(p.getVariables().size(),1);
//        assertEquals(new ArrayList<String>(p.getVariables()).get(0), "p");
//    }
//
//    @Test
//    public void maxPlusTest(){
//        TimeLiteral l1 = new TimeLiteral("2017-01-12");
//        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
//        MaxTimePoint mx1 = new MaxTimePoint(l1,l2);
//        TimeConstant c = new TimeConstant(1);
//
//        PlusTimePoint p = new PlusTimePoint(mx1, c);
//        assertEquals(p.evaluate(), l2.getMilliseconds()+1);
//        assertEquals(p.evaluate(), p.getLowerBound());
//        assertEquals(p.evaluate(), p.getUpperBound());
//
//        // now with undetermined maximum
//        TimeSelector s1 = new TimeSelector("x", "tx_to");
//        MaxTimePoint mx2 = new MaxTimePoint(l1, s1);
//        PlusTimePoint p2 = new PlusTimePoint(mx2, c);
//        assertEquals(p2.evaluate(), TimePoint.UNDEFINED);
//        assertEquals(p2.getLowerBound(), l1.getMilliseconds()+c.getMillis());
//        assertEquals(p2.getUpperBound(), Long.MAX_VALUE);
//    }
//
//    @Test
//    public void minPlusTest(){
//        TimeLiteral l1 = new TimeLiteral("2017-01-12");
//        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
//        MinTimePoint mn1 = new MinTimePoint(l1,l2);
//        TimeConstant c = new TimeConstant(1);
//
//        PlusTimePoint p = new PlusTimePoint(mn1, c);
//        assertEquals(p.evaluate(), l1.getMilliseconds()+1);
//        assertEquals(p.evaluate(), p.getLowerBound());
//        assertEquals(p.evaluate(), p.getUpperBound());
//
//        // now with undetermined minimum
//        TimeSelector s1 = new TimeSelector("x", "tx_to");
//        MinTimePoint mn2 = new MinTimePoint(l1, s1);
//        PlusTimePoint p2 = new PlusTimePoint(mn2, c);
//        assertEquals(p2.evaluate(), TimePoint.UNDEFINED);
//        assertEquals(p2.getLowerBound(), c.getMillis());
//        assertEquals(p2.getUpperBound(), l1.getMilliseconds()+c.getMillis());
//    }
//
//    @Test
//    public void unfoldPredicateTest(){
//        TimeLiteral lit = new TimeLiteral("2020-04-10T12:00:00");
//        TimeConstant c = new TimeConstant(1000);
//        PlusTimePoint plus = new PlusTimePoint(lit, c);
//        TimeSelector s = new TimeSelector("x", TimeSelector.TimeField.VAL_TO);
//        //expected values
//        Comparison cEq = new Comparison(plus, Comparator.EQ, s);
//        Comparison cNeq = new Comparison(plus, Comparator.NEQ, s);
//        Comparison cGt = new Comparison(plus, Comparator.GT, s);
//        Comparison cGte = new Comparison(plus, Comparator.GTE, s);
//        Comparison cLt = new Comparison(plus, Comparator.LT, s);
//        Comparison cLte = new Comparison(plus, Comparator.LTE, s);
//
//        assertEquals(plus.unfoldComparison(Comparator.EQ, s), cEq);
//        assertEquals(plus.unfoldComparison(Comparator.NEQ, s), cNeq);
//        assertEquals(plus.unfoldComparison(Comparator.GT, s), cGt);
//        assertEquals(plus.unfoldComparison(Comparator.GTE, s), cGte);
//        assertEquals(plus.unfoldComparison(Comparator.LT, s), cLt);
//        assertEquals(plus.unfoldComparison(Comparator.LTE, s), cLte);
//
//    }
//
//    @Test
//    public void containsTxToTest(){
//        TimeConstant c = new TimeConstant(1000);
//        TimeSelector s1 = new TimeSelector("a", "val_from");
//        PlusTimePoint p1 = new PlusTimePoint(s1, c);
//        assertFalse(p1.containsSelectorType(TimeSelector.TimeField.TX_TO));
//
//        TimeSelector s2 = new TimeSelector("a", "tx_to");
//        PlusTimePoint p2 = new PlusTimePoint(s2, c);
//        assertTrue(p2.containsSelectorType(TimeSelector.TimeField.TX_TO));maxPlusTest();
//    }
//
//    @Test
//    public void globalTest(){
//        TimeConstant c = new TimeConstant(1000);
//        TimeSelector s1 = new TimeSelector("a", "val_from");
//        TimeSelector global = new TimeSelector(TimeSelector.GLOBAL_SELECTOR, "val_to");
//
//        ArrayList<String> variables = new ArrayList<>(Arrays.asList("a","b"));
//
//        assertEquals(new PlusTimePoint(s1, c).replaceGlobalByLocal(variables),
//                new PlusTimePoint(s1,c));
//
//        PlusTimePoint expectedGlobal = new PlusTimePoint(
//                new MinTimePoint(
//                        new TimeSelector("a", TimeSelector.TimeField.VAL_TO),
//                        new TimeSelector("b", TimeSelector.TimeField.VAL_TO)
//                ),
//                c
//        );
//
//        assertEquals(new PlusTimePoint(global, c).replaceGlobalByLocal(variables),
//                expectedGlobal);
//
//        assertFalse(new PlusTimePoint(s1,c).isGlobal());
//        assertTrue(new PlusTimePoint(global, c).isGlobal());
//    }
//}
