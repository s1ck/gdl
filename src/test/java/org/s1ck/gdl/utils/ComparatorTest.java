package org.s1ck.gdl.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ComparatorTest {

    @Test
    public void testComparatorInversion(){
        assertEquals(Comparator.EQ.getInverse(), Comparator.NEQ);
        assertEquals(Comparator.NEQ.getInverse(), Comparator.EQ);
        assertEquals(Comparator.GT.getInverse(), Comparator.LTE);
        assertEquals(Comparator.LT.getInverse(), Comparator.GTE);
        assertEquals(Comparator.GTE.getInverse(), Comparator.LT);
        assertEquals(Comparator.LTE.getInverse(), Comparator.GT);
    }
}
