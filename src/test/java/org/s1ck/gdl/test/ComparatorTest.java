package org.s1ck.gdl.test;

import org.junit.Test;
import org.s1ck.gdl.utils.Comparator;

import static org.junit.Assert.assertEquals;

public class ComparatorTest {

    @Test
    public void invertComparatorTest(){
        assertEquals(Comparator.EQ.getInverse(), Comparator.NEQ);
        assertEquals(Comparator.NEQ.getInverse(), Comparator.EQ);
        assertEquals(Comparator.GT.getInverse(), Comparator.LTE);
        assertEquals(Comparator.LT.getInverse(), Comparator.GTE);
        assertEquals(Comparator.GTE.getInverse(), Comparator.LT);
        assertEquals(Comparator.LTE.getInverse(), Comparator.GT);
    }
}
