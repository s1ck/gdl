package org.s1ck.gdl.comparables.time;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.TimeConstant;

import static org.junit.Assert.assertEquals;

public class TimeConstantTest {

    @Test
    public void constantTest(){
        TimeConstant c = new TimeConstant(1000);
        assertEquals(c.getMillis(), 1000);

        int days = 23;
        int hours = 11;
        int minutes = 7;
        int seconds = 42;
        int millis = 1;
        TimeConstant c2 = new TimeConstant(days, hours, minutes, seconds, millis);

        int expected_millis = millis + 1000*seconds + (1000*60)*minutes + (1000*60*60)*hours +
                (1000*60*60*24)*days;
        assertEquals(expected_millis, c2.getMillis());

        assertEquals(c2.getMillis(), (long)c2.evaluate().get());
    }
}
