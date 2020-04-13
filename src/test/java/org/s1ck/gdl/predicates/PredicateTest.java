package org.s1ck.gdl.predicates;

import org.junit.Test;
import org.s1ck.gdl.model.comparables.ComparableExpression;
import org.s1ck.gdl.model.comparables.time.MaxTimePoint;
import org.s1ck.gdl.model.comparables.time.MinTimePoint;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;
import org.s1ck.gdl.model.comparables.time.TimeSelector;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.booleans.Not;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.booleans.Xor;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PredicateTest {

    @Test
    // only comparison, only one complex type of depth 1
    public void testSimpleUnfold(){
        //--------------------------------------------------
        // DATA
        //------------------------------------------------
        TimeLiteral literal = new TimeLiteral("1970-01-01");
        TimeSelector m1 = new TimeSelector("p", TimeSelector.TimeField.VAL_FROM);
        TimeLiteral m2 = new TimeLiteral("1972-01-01");
        MaxTimePoint mx1 = new MaxTimePoint(m1, m2);

        //---------------------------------------------------
        // EQ
        //---------------------------------------------------
        Comparison c1 = new Comparison(literal, Comparator.EQ, mx1);
        Comparison c2 = new Comparison(mx1, Comparator.EQ, literal);
        Or expected1 = new Or(
                new And(
                        new Comparison(m1, Comparator.EQ, literal),
                        new Comparison(m1, Comparator.GTE, m2)
                ),
                new And(
                        new Comparison(m2, Comparator.EQ, literal),
                        new Comparison(m2, Comparator.GTE, m1)
                )
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c1), expected1);
        // expected 1 with switched sides
        Or expected2 = new Or(
                new And(
                        new Comparison(literal, Comparator.EQ, m1),
                        new Comparison(m2, Comparator.LTE, m1)
                ),
                new And(
                        new Comparison(literal, Comparator.EQ, m2),
                        new Comparison(m1, Comparator.LTE, m2)
                )
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c2), expected2);

        //--------------------------------------------------------------
        //NEQ
        //-------------------------------------------------------------
        Comparison c3 = new Comparison(literal, Comparator.NEQ, mx1);
        Comparison c4 = new Comparison(mx1, Comparator.NEQ, literal);

        And expected3 = new And(
                new Or(
                        new Comparison(m1, Comparator.NEQ, literal),
                        new Comparison(m1, Comparator.LT, m2)
                ),
                new Or(
                        new Comparison(m2, Comparator.NEQ, literal),
                        new Comparison(m2, Comparator.LT, m1)
                )
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c3), expected3);
        //expected3 with switched sides
        And expected4 = new And(
                new Or(
                        new Comparison(literal, Comparator.NEQ, m1),
                        new Comparison(m2, Comparator.GT, m1)
                ),
                new Or(
                        new Comparison(literal, Comparator.NEQ, m2),
                        new Comparison(m1, Comparator.GT, m2)
                )
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c4), expected4);

        //--------------------------------------------------------------
        // GT
        //-------------------------------------------------------------
        Comparison c5 = new Comparison(literal, Comparator.GT, mx1);
        Comparison c6 = new Comparison(mx1, Comparator.GT, literal);

        And expected5= new And(
                new Comparison(m1, Comparator.LT, literal),
                new Comparison(m2, Comparator.LT, literal)
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c5), expected5);

        Or expected6 = new Or(
                new Comparison(literal, Comparator.LT, m1),
                new Comparison(literal, Comparator.LT, m2)
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c6), expected6);

        //--------------------------------------------------------------
        // GTE
        //-------------------------------------------------------------
        Comparison c7 = new Comparison(literal, Comparator.GTE, mx1);
        Comparison c8 = new Comparison(mx1, Comparator.GTE, literal);

        And expected7= new And(
                new Comparison(m1, Comparator.LTE, literal),
                new Comparison(m2, Comparator.LTE, literal)
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c7), expected7);

        Or expected8 = new Or(
                new Comparison(literal, Comparator.LTE, m1),
                new Comparison(literal, Comparator.LTE, m2)
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c8), expected8);

        //--------------------------------------------------------------
        // LT
        //-------------------------------------------------------------
        Comparison c9 = new Comparison(literal, Comparator.LT, mx1);
        Comparison c10 = new Comparison(mx1, Comparator.LT, literal);

        Or expected9= new Or(
                new Comparison(m1, Comparator.GT, literal),
                new Comparison(m2, Comparator.GT, literal)
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c9), expected9);

        And expected10 = new And(
                new Comparison(literal, Comparator.GT, m1),
                new Comparison(literal, Comparator.GT, m2)
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c10), expected10);

        //--------------------------------------------------------------
        // LTE
        //-------------------------------------------------------------
        Comparison c11 = new Comparison(literal, Comparator.LTE, mx1);
        Comparison c12 = new Comparison(mx1, Comparator.LTE, literal);

        Or expected11= new Or(
                new Comparison(m1, Comparator.GTE, literal),
                new Comparison(m2, Comparator.GTE, literal)
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c7), expected7);

        And expected12 = new And(
                new Comparison(literal, Comparator.GTE, m1),
                new Comparison(literal, Comparator.GTE, m2)
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c8), expected8);

    }

    @Test
    public void testUnfoldWithTwoTempPredicates(){
        TimeSelector mx1 = new TimeSelector("mx1", TimeSelector.TimeField.VAL_FROM);
        TimeSelector mx2 = new TimeSelector("mx2", TimeSelector.TimeField.VAL_TO);
        MaxTimePoint mx = new MaxTimePoint(mx1, mx2);

        TimeSelector mn1 = new TimeSelector("mn1", TimeSelector.TimeField.TX_FROM);
        TimeSelector mn2 = new TimeSelector("mn2", TimeSelector.TimeField.TX_TO);
        MinTimePoint mn = new MinTimePoint(mn1, mn2);

        //-----------------------------------------------
        // compare max to min
        //-------------------------------------------------
        Comparison c1 = new Comparison(mn, Comparator.GT, mx);
        And expected1 = new And(
                new And(
                        new Comparison(mx1, Comparator.LT, mn1),
                        new Comparison(mx2, Comparator.LT, mn1)
                ),
                new And(
                        new Comparison(mx1, Comparator.LT, mn2),
                        new Comparison(mx2, Comparator.LT, mn2)
                )
        );
        assertEquals(expected1, Predicate.unfoldTemporalComparisons(c1));

        Comparison c2 = new Comparison(mx, Comparator.GT, mn);
        Or expected2 = new Or(
                new Or(
                        new Comparison(mn1, Comparator.LT, mx1),
                        new Comparison(mn2, Comparator.LT, mx1)
                ),
                new Or(
                        new Comparison(mn1, Comparator.LT, mx2),
                        new Comparison(mn2, Comparator.LT, mx2)
                )
        );
        assertEquals(expected2, Predicate.unfoldTemporalComparisons(c2));

        //---------------------------------------------------------------
        // compare MAX to MAX
        //--------------------------------------------------------------
        Comparison c3 = new Comparison(mx, Comparator.EQ, mx);
        Or expected3 = new Or(
          new And(
                  // mx == mx1
                  new Or(
                          new And(
                                  new Comparison(mx1, Comparator.EQ, mx1),
                                  new Comparison(mx1, Comparator.GTE, mx2)
                          ),
                          new And(
                                  new Comparison(mx2, Comparator.EQ, mx1),
                                  new Comparison(mx2, Comparator.GTE, mx1)
                          )
                  ),
                  // mx1 >= mx2
                  new Comparison(mx2, Comparator.LTE, mx1)
                  ),
          new And(
                  // mx == mx2
                  new Or(
                          new And(
                                  new Comparison(mx1, Comparator.EQ, mx2),
                                  new Comparison(mx1, Comparator.GTE, mx2)
                          ),
                          new And(
                                  new Comparison(mx2, Comparator.EQ, mx2),
                                  new Comparison(mx2, Comparator.GTE, mx1)
                          )
                  ),
                  new Comparison(mx1, Comparator.LTE, mx2)
          )
        );
        assertEquals(expected3, Predicate.unfoldTemporalComparisons(c3));

        //--------------------------------------------------
        // compare MIN to MIN
        //--------------------------------------------------
        Comparison c4 = new Comparison(mn, Comparator.LTE, mn);
        Or expected4 = new Or(
                new And(
                        new Comparison(mn1, Comparator.GTE, mn1),
                        new Comparison(mn2, Comparator.GTE, mn1)
                ),
                new And(
                        new Comparison(mn1, Comparator.GTE, mn2),
                        new Comparison(mn2, Comparator.GTE, mn2)
                )
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c4), expected4);
    }

    @Test
    public void testUnfoldWithBooleans(){
        TimeSelector mx1 = new TimeSelector("mx1", TimeSelector.TimeField.VAL_FROM);
        TimeSelector mx2 = new TimeSelector("mx2", TimeSelector.TimeField.VAL_TO);
        MaxTimePoint mx = new MaxTimePoint(mx1, mx2);

        TimeSelector mn1 = new TimeSelector("mn1", TimeSelector.TimeField.TX_FROM);
        TimeSelector mn2 = new TimeSelector("mn2", TimeSelector.TimeField.TX_TO);
        MinTimePoint mn = new MinTimePoint(mn1, mn2);

        Comparison c1 = new Comparison(mx, Comparator.NEQ, mn);
        Comparison c2 = new Comparison(mn1, Comparator.LTE, mx2);

        Predicate expected1 = new And(
                new Or(
                        // mn neq mx1
                        new And(
                                new Or(
                                        new Comparison(mn1, Comparator.NEQ, mx1),
                                        new Comparison(mn1, Comparator.GT, mn2)
                                ),
                                new Or(
                                        new Comparison(mn2, Comparator.NEQ, mx1),
                                        new Comparison(mn2, Comparator.GT, mn1)
                                )
                        ),

                        new Comparison(mx2, Comparator.GT, mx1)
                ),
                new Or(
                        // mn neq mx2
                        new And(
                                new Or(
                                        new Comparison(mn1, Comparator.NEQ, mx2),
                                        new Comparison(mn1, Comparator.GT, mn2)
                                ),
                                new Or(
                                        new Comparison(mn2, Comparator.NEQ, mx2),
                                        new Comparison(mn2, Comparator.GT, mn1)
                                )
                        ),
                        new Comparison(mx1, Comparator.GT, mx2)
                )
        );

        Predicate expected2 = new Comparison(mx2, Comparator.GTE, mn1);

        //or
        Or or = new Or(c1, c2);
        assertEquals(Predicate.unfoldTemporalComparisons(or), new Or(expected1, expected2));

        //and
        And and = new And(c1, c2);
        assertEquals(Predicate.unfoldTemporalComparisons(and), new And(expected1, expected2));

        //xor
        Xor xor = new Xor(c1, c2);
        assertEquals(Predicate.unfoldTemporalComparisons(xor), new Xor(expected1, expected2));

        //not
        Not not = new Not(c1);
        assertEquals(Predicate.unfoldTemporalComparisons(not), new Not(expected1));
    }

    @Test
    public void testOneSideDeepUnfold(){
        TimeLiteral l1 = new TimeLiteral("1970-02-01");
        TimeLiteral l2 = new TimeLiteral("2005-04-13");
        TimeLiteral l3 = new TimeLiteral("1999-12-31T23:59:59");
        TimeLiteral l4 = new TimeLiteral("2021-04-11");
        MinTimePoint mn = new MinTimePoint(l1,l2);
        MaxTimePoint mx = new MaxTimePoint(mn, l3);
        Comparison c = new Comparison(mx, Comparator.GT, l4);

        Predicate expected = new Or(
                // mn > l4
                new And(
                        new Comparison(l4, Comparator.LT, l1),
                        new Comparison(l4, Comparator.LT, l2)
                ),
                new Comparison(l4, Comparator.LT, l3)
        );

        assertEquals(Predicate.unfoldTemporalComparisons(c), expected);
    }

    @Test
    public void testBothSideDeepUnfold(){
        TimeLiteral l1 = new TimeLiteral("1970-02-01");
        TimeLiteral l2 = new TimeLiteral("2005-04-13");
        TimeLiteral l3 = new TimeLiteral("1999-12-31T23:59:59");
        TimeLiteral l4 = new TimeLiteral("2021-04-11");

        TimeLiteral l5 = new TimeLiteral("1970-02-01");
        TimeLiteral l6 = new TimeLiteral("2005-04-13");
        TimeLiteral l7 = new TimeLiteral("1999-12-31T23:59:59");
        TimeLiteral l8 = new TimeLiteral("2021-04-11");

        MaxTimePoint lhs = new MaxTimePoint(new MinTimePoint(l1,l2), new MaxTimePoint(l3,l4));
        MinTimePoint rhs = new MinTimePoint(new MaxTimePoint(l5, l6), new MinTimePoint(l7, l8));
        Comparison c = new Comparison(lhs, Comparator.LTE, rhs);

        Predicate expected = new And(
                new Or(
                        new And(
                                new Or(
                                        new Comparison(l5, Comparator.GTE, l1),
                                        new Comparison(l6, Comparator.GTE, l1)
                                ),
                                new And(
                                        new Comparison(l7, Comparator.GTE, l1),
                                        new Comparison(l8, Comparator.GTE, l1)
                                )
                        ),
                        new And(
                                new Or(
                                        new Comparison(l5, Comparator.GTE, l2),
                                        new Comparison(l6, Comparator.GTE, l2)
                                ),
                                new And(
                                        new Comparison(l7, Comparator.GTE, l2),
                                        new Comparison(l8, Comparator.GTE, l2)
                                )
                        )
                ),
                new And(
                        new And(
                                new Or(
                                        new Comparison(l5, Comparator.GTE, l3),
                                        new Comparison(l6, Comparator.GTE, l3)
                                ),
                                new And(
                                        new Comparison(l7, Comparator.GTE, l3),
                                        new Comparison(l8, Comparator.GTE, l3)
                                )
                        ),
                        new And(
                                new Or(
                                        new Comparison(l5, Comparator.GTE, l4),
                                        new Comparison(l6, Comparator.GTE, l4)
                                ),
                                new And(
                                        new Comparison(l7, Comparator.GTE, l4),
                                        new Comparison(l8, Comparator.GTE, l4)
                                )
                        )
                )
        );
        assertEquals(Predicate.unfoldTemporalComparisons(c), expected);
    }


}
