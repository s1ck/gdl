package org.s1ck.gdl.comparables.time;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;

import static org.junit.Assert.assertEquals;

public class TimeLiteralTest {

    @Test
    public void millisInitTest(){
        TimeLiteral tl1 = new TimeLiteral(0);
        System.out.println(tl1.toString());
        assertEquals(tl1.getMilliseconds(),0);
        assertEquals(tl1.getYear(), 1970);
        assertEquals(tl1.getMonth(),1 );
        assertEquals(tl1.getDay(), 1);
        assertEquals(tl1.getHour(),0);
        assertEquals(tl1.getMinute(),0);
        assertEquals(tl1.getSecond(), 0);

    }

    @Test
    public void stringInitTest(){
        TimeLiteral tl1 = new TimeLiteral("2020-04-06T15:33:00");
        System.out.println(tl1.toString());
        assertEquals(tl1.getYear(), 2020);
        assertEquals(tl1.getMonth(),4 );
        assertEquals(tl1.getDay(), 6);
        assertEquals(tl1.getHour(),15);
        assertEquals(tl1.getMinute(),33);
        assertEquals(tl1.getSecond(), 0);

        assertEquals(tl1.getLowerBound(), tl1.getUpperBound());
        assertEquals(tl1.getUpperBound(), tl1.evaluate());
        assertEquals(tl1.evaluate(), tl1.getMilliseconds());

        TimeLiteral tl2 = new TimeLiteral("2020-04-05");
        assertEquals(tl2.getYear(), 2020);
        assertEquals(tl2.getMonth(),4 );
        assertEquals(tl2.getDay(), 5);
        assertEquals(tl2.getHour(),0);
        assertEquals(tl2.getMinute(),0);
        assertEquals(tl2.getSecond(), 0);
    }
}
