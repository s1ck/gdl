package org.s1ck.gdl;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.time.*;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import static org.junit.Assert.assertTrue;
import static org.s1ck.gdl.model.comparables.time.TimeSelector.TimeField.*;
import static org.s1ck.gdl.utils.Comparator.*;

public class GDLLoaderTemporalTest {


    @Test
    public void periodLiteralTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.overlaps(Interval(1970-01-01,1970-01-02))");
        TimeLiteral tl1 = new TimeLiteral("1970-01-01");
        TimeLiteral tl2 = new TimeLiteral("1970-01-02");

        assertPredicateEquals(loader.getPredicates().get(),
                new And(
                new Comparison(
                        new MaxTimePoint(new TimeSelector("a", TX_FROM),
                                tl1),
                        LT,
                        new MinTimePoint(new TimeSelector("a", TX_TO),
                                tl2)
                ),
                        new Comparison(tl1, LTE, tl2)));
    }

    @Test
    public void afterTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.val_from.after(1970-01-01T00:00:01) AND a.tx_to.after(1970-01-01T00:00:01)");
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
        assertPredicateEquals(loader.getPredicates().get(), expected);
    }

    @Test
    public void fromToTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.fromTo(1970-01-01, b.tx_to)");
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
        assertPredicateEquals(loader.getPredicates().get(), expected);
    }

    @Test
    public void betweenTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.between(b.tx_from, 2020-04-28T10:39:15)");
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
        assertPredicateEquals(loader.getPredicates().get(), expected);
    }

    @Test
    public void precedesTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.precedes(b.val)");
        Predicate expected = new Comparison(
                new TimeSelector("a", TX_TO),
                LTE,
                new TimeSelector("b", TimeSelector.TimeField.VAL_FROM)
        );
        assertPredicateEquals(loader.getPredicates().get(), expected);

        // timestamp as caller
        loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx_to.precedes(b.val)");
        expected = new Comparison(
                new TimeSelector("a", TX_TO),
                LTE,
                new TimeSelector("b", TimeSelector.TimeField.VAL_FROM)
        );
        assertPredicateEquals(loader.getPredicates().get(), expected);
    }

    @Test
    public void succeedsTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.val.succeeds(b.tx)");
        Predicate expected = new Comparison(
                new TimeSelector("a", TimeSelector.TimeField.VAL_FROM),
                GTE,
                new TimeSelector("b", TX_TO)
        );
        assertPredicateEquals(loader.getPredicates().get(), expected);

        // timestamp as caller
        loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx_to.succeeds(b.val)");
        expected = new Comparison(
                new TimeSelector("a", TX_TO),
                GTE,
                new TimeSelector("b", VAL_TO)
        );
        assertPredicateEquals(loader.getPredicates().get(), expected);
    }

    @Test
    public void asOfTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE e.asOf(1970-01-01)");
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
        assertPredicateEquals(loader.getPredicates().get(), expected);
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
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE tx.between(1970-01-01, 2020-05-01)");
        Predicate expected1 = new And(
            new Comparison(globalFrom, LTE, l2),
                new Comparison(globalTo, GT, l1)
        );
        assertPredicateEquals(expected1, loader.getPredicates().get());

        //only rhs global
        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE 1970-01-01.precedes(tx)");
        Comparison expected2 = new Comparison(l1, LTE, globalFrom);
        assertPredicateEquals(expected2, loader.getPredicates().get());

        //both sides global
        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE val_to.after(tx_from)");
        TimeSelector aValTo = new TimeSelector("a", VAL_TO);
        TimeSelector bValTo = new TimeSelector("b", VAL_TO);
        TimeSelector eValTo = new TimeSelector("e", VAL_TO);
        MinTimePoint globalValTo = new MinTimePoint(
                eValTo, bValTo, eValTo
        );
        Predicate expected3 = new Comparison(globalValTo, GT, globalFrom);
        //assertPredicateEquals(expectedProcessed3, resultProcessed3);
        assertPredicateEquals(expected3, loader.getPredicates().get());
    }

    @Test
    public void intervalMergeAndJoinTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.merge(b.tx).succeeds(Interval(1970-01-01, 2020-05-01))");
        TimeSelector aTxFrom = new TimeSelector("a", TX_FROM);
        TimeSelector aTxTo = new TimeSelector("a", TX_TO);
        TimeSelector bTxFrom = new TimeSelector("b", TX_FROM);
        TimeSelector bTxTo = new TimeSelector("b",TX_TO);
        TimeLiteral tl1 = new TimeLiteral("1970-01-01");
        TimeLiteral tl2 = new TimeLiteral("2020-05-01");

        Predicate expected = new And(
                new And(
                new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), GTE, tl2),
                new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), LTE,
                        new MinTimePoint(aTxTo, bTxTo))),
                new Comparison(tl1, LTE, tl2)
        );
        assertPredicateEquals(expected, loader.getPredicates().get());

        /*
         * Join
         */
        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.join(b.tx).succeeds(Interval(1970-01-01, 2020-05-01))");
        expected = new And(
                new And(
                new Comparison(new MinTimePoint(aTxFrom, bTxFrom), GTE, tl2),
                new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), LTE,
                        new MinTimePoint(aTxTo, bTxTo))),
                new Comparison(tl1, LTE, tl2)
        );
        assertPredicateEquals(expected, loader.getPredicates().get());

        /*
         * Combine Merge And Join
         */
        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.join(b.tx).precedes(a.tx.merge(b.tx))");
        expected = new And(
                new And(
                        new Comparison(new MaxTimePoint(aTxTo, bTxTo), LTE,
                                new MaxTimePoint(aTxFrom, bTxFrom)),
                        new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), LTE, new
                                MinTimePoint(aTxTo, bTxTo))
                ),
                new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), LTE, new
                        MinTimePoint(aTxTo, bTxTo))
        );
        assertPredicateEquals(expected, loader.getPredicates().get());

        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.merge(b.tx).precedes(a.tx.join(b.tx))");
        expected = new And(
                new And(
                        new Comparison(new MinTimePoint(aTxTo, bTxTo), LTE,
                                new MinTimePoint(aTxFrom, bTxFrom)),
                        new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), LTE, new
                                MinTimePoint(aTxTo, bTxTo))
                ),
                new Comparison(new MaxTimePoint(aTxFrom, bTxFrom), LTE, new
                        MinTimePoint(aTxTo, bTxTo))
        );
        assertPredicateEquals(loader.getPredicates().get(), expected);
    }

    @Test
    public void containsTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.contains(b.val)");
        Predicate expected = new And(
                new Comparison(new TimeSelector("a", TX_FROM), LTE, new TimeSelector("b", VAL_FROM)),
                new Comparison(new TimeSelector("a", TX_TO), GTE, new TimeSelector("b", VAL_TO))
        );
        assertPredicateEquals(expected, loaderDoProcess.getPredicates().get());
    }

    @Test
    public void comparisonTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx_from > b.val_from AND 2013-06-01 <= a.val_to");
        Predicate expected = new And(
                new Comparison(new TimeSelector("a", TX_FROM), GT,
                        new TimeSelector("b", VAL_FROM)),
                new Comparison(new TimeLiteral("2013-06-01"), LTE,
                        new TimeSelector("a", VAL_TO)
                )
        );
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
    }

    @Test
    public void immediatelyPrecedesTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.immediatelyPrecedes(e.val)");
        Predicate expected = new Comparison(
                new TimeSelector("a", TX_TO), EQ, new TimeSelector("e", VAL_FROM)
        );
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
    }

    @Test
    public void immediatelySucceedsTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.immediatelySucceeds(e.val)");
        Predicate expected = new Comparison(
                new TimeSelector("a", TX_FROM), EQ, new TimeSelector("e", VAL_TO)
        );
        assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
    }

    @Test
    public void equalsTest(){
        GDLLoader loaderDoProcess = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.tx.equals(e.val)");
        Predicate expected = new And(
                new Comparison(new TimeSelector("a", TX_FROM), EQ, new TimeSelector("e", VAL_FROM)),
                new Comparison(new TimeSelector("a", TX_TO), EQ, new TimeSelector("e", VAL_TO))
        );
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

        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE MIN(a.tx_from, b.tx_from, e.tx_from).before(2020-05-05)");

        Predicate expected = new Comparison(
                new MinTimePoint(aTxFrom, bTxFrom, eTxFrom), LT, literal1
        );
        assertPredicateEquals(loader.getPredicates().get(), expected);


        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE MAX(a.tx_from, b.tx_from, e.tx_from).before(2020-05-05)");

        expected = new Comparison(
                new MaxTimePoint(aTxFrom, bTxFrom, eTxFrom), LT, literal1
        );
        assertPredicateEquals(loader.getPredicates().get(), expected);


        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE MAX(a.tx_from, b.tx_from, e.tx_from).after(" +
                "MIN(a.val_to, b.val_to, e.val_to))");
        expected = new Comparison(
                new MaxTimePoint(aTxFrom, bTxFrom, eTxFrom), GT,
                new MinTimePoint(aValTo, bValTo, eValTo)
        );
        assertPredicateEquals(loader.getPredicates().get(), expected);


        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE MIN(a.tx_from, b.tx_from, e.tx_from).before(2020-05-05) AND" +
                " a.tx.succeeds(b.tx)");
        expected = new And(
                new Comparison(
                        new MinTimePoint(aTxFrom, bTxFrom, eTxFrom), LT, literal1
                ),
                new Comparison(aTxFrom, GTE, new TimeSelector("b", TX_TO))
        );
        //assertPredicateEquals(loaderDoProcess.getPredicates().get(), expected);
        assertPredicateEquals(loader.getPredicates().get(), expected);

    }

    @Test
    public void longerThanTest(){
        lengthComparisonTest("longerThan", GT);
    }

    @Test
    public void shorterThanTest(){
        lengthComparisonTest("shorterThan", LT);
    }

    @Test
    public void lengthAtLeastTest(){
        lengthComparisonTest("lengthAtLeast", GTE);
    }

    @Test
    public void lengthAtMostTest(){
        lengthComparisonTest("lengthAtMost", LTE);
    }

    private void lengthComparisonTest(String operator, Comparator comparator){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.val."+operator+"(Days(80))");

        TimeSelector aValFrom = new TimeSelector("a", VAL_FROM);
        TimeSelector aValTo = new TimeSelector("a", VAL_TO);
        TimeSelector bValFrom = new TimeSelector("b", VAL_FROM);
        TimeSelector bValTo = new TimeSelector("b", VAL_TO);
        TimeSelector eValFrom = new TimeSelector("e", VAL_FROM);
        TimeSelector eValTo = new TimeSelector("e", VAL_TO);
        TimeConstant eightyDays = new TimeConstant(80,0,0,0,0);

        Duration valDuration = new Duration(aValFrom, aValTo);
        Comparison valDurationPred = new Comparison(aValFrom, LTE, aValTo);

        Predicate expected = new And(valDurationPred,
                new Comparison(valDuration, comparator, eightyDays));
        assertPredicateEquals(loader.getPredicates().get(), expected);

        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.val."+operator+"(Hours(12))");
        TimeConstant twelveHours = new TimeConstant(0,12,0,0,0);
        expected = new And(valDurationPred,
                new Comparison(valDuration, comparator, twelveHours));
        assertPredicateEquals(loader.getPredicates().get(), expected);

        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE val."+operator+"(Minutes(5))");
        TimeConstant fiveMinutes = new TimeConstant(0,0,5,0,0);
        MaxTimePoint globalValFrom = new MaxTimePoint(eValFrom, aValFrom, bValFrom);
        MinTimePoint globalValTo = new MinTimePoint(eValTo, aValTo, bValTo);
        Duration globalValDuration = new Duration(globalValFrom, globalValTo);
        Comparison globalValPred = new Comparison(globalValFrom, LTE, globalValTo);
        expected = new And(globalValPred,
                new Comparison(globalValDuration, comparator, fiveMinutes));
        assertPredicateEquals(loader.getPredicates().get(), expected);

        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.val.merge(b.val)."+operator+"(Hours(20))");
        TimeConstant twentyHours = new TimeConstant(0,20,0,0,0);
        MaxTimePoint mergeFrom = new MaxTimePoint(aValFrom, bValFrom);
        MinTimePoint mergeTo = new MinTimePoint(aValTo, bValTo);
        Duration mergeDuration = new Duration(mergeFrom, mergeTo);
        Comparison mergeDurationPred = new Comparison(mergeFrom, LTE, mergeTo);
        expected = new And(
                new And(
                mergeDurationPred,
                new Comparison(mergeDuration, comparator, twentyHours)),
                new Comparison(mergeFrom, LTE, mergeTo)
        );
        assertPredicateEquals(loader.getPredicates().get(), expected);

        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.val.merge(b.val)."+operator+"(e.val.join(b.val))");
        MinTimePoint joinFrom = new MinTimePoint(eValFrom, bValFrom);
        MaxTimePoint joinTo = new MaxTimePoint(eValTo, bValTo);
        Duration joinDuration = new Duration(joinFrom, joinTo);
        Comparison joinDurationPred = new Comparison(joinFrom, LTE, joinTo);
        mergeDurationPred = new Comparison(mergeFrom, LTE, mergeTo);
        Comparison ebOverlap = new Comparison(new MaxTimePoint(eValFrom, bValFrom), LTE,
                new MinTimePoint(eValTo, bValTo));
        expected = new And(
                new And(
                        new And(
                                new And(
                                mergeDurationPred,
                                joinDurationPred),
                                new Comparison(mergeDuration, comparator, joinDuration)),
                        ebOverlap),
                new Comparison(mergeFrom, LTE, mergeTo)
        );

        assertPredicateEquals(loader.getPredicates().get(), expected);

        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE Interval(a.val_from, b.val_to)."+operator+"(Days(4))");
        TimeConstant fourDays = new TimeConstant(4,0,0,0,0);
        Duration intervalDuration = new Duration(aValFrom, bValTo);
        Comparison intervalDurationPred = new Comparison(aValFrom, LTE, bValTo);
        expected = new And(
                new And(
                    intervalDurationPred,
                    new Comparison(intervalDuration, comparator, fourDays)),
                new Comparison(aValFrom, LTE, bValTo));
        assertPredicateEquals(loader.getPredicates().get(), expected);

        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE a.val."+operator+"(b.val)");
        Duration aVal = new Duration(aValFrom, aValTo);
        Duration bVal = new Duration(bValFrom, bValTo);
        Comparison aValPred = new Comparison(aValFrom, LTE, aValTo);
        Comparison bValPred = new Comparison(bValFrom, LTE, bValTo);
        expected = new And(
                new And(
                        aValPred, bValPred
                ),
                new Comparison(aVal, comparator, bVal));
        assertPredicateEquals(loader.getPredicates().get(), expected);

        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE val."+operator+"(tx)");
        TimeSelector eTxFrom = new TimeSelector("e", TX_FROM);
        TimeSelector aTxFrom = new TimeSelector("a", TX_FROM);
        TimeSelector bTxFrom = new TimeSelector("b", TX_FROM);
        TimeSelector eTxTo = new TimeSelector("e", TX_TO);
        TimeSelector aTxTo = new TimeSelector("a", TX_TO);
        TimeSelector bTxTo = new TimeSelector("b", TX_TO);
        MaxTimePoint globalTxFrom = new MaxTimePoint(eTxFrom, aTxFrom, bTxFrom);
        MinTimePoint globalTxTo = new MinTimePoint(eTxTo, aTxTo, bTxTo);
        Duration globalTxDuration = new Duration(globalTxFrom, globalTxTo);
        Comparison globalTxPred = new Comparison(globalTxFrom, LTE, globalTxTo);
        expected = new And(
                new And(
                        globalValPred, globalTxPred
                ),
                new Comparison(globalValDuration, comparator, globalTxDuration));
        assertPredicateEquals(loader.getPredicates().get(), expected);

        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE val."+operator+"(Interval(2020-05-01, 2020-05-05))");
        TimeLiteral l1 = new TimeLiteral("2020-05-01");
        TimeLiteral l2 = new TimeLiteral("2020-05-05");
        Duration constantInterval = new Duration(l1, l2);
        Comparison constantPred = new Comparison(l1, LTE, l2);
        expected = new And(
                new And(
                new And(globalValPred, constantPred),
        new Comparison(globalValDuration, comparator, constantInterval)),
        new Comparison(l1, LTE, l2));
        assertPredicateEquals(loader.getPredicates().get(), expected);
    }

    /**
     * Does not fail iff {@code result==expected} or {@code result.switchSides()==expected}
     * @param result    predicate to compare
     * @param expected  predicate to compare
     */
    private void assertPredicateEquals(Predicate result, Predicate expected){
        assertTrue(result.equals(expected) || result.switchSides().equals(expected)
        || result.toString().equals(expected.toString()) ||
                result.switchSides().toString().equals(expected.toString()));
    }


    private static final String DEFAULT_GRAPH_LABEL = "DefaultGraph";
    private static final String DEFAULT_VERTEX_LABEL = "DefaultVertex";
    private static final String DEFAULT_EDGE_LABEL = "DefaultEdge";

    private GDLLoader getLoaderFromGDLString(String gdlString){
        GDLLexer lexer = new GDLLexer(new ANTLRInputStream(gdlString));
        GDLParser parser = new GDLParser(new CommonTokenStream(lexer));

        ParseTreeWalker walker = new ParseTreeWalker();
        GDLLoader loader = new GDLLoader(DEFAULT_GRAPH_LABEL, DEFAULT_VERTEX_LABEL, DEFAULT_EDGE_LABEL);
        walker.walk(loader, parser.database());
        return loader;
    }
}
