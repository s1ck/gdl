package org.s1ck.gdl.comparables.time;

import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.MaxTimePoint;
import org.s1ck.gdl.model.comparables.time.MinTimePoint;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;
import org.s1ck.gdl.model.comparables.time.TimeSelector;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.booleans.Not;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import static org.junit.Assert.*;

public class MinTimeTest {

    @Test
    public void simpleMinTimeTest(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeLiteral l5 = new TimeLiteral("2019-04-05T01:02:31");
        MinTimePoint mn = new MinTimePoint(l1,l2,l3,l4,l5);
        assertEquals(mn.evaluate(), l3.evaluate());
        assertEquals(mn.getLowerBound(), mn.getUpperBound());
        assertEquals(mn.getUpperBound(), mn.evaluate());
    }

    @Test
    public void mixedMinTimeTest(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeSelector l4 = new TimeSelector("v", TimeSelector.TimeField.VAL_TO);
        MinTimePoint mn = new MinTimePoint(l1,l2,l3,l4);
        assertEquals(mn.evaluate(),-1);
        assertEquals(mn.getLowerBound(), 0);
        assertEquals(mn.getUpperBound(), l3.evaluate());

        // only selectors
        TimeSelector l5 = new TimeSelector("x",TimeSelector.TimeField.VAL_FROM);
        mn = new MinTimePoint(l4,l5);
        assertEquals(mn.evaluate(),-1);
        assertEquals(mn.getLowerBound(), 0);
        assertEquals(mn.getUpperBound(), Long.MAX_VALUE);
    }

    @Test
    public void complexMinTest(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12T08:00");
        TimeSelector s1 = new TimeSelector("p", TimeSelector.TimeField.VAL_TO);
        MinTimePoint mn1 = new MinTimePoint(l1,s1);

        TimeLiteral l2 = new TimeLiteral("2005-08-12T17:21:43");
        TimeSelector s2 = new TimeSelector("q", TimeSelector.TimeField.TX_FROM);
        MinTimePoint mn2 = new MinTimePoint(s2,l2);

        MinTimePoint mn3 = new MinTimePoint(mn1, mn2);
        assertEquals(mn3.evaluate(),-1);
        assertEquals(mn3.getLowerBound(), 0);
        assertEquals(mn3.getUpperBound(), l2.evaluate());

        assertEquals(mn3.getVariables().size(),2);
        assertTrue(mn3.getVariables().contains("q"));
        assertTrue(mn3.getVariables().contains("p"));
    }

    @Test
    public void minMaxTest1(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        MaxTimePoint mx1 = new MaxTimePoint(l1,l2);
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeLiteral l5 = new TimeLiteral("2019-04-05T01:02:31");
        MaxTimePoint mx2 = new MaxTimePoint(l3,l4,l5);
        MinTimePoint mn = new MinTimePoint(mx1,mx2);
        assertEquals(mn.evaluate(), l2.evaluate());
    }

    @Test
    public void minMaxTest2(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        MaxTimePoint mx1 = new MaxTimePoint(l1,l2);
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeLiteral l5 = new TimeLiteral("2019-04-05T01:02:31");
        MinTimePoint mn1 = new MinTimePoint(l3,l4,l5);
        MinTimePoint mn = new MinTimePoint(mx1,mn1);
        assertEquals(mn.evaluate(), l3.evaluate());
    }

    @Test
    public void minMaxTest3(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        TimeSelector s1 = new TimeSelector("s", TimeSelector.TimeField.TX_FROM);
        MaxTimePoint mx1 = new MaxTimePoint(l1,l2,s1);
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeSelector s2 = new TimeSelector("t", TimeSelector.TimeField.VAL_TO);
        MaxTimePoint mx2 = new MaxTimePoint(l3,l4,s2);
        MinTimePoint mn = new MinTimePoint(mx1,mx2);
        assertEquals(mn.evaluate(), -1);
        assertEquals(mn.getLowerBound(), l2.evaluate());
        assertEquals(mn.getUpperBound(), Long.MAX_VALUE);
    }

    @Test
    public void minMaxTest4(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        TimeSelector s1 = new TimeSelector("s", TimeSelector.TimeField.TX_FROM);
        MinTimePoint mn1 = new MinTimePoint(l1,l2,s1);
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeLiteral l5 = new TimeLiteral("2019-04-05T01:02:31");
        TimeSelector s2 = new TimeSelector("t", TimeSelector.TimeField.VAL_TO);
        MaxTimePoint mx1 = new MaxTimePoint(l3,l4,l5,s2);
        MinTimePoint mn = new MinTimePoint(mn1,mx1);
        assertEquals(mn.evaluate(), -1);
        assertEquals(mn.getUpperBound(), l1.evaluate());
        assertEquals(mn.getLowerBound(), 0);
    }

    @Test
    public void unfoldEQTest(){
        TimeLiteral p1 = new TimeLiteral("2020-04-10T12:30:00");
        TimeSelector p2 = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeLiteral p3 = new TimeLiteral("1970-01-01T01:01:01");
        MinTimePoint mn = new MinTimePoint(p1, p2, p3);
        TimeLiteral arg = new TimeLiteral("2020-04-10T12:28:45");

        Predicate expected =
                new Or(
                        new Or(
                                new And(
                                        new And(
                                                new Comparison(p1, Comparator.EQ, arg),
                                                new Comparison(p1, Comparator.LTE, p2)
                                        ),
                                        new Comparison(p1, Comparator.LTE, p3)
                                ),
                                new And(
                                        new And(
                                                new Comparison(p2, Comparator.EQ, arg),
                                                new Comparison(p2, Comparator.LTE, p1)
                                        ),
                                        new Comparison(p2, Comparator.LTE, p3)
                                )
                        ),
                        new And(
                                new And(
                                        new Comparison(p3, Comparator.EQ, arg),
                                        new Comparison(p3, Comparator.LTE, p1)
                                ),
                                new Comparison(p3, Comparator.LTE, p2)
                        )
                );

        Predicate unfolded = mn.unfoldComparison(Comparator.EQ, arg);
        System.out.println(unfolded);
        assertEquals(expected, unfolded);
    }

