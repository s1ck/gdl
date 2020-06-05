package org.s1ck.gdl.comparables.time;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;
import org.s1ck.gdl.model.comparables.time.TimeSelector;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.s1ck.gdl.utils.Comparator.NEQ;

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

        assertTrue(millis - tl3.getMilliseconds() > 0);
        assertTrue(millis - tl3.getMilliseconds() < 5000);
    }

    @Test
    public void unfoldPredicateTest(){
        TimeLiteral literal = new TimeLiteral("1970-02-01T15:23:05");
        TimeSelector s = new TimeSelector("x", TimeSelector.TimeField.VAL_TO);
        //expected values
        Comparison cEq = new Comparison(literal, Comparator.EQ, s);
        Comparison cNeq = new Comparison(literal, NEQ, s);
        Comparison cGt = new Comparison(literal, Comparator.GT, s);
        Comparison cGte = new Comparison(literal, Comparator.GTE, s);
        Comparison cLt = new Comparison(literal, Comparator.LT, s);
        Comparison cLte = new Comparison(literal, Comparator.LTE, s);

        assertEquals(literal.unfoldComparison(Comparator.EQ, s), cEq);
        assertEquals(literal.unfoldComparison(NEQ, s), cNeq);
        assertEquals(literal.unfoldComparison(Comparator.GT, s), cGt);
        assertEquals(literal.unfoldComparison(Comparator.GTE, s), cGte);
        assertEquals(literal.unfoldComparison(Comparator.LT, s), cLt);
        assertEquals(literal.unfoldComparison(Comparator.LTE, s), cLte);

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
