package org.s1ck.gdl.comparables.time;

import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.*;

import static org.junit.Assert.assertEquals;

public class PlusTimeTest {

    @Test
    public void simplePlusTest(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12T08:00");
        TimeLiteral l2 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l3 = new TimeLiteral("2019-04-05");

        PlusTimePoint p = new PlusTimePoint(l1,l2,l3);
        long sum = l1.getMilliseconds() + l2.getMilliseconds() + l3.getMilliseconds();
        assertEquals(p.evaluate(), sum);
        assertEquals(p.getLowerBound(), p.evaluate());
        assertEquals(p.getUpperBound(), p.evaluate());

        assertEquals(p.getVariables().size(),0);
    }

    @Test
    public void mixedPlusTest(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12T08:00");
        TimeLiteral l2 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l3 = new TimeLiteral("2019-04-05");
        TimeSelector s1 = new TimeSelector("p", TimeSelector.TimeField.VAL_FROM);

        PlusTimePoint p = new PlusTimePoint(l1, s1, l2,l3);
        long sum = l1.getMilliseconds() + l2.getMilliseconds() + l3.getMilliseconds();
        assertEquals(p.evaluate(), -1);
        assertEquals(p.getLowerBound(), sum);
        assertEquals(p.getUpperBound(), Long.MAX_VALUE);

        assertEquals(p.getVariables().size(),1);
        assertEquals(p.getVariables().get(0), "p");
    }

    @Test
    public void selectorPlusTest(){
        TimeSelector s1 = new TimeSelector("p", TimeSelector.TimeField.VAL_TO);
        TimeSelector s2 = new TimeSelector("q", TimeSelector.TimeField.TX_FROM);
        PlusTimePoint p = new PlusTimePoint(s1, s2);
        assertEquals(p.evaluate(), -1);
        assertEquals(p.getLowerBound(), 0);
        assertEquals(p.getUpperBound(), Long.MAX_VALUE);

    }

    @Test
    public void minMaxPlusTest(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        MaxTimePoint mx1 = new MaxTimePoint(l1,l2);
        TimeLiteral l3 = new TimeLiteral("2019-04-05");
        TimeLiteral l4 = new TimeLiteral("2019-04-05T01:02:31");
        MinTimePoint mn1 = new MinTimePoint(l3,l4);

        PlusTimePoint p = new PlusTimePoint(mx1, mn1);
        long sum = l2.getMilliseconds()+l3.getMilliseconds();
        assertEquals(p.evaluate(), sum);
        assertEquals(p.evaluate(), p.getLowerBound());
        assertEquals(p.evaluate(), p.getUpperBound());
    }
}
