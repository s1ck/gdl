package org.s1ck.gdl.comparables.time;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;
import org.s1ck.gdl.model.comparables.time.TimeSelector;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

public class TimeLiteralTest {

    @Test
    public void millisInitTest(){
        TimeLiteral tl1 = new TimeLiteral(0);
        assertEquals(tl1.getMilliseconds(),0);
        assertEquals(tl1.getYear(), 1970);
        assertEquals(tl1.getMonth(),1 );
        assertEquals(tl1.getDay(), 1);
        assertEquals(tl1.getHour(),0);
        assertEquals(tl1.getMinute(),0);
        assertEquals(tl1.getSecond(), 0);

        TimeLiteral tl2 = new TimeLiteral(-1000);
        assertEquals(tl2.getMilliseconds(),-1000);
        assertEquals(tl2.getYear(), 1969);
        assertEquals(tl2.getMonth(),12);
        assertEquals(tl2.getDay(), 31);
        assertEquals(tl2.getHour(),23);
        assertEquals(tl2.getMinute(),59);
        assertEquals(tl2.getSecond(), 59);
    }

    @Test
    public void stringInitTest(){
        TimeLiteral tl1 = new TimeLiteral("2020-04-06T15:33:00");
        assertEquals(tl1.getYear(), 2020);
        assertEquals(tl1.getMonth(),4 );
        assertEquals(tl1.getDay(), 6);
        assertEquals(tl1.getHour(),15);
        assertEquals(tl1.getMinute(),33);
        assertEquals(tl1.getSecond(), 0);

        assertEquals((long)tl1.evaluate().get(), tl1.getMilliseconds());

        TimeLiteral tl2 = new TimeLiteral("2020-04-05");
        assertEquals(tl2.getYear(), 2020);
        assertEquals(tl2.getMonth(),4 );
        assertEquals(tl2.getDay(), 5);
        assertEquals(tl2.getHour(),0);
        assertEquals(tl2.getMinute(),0);
        assertEquals(tl2.getSecond(), 0);

        TimeLiteral tl3 = new TimeLiteral("now");
        long millis = LocalDateTime.now().toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli();

        assertTrue(millis - tl3.getMilliseconds() >= 0);
        //assertTrue(millis - tl3.getMilliseconds() < 5000);
    }

    @Test
    public void containsTxToTest(){
        TimeLiteral literal = new TimeLiteral("1970-02-01T15:23:05");
        assertFalse(literal.containsSelectorType(TimeSelector.TimeField.TX_TO));
    }

    @Test
    public void globalTest(){
        TimeLiteral literal1 = new TimeLiteral();
        TimeLiteral literal2 = new TimeLiteral("2020-05-06");
        assertFalse(literal1.isGlobal() || literal2.isGlobal());
        assertEquals(literal1.replaceGlobalByLocal(new ArrayList<>(Collections.singletonList("a"))),
                literal1);
    }
}
