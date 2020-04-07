package org.s1ck.gdl.comparables.time;
import org.junit.Assert;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.TimeConstant;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;

import static org.junit.Assert.assertEquals;

public class TimeConstantTest {

    @Test
    public void constantTest(){
        TimeConstant c = new TimeConstant(1000);
        assertEquals(c.getMillis(), 1000);
        assertEquals(c.evaluate(), c.getLowerBound());
        assertEquals(c.getLowerBound(), c.getUpperBound());
        assertEquals(c.getUpperBound(), c.getMillis());

        int days = 23;
        int hours = 11;
        int minutes = 7;
        int seconds = 42;
        int millis = 1;
        TimeConstant c2 = new TimeConstant(days, hours, minutes, seconds, millis);
        TimeLiteral c2lit = new TimeLiteral(c2.getMillis());
        assertEquals(c2lit.getDay(), days+1);
        assertEquals(c2lit.getHour(), hours);
        assertEquals(c2lit.getMinute(), minutes);
        assertEquals(c2lit.getSecond(), seconds);
    }
}
