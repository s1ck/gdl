package org.s1ck.gdl.comparables.time;

import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.Duration;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;
import org.s1ck.gdl.model.comparables.time.TimeSelector;

import static org.junit.Assert.*;
import static org.s1ck.gdl.model.comparables.time.TimeSelector.TimeField.TX_FROM;
import static org.s1ck.gdl.model.comparables.time.TimeSelector.TimeField.TX_TO;

public class DurationTest {

    @Test
    public void simpleDurationTest(){
        TimeLiteral l1 = new TimeLiteral("1970-01-01T00:00:00");
        TimeLiteral l2 = new TimeLiteral("1970-01-01T00:00:01");
        Duration duration = new Duration(l1, l2);
        assertEquals((long) duration.evaluate().get(), 1000L);
    }

    @Test
    public void selectorDurationTest(){
        TimeLiteral l1 = new TimeLiteral("1979-04-11T00:12:12");
        TimeSelector s1 = new TimeSelector("a", TX_TO);
        Duration duration = new Duration(l1, s1);
        assertFalse(duration.evaluate().isPresent());
        assertEquals(duration.getVariables().size(), 1);
        assertEquals(duration.getVariables().toArray()[0], "a");
        assertFalse(duration.isGlobal());

        TimeSelector global = new TimeSelector(TimeSelector.GLOBAL_SELECTOR, TX_FROM);
        duration = new Duration(global, s1);
        assertTrue(duration.isGlobal());
    }
}
