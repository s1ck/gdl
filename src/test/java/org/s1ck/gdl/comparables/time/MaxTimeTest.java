package org.s1ck.gdl.comparables.time;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.*;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

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
}
