package org.s1ck.gdl.comparables.time;

import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.MaxTimePoint;
import org.s1ck.gdl.model.comparables.time.MinTimePoint;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;
import org.s1ck.gdl.model.comparables.time.TimeSelector;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class MinTimeTest {

    @Test
    public void simpleMinTimeTest(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeLiteral l5 = new TimeLiteral("2019-04-05T01:02:31");
        MinTimePoint mn = new MinTimePoint(l1,l2,l3,l4,l5);
        assertEquals(mn.evaluate().get(), l3.evaluate().get());
    }

    @Test
    public void mixedMinTimeTest(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeSelector l4 = new TimeSelector("v", TimeSelector.TimeField.VAL_TO);
        MinTimePoint mn = new MinTimePoint(l1,l2,l3,l4);
        assertFalse(mn.evaluate().isPresent());

        // only selectors
        TimeSelector l5 = new TimeSelector("x",TimeSelector.TimeField.VAL_FROM);
        mn = new MinTimePoint(l4,l5);
        assertFalse(mn.evaluate().isPresent());
    }

    @Test
    public void complexMinTest(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12T08:00");
        TimeSelector s1 = new TimeSelector("p", TimeSelector.TimeField.VAL_TO);
        MinTimePoint mn1 = new MinTimePoint(l1,s1);

        TimeLiteral l2 = new TimeLiteral("2005-08-12T17:21:43");
        TimeSelector s2 = new TimeSelector("q", TimeSelector.TimeField.TX_FROM);
        MinTimePoint mn2 = new MinTimePoint(s2,l2);

        MinTimePoint mn3 = new MinTimePoint(mn1, mn2);
        assertFalse(mn3.evaluate().isPresent());

        assertEquals(mn3.getVariables().size(),2);
        assertTrue(mn3.getVariables().contains("q"));
        assertTrue(mn3.getVariables().contains("p"));
    }

    @Test
    public void minMaxTest1(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        MaxTimePoint mx1 = new MaxTimePoint(l1,l2);
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeLiteral l5 = new TimeLiteral("2019-04-05T01:02:31");
        MaxTimePoint mx2 = new MaxTimePoint(l3,l4,l5);
        MinTimePoint mn = new MinTimePoint(mx1,mx2);
        assertEquals(mn.evaluate().get(), l2.evaluate().get());
    }

    @Test
    public void minMaxTest2(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        MaxTimePoint mx1 = new MaxTimePoint(l1,l2);
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeLiteral l5 = new TimeLiteral("2019-04-05T01:02:31");
        MinTimePoint mn1 = new MinTimePoint(l3,l4,l5);
        MinTimePoint mn = new MinTimePoint(mx1,mn1);
        assertEquals(mn.evaluate().get(), l3.evaluate().get());
    }

    @Test
    public void minMaxTest3(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        TimeSelector s1 = new TimeSelector("s", TimeSelector.TimeField.TX_FROM);
        MaxTimePoint mx1 = new MaxTimePoint(l1,l2,s1);
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeSelector s2 = new TimeSelector("t", TimeSelector.TimeField.VAL_TO);
        MaxTimePoint mx2 = new MaxTimePoint(l3,l4,s2);
        MinTimePoint mn = new MinTimePoint(mx1,mx2);
        assertFalse(mn.evaluate().isPresent());
    }

    @Test
    public void minMaxTest4(){
        TimeLiteral l1 = new TimeLiteral("2017-01-12");
        TimeLiteral l2 = new TimeLiteral("2017-01-12T08:00");
        TimeSelector s1 = new TimeSelector("s", TimeSelector.TimeField.TX_FROM);
        MinTimePoint mn1 = new MinTimePoint(l1,l2,s1);
        TimeLiteral l3 = new TimeLiteral("2005-08-12T17:21:43");
        TimeLiteral l4 = new TimeLiteral("2019-04-05");
        TimeLiteral l5 = new TimeLiteral("2019-04-05T01:02:31");
        TimeSelector s2 = new TimeSelector("t", TimeSelector.TimeField.VAL_TO);
        MaxTimePoint mx1 = new MaxTimePoint(l3,l4,l5,s2);
        MinTimePoint mn = new MinTimePoint(mn1,mx1);
        assertFalse(mn.evaluate().isPresent());
    }

    @Test
    public void containsTxToTest(){
        TimeSelector s1 = new TimeSelector("a", "val_to");
        TimeLiteral l = new TimeLiteral("2020-04-28");
        MinTimePoint m1 = new MinTimePoint(s1,l);
        assertFalse(m1.containsSelectorType(TimeSelector.TimeField.TX_TO));

        TimeSelector s2 = new TimeSelector("a", "tx_to");
        MinTimePoint m2 = new MinTimePoint(s2,l);
        assertTrue(m2.containsSelectorType(TimeSelector.TimeField.TX_TO));
    }

    @Test
    public void globalTest(){
        TimeSelector s1 = new TimeSelector("a", "val_to");
        TimeLiteral l = new TimeLiteral("2020-04-28");
        TimeSelector global = new TimeSelector(TimeSelector.GLOBAL_SELECTOR, "tx_from");

        ArrayList<String> variables = new ArrayList<>(Arrays.asList("a","b"));
        MaxTimePoint mx = new MaxTimePoint(
                new TimeSelector("a", TimeSelector.TimeField.TX_FROM),
                new TimeSelector("b", TimeSelector.TimeField.TX_FROM)
        );

        MinTimePoint test = new MinTimePoint(global, l);
        MinTimePoint expected = new MinTimePoint(mx, l);

        assertEquals(expected, test.replaceGlobalByLocal(variables));

        assertTrue(new MinTimePoint(s1,global, l, global).isGlobal());
        assertFalse(new MinTimePoint(s1,l).isGlobal());
    }


}
