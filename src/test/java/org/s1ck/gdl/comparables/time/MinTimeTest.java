package org.s1ck.gdl.comparables.time;

import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.MaxTimePoint;
import org.s1ck.gdl.model.comparables.time.MinTimePoint;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;
import org.s1ck.gdl.model.comparables.time.TimeSelector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
}
