package org.s1ck.gdl;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.Literal;
import org.s1ck.gdl.model.comparables.PropertySelector;
import org.s1ck.gdl.model.comparables.time.*;
import org.s1ck.gdl.model.comparables.time.util.TimeConstant;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import java.sql.Time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.s1ck.gdl.model.comparables.time.TimeSelector.TimeField.*;
import static org.s1ck.gdl.utils.Comparator.*;

public class GDLLoaderTemporalTest {

    @Test
    public void simpleTimestampFunctionsTest(){
        GDLLoader loader = getLoaderFromGDLString(
                "MATCH (alice)-[e1:knows {since : 2014}]->(bob) (alice)-[e2:knows {since : 2013}]->(eve) " +
                        "WHERE (e1.val_from.before(2017-01-01) AND e2.tx_to.after(2018-12-23T15:55:23)) OR e1.knows>2010");
        assertTrue(predicateContainedIn(
                new Comparison(new TimeSelector("e1", "val_from"), Comparator.LT, new TimeLiteral("2017-01-01")),
                loader.getPredicates().get().switchSides()));
        assertTrue(predicateContainedIn(
                new Comparison(new TimeSelector("e2", "tx_to"), Comparator.GT, new TimeLiteral("2018-12-23T15:55:23")),
                loader.getPredicates().get().switchSides()));
        assertTrue(predicateContainedIn(
                new Comparison(new PropertySelector("e1", "knows"), Comparator.GT, new Literal(2010)),
                loader.getPredicates().get().switchSides()));
    }

