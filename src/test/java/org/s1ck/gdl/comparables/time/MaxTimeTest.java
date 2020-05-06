package org.s1ck.gdl.comparables.time;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.*;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import static org.junit.Assert.*;

public class MaxTimeTest {

    @Test
    public void simpleMaxTimeTest(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeLiteral l5 = new TimeLiteral("2019-04-05T01:02:31");
        MaxTimePoint mx = new MaxTimePoint(l1,l2,l3,l4,l5);
        assertEquals(mx.evaluate(), l5.evaluate());
        assertEquals(mx.getLowerBound(), mx.getUpperBound());
        assertEquals(mx.getUpperBound(), mx.evaluate());
    }

    @Test
    public void mixedMaxTimeTest(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeSelector l4 = new TimeSelector("v", TimeSelector.TimeField.VAL_TO);
        MaxTimePoint mx = new MaxTimePoint(l1,l2,l3,l4);
        assertEquals(mx.evaluate(),-1);
        assertEquals(mx.getLowerBound(), l2.evaluate());
        assertEquals(mx.getUpperBound(), Long.MAX_VALUE);

        // only selectors
        TimeSelector l5 = new TimeSelector("x",TimeSelector.TimeField.VAL_FROM);
        mx = new MaxTimePoint(l4,l5);
        assertEquals(mx.evaluate(),-1);
        assertEquals(mx.getLowerBound(), 0);
        assertEquals(mx.getUpperBound(), Long.MAX_VALUE);
    }

    @Test
    public void complexMaxTest(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12T08:00");
        TimeSelector s1 = new TimeSelector("p", TimeSelector.TimeField.VAL_TO);
        MaxTimePoint mx1 = new MaxTimePoint(l1,s1);

        TimeLiteral l2 = new TimeLiteral("2005-08-12T17:21:43");
        TimeSelector s2 = new TimeSelector("q", TimeSelector.TimeField.TX_FROM);
        MaxTimePoint mx2 = new MaxTimePoint(s2,l2);

        MaxTimePoint mx3 = new MaxTimePoint(mx1, mx2);
        assertEquals(mx3.evaluate(),-1);
        assertEquals(mx3.getLowerBound(), mx1.getLowerBound());
        assertEquals(mx3.getUpperBound(), Long.MAX_VALUE);
    }


    @Test
    public void maxMinTest1(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        MinTimePoint mn1 = new MinTimePoint(l1,l2);
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeLiteral l5 = new TimeLiteral("2019-04-05T01:02:31");
        MinTimePoint mn2 = new MinTimePoint(l3,l4,l5);
        MaxTimePoint mx = new MaxTimePoint(mn1,mn2);
        assertEquals(mx.evaluate(), l1.evaluate());
    }

    @Test
    public void maxMinTest2(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        MinTimePoint mn1 = new MinTimePoint(l1,l2);
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeLiteral l5 = new TimeLiteral("2019-04-05T01:02:31");
        MaxTimePoint mx1 = new MaxTimePoint(l3,l4,l5);
        MaxTimePoint mx = new MaxTimePoint(mn1,mx1);
        assertEquals(mx.evaluate(), l5.evaluate());
    }

    @Test
    public void maxMinTest3(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        TimeSelector s1 = new TimeSelector("s", TimeSelector.TimeField.TX_FROM);
        MinTimePoint mn1 = new MinTimePoint(l1,l2,s1);
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeSelector s2 = new TimeSelector("t", TimeSelector.TimeField.VAL_TO);
        MinTimePoint mn2 = new MinTimePoint(l3,l4,s2);
        MaxTimePoint mx = new MaxTimePoint(mn1,mn2);
        assertEquals(mx.evaluate(), -1);
        assertEquals(mx.getLowerBound(), 0);
        assertEquals(mx.getUpperBound(), l1.evaluate());
    }

    @Test
    public void maxMinTest4(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        TimeSelector s1 = new TimeSelector("s", TimeSelector.TimeField.TX_FROM);
        MinTimePoint mn1 = new MinTimePoint(l1,l2,s1);
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeLiteral l5 = new TimeLiteral("2019-04-05T01:02:31");
        TimeSelector s2 = new TimeSelector("t", TimeSelector.TimeField.VAL_TO);
        MaxTimePoint mx1 = new MaxTimePoint(l3,l4,l5,s2);
        MaxTimePoint mx = new MaxTimePoint(mn1,mx1);
        assertEquals(mx.evaluate(), -1);
        assertEquals(mx.getUpperBound(), Long.MAX_VALUE);
        assertEquals(mx.getLowerBound(), l5.evaluate());
    }

