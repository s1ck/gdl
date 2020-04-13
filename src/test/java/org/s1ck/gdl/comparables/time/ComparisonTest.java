package org.s1ck.gdl.comparables.time;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.PropertySelector;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;
import org.s1ck.gdl.model.comparables.time.TimeSelector;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import static org.junit.Assert.*;

public class ComparisonTest {
    @Test
    public void testIsTemporal(){
        TimeSelector ts = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeLiteral tl = new TimeLiteral("1970-01-01T01:01:01");
        Comparison timeComp = new Comparison(ts, Comparator.NEQ, tl);
        assertTrue(timeComp.isTemporal());

        PropertySelector ps1 = new PropertySelector("p", "prop");
        PropertySelector ps2 = new PropertySelector("q", "prop");
        Comparison propertyComp = new Comparison(ps1, Comparator.LT, ps2);
        assertFalse(propertyComp.isTemporal());
    }

    @Test
    public void testSwitchSides(){
        TimeSelector ts = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeLiteral tl = new TimeLiteral("1970-01-01T01:01:01");

        Comparison timeComp = new Comparison(ts, Comparator.EQ, tl);
        assertEquals(timeComp.switchSides(), new Comparison(tl, Comparator.EQ, ts));

        timeComp = new Comparison(ts, Comparator.NEQ, tl);
        assertEquals(timeComp.switchSides(), new Comparison(tl, Comparator.NEQ, ts));

        timeComp = new Comparison(ts, Comparator.LT, tl);
        assertEquals(timeComp.switchSides(), new Comparison(tl, Comparator.GT, ts));

        timeComp = new Comparison(ts, Comparator.LTE, tl);
        assertEquals(timeComp.switchSides(), new Comparison(tl, Comparator.GTE, ts));

        timeComp = new Comparison(ts, Comparator.GTE, tl);
        assertEquals(timeComp.switchSides(), new Comparison(tl, Comparator.LTE, ts));

        timeComp = new Comparison(ts, Comparator.GT, tl);
        assertEquals(timeComp.switchSides(), new Comparison(tl, Comparator.LT, ts));
    }

}