    @Test
    public void unfoldNEQTest(){
        TimeLiteral p1 = new TimeLiteral("2020-04-10T12:30:00");
        TimeSelector p2 = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeLiteral p3 = new TimeLiteral("1970-01-01T01:01:01");
        MinTimePoint mn = new MinTimePoint(p1, p2, p3);
        TimeLiteral arg = new TimeLiteral("2020-04-10T12:28:45");

        Predicate expected =
                new And(
                        new And(
                                new Or(
                                        new Or(
                                                new Comparison(p1, Comparator.NEQ, arg),
                                                new Comparison(p1, Comparator.GT, p2)
                                        ),
                                        new Comparison(p1, Comparator.GT, p3)
                                ),
                                new Or(
                                        new Or(
                                                new Comparison(p2, Comparator.NEQ, arg),
                                                new Comparison(p2, Comparator.GT, p1)
                                        ),
                                        new Comparison(p2, Comparator.GT, p3)
                                )
                        ),
                        new Or(
                                new Or(
                                        new Comparison(p3, Comparator.NEQ, arg),
                                        new Comparison(p3, Comparator.GT, p1)
                                ),
                                new Comparison(p3, Comparator.GT,p2)
                        )
                );

        Predicate unfolded = mn.unfoldComparison(Comparator.NEQ, arg);
        System.out.println(unfolded);
        assertEquals(expected, unfolded);
    }

    @Test
    public void unfoldGTTest(){
        TimeLiteral p1 = new TimeLiteral("2020-04-10T12:30:00");
        TimeSelector p2 = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeLiteral p3 = new TimeLiteral("1970-01-01T01:01:01");
        MinTimePoint mn = new MinTimePoint(p1, p2, p3);
        TimeLiteral arg = new TimeLiteral("2020-04-10T12:28:45");

        Predicate expected =
                new And(
                        new And(
                                new Comparison(p1, Comparator.GT, arg),
                                new Comparison(p2, Comparator.GT, arg)
                        ),
                        new Comparison(p3, Comparator.GT, arg)
                );

        Predicate unfolded = mn.unfoldComparison(Comparator.GT, arg);
        System.out.println(unfolded);
        assertEquals(expected, unfolded);
    }

    @Test
    public void unfoldGTETest(){
        TimeLiteral p1 = new TimeLiteral("2020-04-10T12:30:00");
        TimeSelector p2 = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeLiteral p3 = new TimeLiteral("1970-01-01T01:01:01");
        MinTimePoint mn = new MinTimePoint(p1, p2, p3);
        TimeLiteral arg = new TimeLiteral("2020-04-10T12:28:45");

        Predicate expected =
                new And(
                        new And(
                                new Comparison(p1, Comparator.GTE, arg),
                                new Comparison(p2, Comparator.GTE, arg)
                        ),
                        new Comparison(p3, Comparator.GTE, arg)
                );

        Predicate unfolded = mn.unfoldComparison(Comparator.GTE, arg);
        System.out.println(unfolded);
        assertEquals(expected, unfolded);
    }

    @Test
    public void unfoldLTTest(){
        TimeLiteral p1 = new TimeLiteral("2020-04-10T12:30:00");
        TimeSelector p2 = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeLiteral p3 = new TimeLiteral("1970-01-01T01:01:01");
        MinTimePoint mn = new MinTimePoint(p1, p2, p3);
        TimeLiteral arg = new TimeLiteral("2020-04-10T12:28:45");

        Predicate expected =
                new Or(
                        new Or(
                                new Comparison(p1, Comparator.LT, arg),
                                new Comparison(p2, Comparator.LT, arg)
                        ),
                        new Comparison(p3, Comparator.LT, arg)
                );

        Predicate unfolded = mn.unfoldComparison(Comparator.LT, arg);
        System.out.println(unfolded);
        assertEquals(expected, unfolded);
    }

    @Test
    public void unfoldLTETest(){
        TimeLiteral p1 = new TimeLiteral("2020-04-10T12:30:00");
        TimeSelector p2 = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeLiteral p3 = new TimeLiteral("1970-01-01T01:01:01");
        MinTimePoint mn = new MinTimePoint(p1, p2, p3);
        TimeLiteral arg = new TimeLiteral("2020-04-10T12:28:45");

        Predicate expected =
                new Or(
                        new Or(
                                new Comparison(p1, Comparator.LTE, arg),
                                new Comparison(p2, Comparator.LTE, arg)
                        ),
                        new Comparison(p3, Comparator.LTE, arg)
                );

        Predicate unfolded = mn.unfoldComparison(Comparator.LTE, arg);
        System.out.println(unfolded);
        assertEquals(expected, unfolded);
    }

    @Test
    public void containsTxToTest(){
        TimeSelector s1 = new TimeSelector("a", "val_to");
        TimeLiteral l = new TimeLiteral("2020-04-28");
        MinTimePoint m1 = new MinTimePoint(s1,l);
        assertFalse(m1.containsSelectorType(TimeSelector.TimeField.TX_TO));

        TimeSelector s2 = new TimeSelector("a", "tx_to");
        MinTimePoint m2 = new MinTimePoint(s2,l);
        assertTrue(m2.containsSelectorType(TimeSelector.TimeField.TX_TO));
    }




}