    @Test
    public void unfoldEQTest(){
        TimeLiteral p1 = new TimeLiteral("2020-04-10T12:30:00");
        TimeSelector p2 = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeLiteral p3 = new TimeLiteral("1970-01-01T01:01:01");
        MaxTimePoint mn = new MaxTimePoint(p1, p2, p3);
        TimeLiteral arg = new TimeLiteral("2020-04-10T12:28:45");

        Predicate expected =
                new Or(
                        new Or(
                                new And(
                                        new And(
                                                new Comparison(p1, Comparator.EQ, arg),
                                                new Comparison(p1, Comparator.GTE, p2)
                                        ),
                                        new Comparison(p1, Comparator.GTE, p3)
                                ),
                                new And(
                                        new And(
                                                new Comparison(p2, Comparator.EQ, arg),
                                                new Comparison(p2, Comparator.GTE, p1)
                                        ),
                                        new Comparison(p2, Comparator.GTE, p3)
                                )
                        ),
                        new And(
                                new And(
                                        new Comparison(p3, Comparator.EQ, arg),
                                        new Comparison(p3, Comparator.GTE, p1)
                                ),
                                new Comparison(p3, Comparator.GTE, p2)
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
        MaxTimePoint mn = new MaxTimePoint(p1, p2, p3);
        TimeLiteral arg = new TimeLiteral("2020-04-10T12:28:45");

        Predicate expected =
                new And(
                        new And(
                                new Or(
                                        new Or(
                                                new Comparison(p1, Comparator.NEQ, arg),
                                                new Comparison(p1, Comparator.LT, p2)
                                        ),
                                        new Comparison(p1, Comparator.LT, p3)
                                ),
                                new Or(
                                        new Or(
                                                new Comparison(p2, Comparator.NEQ, arg),
                                                new Comparison(p2, Comparator.LT, p1)
                                        ),
                                        new Comparison(p2, Comparator.LT, p3)
                                )
                        ),
                        new Or(
                                new Or(
                                        new Comparison(p3, Comparator.NEQ, arg),
                                        new Comparison(p3, Comparator.LT, p1)
                                ),
                                new Comparison(p3, Comparator.LT,p2)
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
        MaxTimePoint mn = new MaxTimePoint(p1, p2, p3);
        TimeLiteral arg = new TimeLiteral("2020-04-10T12:28:45");

        Predicate expected =
                new Or(
                        new Or(
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
        MaxTimePoint mn = new MaxTimePoint(p1, p2, p3);
        TimeLiteral arg = new TimeLiteral("2020-04-10T12:28:45");

        Predicate expected =
                new Or(
                        new Or(
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
        MaxTimePoint mn = new MaxTimePoint(p1, p2, p3);
        TimeLiteral arg = new TimeLiteral("2020-04-10T12:28:45");

        Predicate expected =
                new And(
                        new And(
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
        MaxTimePoint mn = new MaxTimePoint(p1, p2, p3);
        TimeLiteral arg = new TimeLiteral("2020-04-10T12:28:45");

        Predicate expected =
                new And(
                        new And(
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
        MaxTimePoint m1 = new MaxTimePoint(s1,l);
        assertFalse(m1.containsSelectorType(TimeSelector.TimeField.TX_TO));

        TimeSelector s2 = new TimeSelector("a", "tx_to");
        MaxTimePoint m2 = new MaxTimePoint(s2,l);
        assertTrue(m2.containsSelectorType(TimeSelector.TimeField.TX_TO));
    }

    @Test
    public void globalTest(){
        TimeSelector s1 = new TimeSelector("a", "val_to");
        TimeLiteral l = new TimeLiteral("2020-04-28");
        TimeSelector global = new TimeSelector(TimeSelector.GLOBAL_SELECTOR, TimeSelector.TimeField.VAL_TO);

        assertTrue(new MaxTimePoint(s1, l, global).isGlobal());
        assertFalse(new MaxTimePoint(s1,l,l,s1).isGlobal());
    }
}
