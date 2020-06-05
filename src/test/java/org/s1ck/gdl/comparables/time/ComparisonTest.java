package org.s1ck.gdl.comparables.time;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.PropertySelector;
import org.s1ck.gdl.model.comparables.time.MaxTimePoint;
import org.s1ck.gdl.model.comparables.time.MinTimePoint;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;
import org.s1ck.gdl.model.comparables.time.TimeSelector;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import static org.junit.Assert.*;
import static org.s1ck.gdl.utils.Comparator.GT;
import static org.s1ck.gdl.utils.Comparator.LT;

public class ComparisonTest {
    @Test
    public void testIsTemporal(){
        TimeSelector ts = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeLiteral tl = new TimeLiteral("1970-01-01T01:01:01");
        Comparison timeComp = new Comparison(ts, Comparator.NEQ, tl);
        assertTrue(timeComp.isTemporal());

        PropertySelector ps1 = new PropertySelector("p", "prop");
        PropertySelector ps2 = new PropertySelector("q", "prop");
        Comparison propertyComp = new Comparison(ps1, LT, ps2);
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

        timeComp = new Comparison(ts, LT, tl);
        assertEquals(timeComp.switchSides(), new Comparison(tl, GT, ts));

        timeComp = new Comparison(ts, Comparator.LTE, tl);
        assertEquals(timeComp.switchSides(), new Comparison(tl, Comparator.GTE, ts));

        timeComp = new Comparison(ts, Comparator.GTE, tl);
        assertEquals(timeComp.switchSides(), new Comparison(tl, Comparator.LTE, ts));

        timeComp = new Comparison(ts, GT, tl);
        assertEquals(timeComp.switchSides(), new Comparison(tl, LT, ts));
    }

    @Test
    public void testGlobal(){
        TimeSelector ts = new TimeSelector("var", TimeSelector.TimeField.VAL_TO);
        TimeLiteral tl = new TimeLiteral("1970-01-01T01:01:01");
        TimeSelector global = new TimeSelector(TimeSelector.GLOBAL_SELECTOR,
                TimeSelector.TimeField.TX_FROM);
        assertFalse(new Comparison(ts, Comparator.LTE, tl).isGlobal());
        assertTrue(new Comparison(ts, Comparator.GTE, global).isGlobal());
        assertFalse(new Comparison(new MinTimePoint(ts, tl), LT, new MaxTimePoint(ts,tl)).isGlobal());
        assertTrue(new Comparison(new MinTimePoint(ts, global), GT, new MaxTimePoint(ts, tl)).isGlobal());
    }

}
