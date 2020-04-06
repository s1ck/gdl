package org.s1ck.gdl.comparables.time;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.TimeSelector;

import static org.junit.Assert.assertEquals;

public class TimeSelectorTest {

    @Test
    public void selectorTest(){
        TimeSelector selector = new TimeSelector("var", TimeSelector.TimeField.TX_FROM);
        assertEquals(selector.getVariable(), "var");
        assertEquals(selector.getVariables().size(), 1);
        assertEquals(selector.getVariables().get(0), "var");
        assertEquals(selector.getLowerBound(), 0);
        assertEquals(selector.getUpperBound(), Long.MAX_VALUE);
        assertEquals(selector.getTimeProp(), TimeSelector.TimeField.TX_FROM);
    }
}