    @Test
    public void periodLiteralTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.overlaps(Interval(1970-01-01,1970-01-02))");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.overlaps(Interval(1970-01-01,1970-01-02))", false);

        assertPredicateEquals(loaderDoProcess.getPredicates().get(),
                new And(
                   new And(
                           new Comparison(
                                   new TimeSelector("a", "tx_to"),
                                   Comparator.GT,
                                   new TimeSelector("a", "tx_from")
                           ),
                           new Comparison(
                                   new TimeLiteral("1970-01-02"),
                                   Comparator.GT,
                                   new TimeSelector("a", "tx_from")
                           )
                   ),
                   new And(
                           new Comparison(
                                   new TimeSelector("a", "tx_to"),
                                   Comparator.GT,
                                   new TimeLiteral("1970-01-01")
                           ),
                           new Comparison(
                                   new TimeLiteral("1970-01-02"),
                                   Comparator.GT,
                                   new TimeLiteral("1970-01-01")
                           )
                   )
                )
                );
        assertPredicateEquals(loaderRaw.getPredicates().get(),
                new Comparison(
                        new MaxTimePoint(new TimeSelector("a", TX_FROM),
                                new TimeLiteral("1970-01-01")),
                        LT,
                        new MinTimePoint(new TimeSelector("a", TX_TO),
                                new TimeLiteral("1970-01-02"))
                ));
    }

    @Test
    public void afterTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.val_from.after(1970-01-01T00:00:01) AND a.tx_to.after(1970-01-01T00:00:01)");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.val_from.after(1970-01-01T00:00:01) AND a.tx_to.after(1970-01-01T00:00:01)",
                false);
        Predicate result = loaderDoProcess.getPredicates().get();
        Predicate expected =
                new And(
                    new Comparison(
                        new TimeLiteral("1970-01-01T00:00:01"),
                        Comparator.LT,
                        new TimeSelector("a", "val_from")),
                        new Comparison(
                                new TimeLiteral("1970-01-01T00:00:01"),
                                Comparator.LT,
                                new TimeSelector("a", "tx_to"))

                );
        assertPredicateEquals(result, expected);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expected);
    }

    @Test
    public void fromToTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.fromTo(1970-01-01, b.tx_to)");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.fromTo(1970-01-01, b.tx_to)", false);
        Predicate result = loaderDoProcess.getPredicates().get();
        Predicate expected = new And(
                new Comparison(
                        new TimeSelector("b", "tx_to"),
                        Comparator.GT,
                        new TimeSelector("a", "tx_from")
                ),
                new Comparison(
                        new TimeLiteral("1970-01-01"),
                        Comparator.LT,
                        new TimeSelector("a", "tx_to")
                )
        );
        assertPredicateEquals(result, expected);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expected);
    }

    @Test
    public void betweenTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.between(b.tx_from, 2020-04-28T10:39:15)");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.between(b.tx_from, 2020-04-28T10:39:15)", false);
        Predicate result = loaderDoProcess.getPredicates().get();
        Predicate expected = new And(
                new Comparison(
                        new TimeSelector("a", TX_FROM),
                        LTE,
                        new TimeLiteral("2020-04-28T10:39:15")
                ),
                new Comparison(
                        new TimeSelector("a", TX_TO),
                        Comparator.GT,
                        new TimeSelector("b", TX_FROM)
                )
        );
        assertPredicateEquals(result, expected);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expected);
    }

    @Test
    public void precedesTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.precedes(b.val)");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.precedes(b.val)", false);
        Predicate result = loaderDoProcess.getPredicates().get();
        Predicate expected = new Comparison(
                new TimeSelector("a", TX_TO),
                LTE,
                new TimeSelector("b", TimeSelector.TimeField.VAL_FROM)
        );
        assertPredicateEquals(result, expected);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expected);

        // timestamp as caller
        loaderDoProcess = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx_to.precedes(b.val)");
        loaderRaw = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx_to.precedes(b.val)", false);
        result = loaderDoProcess.getPredicates().get();
        expected = new Comparison(
                new TimeSelector("a", TX_TO),
                LTE,
                new TimeSelector("b", TimeSelector.TimeField.VAL_FROM)
        );
        assertPredicateEquals(result, expected);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expected);
    }

    @Test
    public void succeedsTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.val.succeeds(b.tx)");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.val.succeeds(b.tx)", false);
        Predicate result = loaderDoProcess.getPredicates().get();
        Predicate expected = new Comparison(
                new TimeSelector("a", TimeSelector.TimeField.VAL_FROM),
                GTE,
                new TimeSelector("b", TX_TO)
        );
        assertPredicateEquals(result, expected);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expected);

        // timestamp as caller
        loaderDoProcess = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx_to.succeeds(b.val)");
        loaderRaw = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx_to.succeeds(b.val)", false);
        result = loaderDoProcess.getPredicates().get();
        expected = new Comparison(
                new TimeSelector("a", TX_TO),
                GTE,
                new TimeSelector("b", VAL_TO)
        );
        assertPredicateEquals(result, expected);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expected);
    }

    @Test
    public void asOfTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE e.asOf(1970-01-01)");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE e.asOf(1970-01-01)", false);
        Predicate result = loaderDoProcess.getPredicates().get();
        Predicate expected = new And(
                new Comparison(
                        new TimeSelector("e", TX_FROM),
                        LTE,
                        new TimeLiteral("1970-01-01")
                ),
                new Comparison(
                        new TimeSelector("e", TX_TO),
                        GTE,
                        new TimeLiteral("1970-01-01")
                )
        );
        assertPredicateEquals(result, expected);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expected);
    }

    @Test
    public void globalPredicateTest(){
        TimeLiteral l1 = new TimeLiteral("1970-01-01");
        TimeLiteral l2 = new TimeLiteral("2020-05-01");

        TimeSelector aFrom = new TimeSelector("a", TX_FROM);
        TimeSelector aTo = new TimeSelector("a", TX_TO);
        TimeSelector bFrom = new TimeSelector("b", TX_FROM);
        TimeSelector bTo = new TimeSelector("b", TX_TO);
        TimeSelector eFrom = new TimeSelector("e", TX_FROM);
        TimeSelector eTo = new TimeSelector("e", TX_TO);

        MaxTimePoint globalFrom = new MaxTimePoint(
                eFrom, aFrom, bFrom
        );
        MinTimePoint globalTo = new MinTimePoint(
                eTo, aTo, bTo
        );
        // only lhs global
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE tx.between(1970-01-01, 2020-05-01)");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE tx.between(1970-01-01, 2020-05-01)", false);
        Predicate result1 = loaderDoProcess.getPredicates().get();
        Predicate expectedProcessed1 = new And(
                // all froms <= l2
                new And(
                        new And(
                                new Comparison(eFrom, LTE, l2),
                                new Comparison(aFrom, LTE, l2)
                        ),
                        new Comparison(bFrom, LTE, l2)
                ),
                new And(
                        new And(
                                new Comparison(eTo, Comparator.GT, l1),
                                new Comparison(aTo, Comparator.GT, l1)
                        ),
                        new Comparison(bTo, Comparator.GT, l1)
                )
        );
        Predicate expectedRaw1 = new And(
            new Comparison(globalFrom, LTE, l2),
                new Comparison(globalTo, GT, l1)
        );
        assertPredicateEquals(expectedProcessed1, result1);

        assertPredicateEquals(expectedRaw1, loaderRaw.getPredicates().get());

        //only rhs global
        loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE 1970-01-01.precedes(tx)");
        loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE 1970-01-01.precedes(tx)", false);
        Predicate resultProcessed2 = loaderDoProcess.getPredicates().get();
        Or expectedProcessed2 = new Or(
                new Or(
                        new Comparison(l1, LTE, eFrom),
                        new Comparison(l1, LTE, aFrom)
                ),
                new Comparison(l1, LTE, bFrom)
        );
        Comparison expectedRaw2 = new Comparison(l1, LTE, globalFrom);
        assertPredicateEquals(expectedProcessed2, resultProcessed2);
        assertPredicateEquals(expectedRaw2, loaderRaw.getPredicates().get());

        //both sides global

        loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE val_to.after(tx_from)");
        loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE val_to.after(tx_from)", false);
        TimeSelector aValTo = new TimeSelector("a", VAL_TO);
        TimeSelector bValTo = new TimeSelector("b", VAL_TO);
        TimeSelector eValTo = new TimeSelector("e", VAL_TO);
        MinTimePoint globalValTo = new MinTimePoint(
                eValTo, bValTo, eValTo
        );
        Predicate resultProcessed3 = loaderDoProcess.getPredicates().get();
        Predicate expectedProcessed3 = new And(
                new And(
                        new And(
                                new And(
                                        new Comparison(eValTo, Comparator.GT, eFrom),
                                        new Comparison(aValTo, Comparator.GT, eFrom)
                                ),
                                new Comparison(bValTo, Comparator.GT, eFrom)
                        ),
                        new And(
                                new And(
                                        new Comparison(eValTo, Comparator.GT, aFrom),
                                        new Comparison(aValTo, Comparator.GT, aFrom)
                                ),
                                new Comparison(bValTo, Comparator.GT, aFrom)
                        )
                ),
                new And(
                        new And(
                                new Comparison(eValTo, Comparator.GT, bFrom),
                                new Comparison(aValTo, Comparator.GT, bFrom)
                        ),
                        new Comparison(bValTo, Comparator.GT, bFrom)
                )
        );
        Predicate expectedRaw3 = new Comparison(globalValTo, GT, globalFrom);
        assertPredicateEquals(expectedProcessed3, resultProcessed3);
        assertPredicateEquals(expectedRaw3, loaderRaw.getPredicates().get());
    }

    @Test
    public void intervallMergeAndJoinTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.merge(b.tx).succeeds(Interval(1970-01-01, 2020-05-01))");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.merge(b.tx).succeeds(Interval(1970-01-01, 2020-05-01))", false);
        TimeSelector aTxFrom = new TimeSelector("a", TX_FROM);
        TimeSelector aTxTo = new TimeSelector("a", TX_TO);
        TimeSelector bTxFrom = new TimeSelector("b", TX_FROM);
        TimeSelector bTxTo = new TimeSelector("b",TX_TO);
        TimeLiteral tl1 = new TimeLiteral("1970-01-01");
        TimeLiteral tl2 = new TimeLiteral("2020-05-01");
        Predicate expectedProcessed = new And(
                //succeeds
                new Or(
                    new Comparison(aTxFrom, GTE, tl2),
                    new Comparison(bTxFrom, GTE, tl2)
                ),
                //overlaps/meets
                new And(
                        new And(
                                new Comparison(aTxFrom, LTE, aTxTo),
                                new Comparison(aTxFrom, LTE, bTxTo)
                        ),
                        new And(
                                new Comparison(bTxFrom, LTE, aTxTo),
                                new Comparison(bTxFrom, LTE, bTxTo)
                        )
                )
        );
        Predicate expectedRaw = new And(
                new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), GTE, tl2),
                new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), LTE,
                        new MinTimePoint(aTxTo, bTxTo))
        );
        Predicate result = loaderDoProcess.getPredicates().get();
        assertPredicateEquals(expectedProcessed, result);
        assertPredicateEquals(expectedRaw, loaderRaw.getPredicates().get());

        /*
         * Join
         */
        loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.join(b.tx).succeeds(Interval(1970-01-01, 2020-05-01))");
        loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.join(b.tx).succeeds(Interval(1970-01-01, 2020-05-01))", false);
        expectedProcessed = new And(
                //succeeds
                new And(
                        new Comparison(aTxFrom, GTE, tl2),
                        new Comparison(bTxFrom, GTE, tl2)
                ),
                //overlaps/meets
                new And(
                        new And(
                                new Comparison(aTxFrom, LTE, aTxTo),
                                new Comparison(aTxFrom, LTE, bTxTo)
                        ),
                        new And(
                                new Comparison(bTxFrom, LTE, aTxTo),
                                new Comparison(bTxFrom, LTE, bTxTo)
                        )
                )
        );
        expectedRaw = new And(
                new Comparison(new MinTimePoint(aTxFrom, bTxFrom), GTE, tl2),
                new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), LTE,
                        new MinTimePoint(aTxTo, bTxTo))
        );
        result = loaderDoProcess.getPredicates().get();
        assertPredicateEquals(expectedProcessed, result);
        assertPredicateEquals(expectedRaw, loaderRaw.getPredicates().get());
        /*
         * Combine Merge And Join
         */
        loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.join(b.tx).precedes(a.tx.merge(b.tx))");
        loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.join(b.tx).precedes(a.tx.merge(b.tx))", false);
        expectedProcessed = new And(
                new And(
                        // actual precedes predicate
                        new And(
                                new Or(
                                      new Comparison(aTxTo, LTE, aTxFrom),
                                      new Comparison(aTxTo, LTE, bTxFrom)
                                ),
                                new Or(
                                        new Comparison(bTxTo, LTE, aTxFrom),
                                        new Comparison(bTxTo, LTE, bTxFrom)
                                )
                        ),
                        //first overlap/meet
                        new And(
                                new And(
                                        new Comparison(aTxFrom, LTE, aTxTo),
                                        new Comparison(aTxFrom, LTE, bTxTo)
                                ),
                                new And(
                                        new Comparison(bTxFrom, LTE, aTxTo),
                                        new Comparison(bTxFrom, LTE, bTxTo)
                                )
                        )
                ),
                //second overlap/meet
                new And(
                        new And(
                                new Comparison(aTxFrom, LTE, aTxTo),
                                new Comparison(aTxFrom, LTE, bTxTo)
                        ),
                        new And(
                                new Comparison(bTxFrom, LTE, aTxTo),
                                new Comparison(bTxFrom, LTE, bTxTo)
                        )
                )
        );
        expectedRaw = new And(
                new And(
                        new Comparison(new MaxTimePoint(aTxTo, bTxTo), LTE,
                                new MaxTimePoint(aTxFrom, bTxFrom)),
                        new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), LTE, new
                                MinTimePoint(aTxTo, bTxTo))
                ),
                new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), LTE, new
                        MinTimePoint(aTxTo, bTxTo))
        );
        result = loaderDoProcess.getPredicates().get();
        assertPredicateEquals(expectedProcessed, result);
        assertPredicateEquals(expectedRaw, loaderRaw.getPredicates().get());

        loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.merge(b.tx).precedes(a.tx.join(b.tx))");
        loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.merge(b.tx).precedes(a.tx.join(b.tx))", false);
        expectedProcessed = new And(
                new And(
                        // actual preceeds predicate
                        new Or(
                                new And(
                                        new Comparison(aTxTo, LTE, aTxFrom),
                                        new Comparison(aTxTo, LTE, bTxFrom)
                                ),
                                new And(
                                        new Comparison(bTxTo, LTE, aTxFrom),
                                        new Comparison(bTxTo, LTE, bTxFrom)
                                )
                        ),
                        //first overlap/meet
                        new And(
                                new And(
                                        new Comparison(aTxFrom, LTE, aTxTo),
                                        new Comparison(aTxFrom, LTE, bTxTo)
                                ),
                                new And(
                                        new Comparison(bTxFrom, LTE, aTxTo),
                                        new Comparison(bTxFrom, LTE, bTxTo)
                                )
                        )
                ),
                //second overlap/meet
                new And(
                        new And(
                                new Comparison(aTxFrom, LTE, aTxTo),
                                new Comparison(aTxFrom, LTE, bTxTo)
                        ),
                        new And(
                                new Comparison(bTxFrom, LTE, aTxTo),
                                new Comparison(bTxFrom, LTE, bTxTo)
                        )
                )
        );
        expectedRaw = new And(
                new And(
                        new Comparison(new MinTimePoint(aTxTo, bTxTo), LTE,
                                new MinTimePoint(aTxFrom, bTxFrom)),
                        new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), LTE, new
                                MinTimePoint(aTxTo, bTxTo))
                ),
                new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), LTE, new
                        MinTimePoint(aTxTo, bTxTo))
        );
        result = loaderDoProcess.getPredicates().get();
        assertPredicateEquals(result, expectedProcessed);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expectedRaw);
    }

    @Test
    public void containsTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.contains(b.val)");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.contains(b.val)", false);
        Predicate expected = new And(
                new Comparison(new TimeSelector("a", TX_FROM), LTE, new TimeSelector("b", VAL_FROM)),
                new Comparison(new TimeSelector("a", TX_TO), GTE, new TimeSelector("b", VAL_TO))
        );
        assertPredicateEquals(expected, loaderRaw.getPredicates().get());
        assertPredicateEquals(expected, loaderDoProcess.getPredicates().get());
    }

    @Test
    public void comparisonTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx_from > b.val_from AND 2013-06-01 <= a.val_to");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx_from > b.val_from AND 2013-06-01 <= a.val_to", false);
        Predicate expected = new And(
                new Comparison(new TimeSelector("a", TX_FROM), GT,
                        new TimeSelector("b", VAL_FROM)),
                new Comparison(new TimeLiteral("2013-06-01"), LTE,
                        new TimeSelector("a", VAL_TO)
                )
        );
        assertPredicateEquals(loaderRaw.getPredicates().get(), expected);
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
    }

    @Test
    public void immediatelyPrecedesTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.immediatelyPrecedes(e.val)");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.immediatelyPrecedes(e.val)", false);
        Predicate expected = new Comparison(
                new TimeSelector("a", TX_TO), EQ, new TimeSelector("e", VAL_FROM)
        );
        assertPredicateEquals(loaderRaw.getPredicates().get(), expected);
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
    }

    @Test
    public void immediatelySucceedsTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.immediatelySucceeds(e.val)");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.immediatelySucceeds(e.val)", false);
        Predicate expected = new Comparison(
                new TimeSelector("a", TX_FROM), EQ, new TimeSelector("e", VAL_TO)
        );
        assertPredicateEquals(loaderRaw.getPredicates().get(), expected);
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
    }

    @Test
    public void equalsTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.equals(e.val)");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.equals(e.val)", false);
        Predicate expected = new And(
                new Comparison(new TimeSelector("a", TX_FROM), EQ, new TimeSelector("e", VAL_FROM)),
                new Comparison(new TimeSelector("a", TX_TO), EQ, new TimeSelector("e", VAL_TO))
        );
        assertPredicateEquals(loaderRaw.getPredicates().get(), expected);
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
    }

    @Test
    public void minMaxTest(){
        TimeSelector aTxFrom = new TimeSelector("a", TX_FROM);
        TimeSelector bTxFrom = new TimeSelector("b", TX_FROM);
        TimeSelector eTxFrom = new TimeSelector("e", TX_FROM);
        TimeSelector aValTo = new TimeSelector("a", VAL_TO);
        TimeSelector bValTo = new TimeSelector("b", VAL_TO);
        TimeSelector eValTo = new TimeSelector("e", VAL_TO);
        TimeLiteral literal1 = new TimeLiteral("2020-05-05");
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE MIN(a.tx_from, b.tx_from, e.tx_from).before(2020-05-05)");
        GDLLoader loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE MIN(a.tx_from, b.tx_from, e.tx_from).before(2020-05-05)", false);
        Predicate expected = new Or(
                new Or(
                        new Comparison(aTxFrom, LT, literal1),
                        new Comparison(bTxFrom, LT, literal1)
                ),
                new Comparison(eTxFrom, LT, literal1)
        );
        Predicate expectedRaw = new Comparison(
                new MinTimePoint(aTxFrom, bTxFrom, eTxFrom), LT, literal1
        );
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expectedRaw);

        loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE MAX(a.tx_from, b.tx_from, e.tx_from).before(2020-05-05)");
        loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE MAX(a.tx_from, b.tx_from, e.tx_from).before(2020-05-05)", false);
        expected = new And(
                new And(
                        new Comparison(aTxFrom, LT, literal1),
                        new Comparison(bTxFrom, LT, literal1)
                ),
                new Comparison(eTxFrom, LT, literal1)
        );
        expectedRaw = new Comparison(
                new MaxTimePoint(aTxFrom, bTxFrom, eTxFrom), LT, literal1
        );
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expectedRaw);


        loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE MAX(a.tx_from, b.tx_from, e.tx_from).after(" +
                "MIN(a.val_to, b.val_to, e.val_to))");
        loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE MAX(a.tx_from, b.tx_from, e.tx_from).after(" +
                "MIN(a.val_to, b.val_to, e.val_to))", false);
        expected = new Or(
                //a, b
                new Or(
                        // a
                        new Or(new Or(
                                new Comparison(aTxFrom, GT, aValTo),
                                new Comparison(aTxFrom, GT, bValTo)
                        ),
                                new Comparison(aTxFrom, GT, eValTo)
                        ),
                        // b
                        new Or(
                                new Or(
                                        new Comparison(bTxFrom, GT, aValTo),
                                        new Comparison(bTxFrom, GT, bValTo)
                                ),
                                new Comparison(bTxFrom, GT, eValTo)
                        )
                ),
                //e
                new Or(
                        new Or(
                                new Comparison(eTxFrom, GT, aValTo),
                                new Comparison(eTxFrom, GT, bValTo)
                        ),
                        new Comparison(eTxFrom, GT, eValTo)
                )
        );
        expectedRaw = new Comparison(
                new MaxTimePoint(aTxFrom, bTxFrom, eTxFrom), GT,
                new MinTimePoint(aValTo, bValTo, eValTo)
        );
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expectedRaw);

        loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE MIN(a.tx_from, b.tx_from, e.tx_from).before(2020-05-05) AND" +
                " a.tx.succeeds(b.tx)");
        loaderRaw = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE MIN(a.tx_from, b.tx_from, e.tx_from).before(2020-05-05) AND" +
                " a.tx.succeeds(b.tx)", false);
        expected = new And(
                new Or(
                        new Or(
                                new Comparison(aTxFrom, LT, literal1),
                                new Comparison(bTxFrom, LT, literal1)
                        ),
                        new Comparison(eTxFrom, LT, literal1)
                ),
                new Comparison(aTxFrom, GTE, new TimeSelector("b", TX_TO))
        );
        expectedRaw = new And(
                new Comparison(
                        new MinTimePoint(aTxFrom, bTxFrom, eTxFrom), LT, literal1
                ),
                new Comparison(aTxFrom, GTE, new TimeSelector("b", TX_TO))
        );
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
        assertPredicateEquals(loaderRaw.getPredicates().get(), expectedRaw);

    }

    @Test
    public void longerThanTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.val.longerThan(10 days)");
        TimeSelector aValFrom = new TimeSelector("a", VAL_FROM);
        TimeSelector aValTo = new TimeSelector("a", VAL_TO);
        TimeSelector bValFrom = new TimeSelector("b", VAL_FROM);
        TimeSelector bValTo = new TimeSelector("b", VAL_TO);
        TimeSelector eValFrom = new TimeSelector("e", VAL_FROM);
        TimeSelector eValTo = new TimeSelector("e", VAL_TO);
        TimeConstant tenDays = new TimeConstant(10,0,0,0,0);
        Predicate expected = new Comparison(new PlusTimePoint(aValFrom, tenDays), LT, aValTo);
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);

        loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.val.longerThan(12 hours)");
        TimeConstant twelveHours = new TimeConstant(0,12,0,0,0);
        expected = new Comparison(new PlusTimePoint(aValFrom, twelveHours), LT, aValTo);
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
        System.out.println(loaderDoProcess.getPredicates());

        loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE val.longerThan(5 minutes)");
        TimeConstant fiveMinutes = new TimeConstant(0,0,5,0,0);
        expected = new And(
                // <e.val_to, <a.val_to
                new And(
                        //e
                        new And(
                                // e,a
                                new And(
                                        new Comparison(
                                                new PlusTimePoint(eValFrom, fiveMinutes), LT, eValTo),
                                        new Comparison(new PlusTimePoint(aValFrom, fiveMinutes), LT, eValTo)
                                ),
                                //b
                                new Comparison(new PlusTimePoint(bValFrom, fiveMinutes), LT, eValTo)
                        ),
                        //a
                        new And(
                                // e,a
                                new And(
                                        new Comparison(new PlusTimePoint(eValFrom, fiveMinutes), LT, aValTo),
                                        new Comparison(new PlusTimePoint(aValFrom, fiveMinutes), LT, aValTo)
                                ),
                                //b
                                new Comparison(new PlusTimePoint(bValFrom, fiveMinutes), LT, aValTo)
                        )
                ),
                // <b.val_to
                new And(
                        // e,a
                        new And(
                            new Comparison(new PlusTimePoint(eValFrom, fiveMinutes), LT, bValTo),
                                new Comparison(new PlusTimePoint(aValFrom, fiveMinutes), LT, bValTo)
                        ),
                        //b
                        new Comparison(new PlusTimePoint(bValFrom, fiveMinutes), LT, bValTo)
                )
        );
        System.out.println(expected);
        System.out.println(loaderDoProcess.getPredicates().get());
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
        System.out.println(loaderDoProcess.getPredicates());
    }

    /**
     * Does not fail iff {@code result==expected} or {@code result.switchSides()==expected}
     * @param result    predicate to compare
     * @param expected  predicate to compare
     */
    private void assertPredicateEquals(Predicate result, Predicate expected){
        assertTrue(result.equals(expected) || result.switchSides().equals(expected));
    }



    // checks if pred 1 is contained in pred 2
    private boolean predicateContainedIn(Predicate p1, Predicate p2){
        if(p1 instanceof Comparison){
            if(p2 instanceof Comparison){
                // TODO better equals method?
                return p1.equals(p2) || p1.switchSides().equals(p2);
            }
            else{
                Predicate lhs = p2.getArguments()[0];
                Predicate rhs = p2.getArguments()[1];
                return (predicateContainedIn(p1,lhs) || predicateContainedIn(p1,rhs));
            }
        }
        else{
            if(p2 instanceof Comparison){
                return false;
            }
            if(p1.toString().length() < p2.toString().length()){
                return false;
            }
            else if(p1.toString().length() == p2.toString().length()){
                return p1.equals(p2) || p1.switchSides().equals(p2);
            }
            else{
                Predicate lhs = p2.getArguments()[0];
                Predicate rhs = p2.getArguments()[1];
                return (predicateContainedIn(p1,lhs) || predicateContainedIn(p1,rhs));
            }
        }
    }






    private static final String DEFAULT_GRAPH_LABEL = "DefaultGraph";
    private static final String DEFAULT_VERTEX_LABEL = "DefaultVertex";
    private static final String DEFAULT_EDGE_LABEL = "DefaultEdge";

    private GDLLoader getLoaderFromGDLString(String gdlString) {
        return getLoaderFromGDLString(gdlString, true);
    }

    private GDLLoader getLoaderFromGDLString(String gdlString, boolean processPredicates){
        GDLLexer lexer = new GDLLexer(new ANTLRInputStream(gdlString));
        GDLParser parser = new GDLParser(new CommonTokenStream(lexer));

        ParseTreeWalker walker = new ParseTreeWalker();
        GDLLoader loader = new GDLLoader(DEFAULT_GRAPH_LABEL, DEFAULT_VERTEX_LABEL, DEFAULT_EDGE_LABEL,
                processPredicates);
        walker.walk(loader, parser.database());
        return loader;
    }
}
