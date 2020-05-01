package org.s1ck.gdl;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;
import org.s1ck.gdl.model.comparables.Literal;
import org.s1ck.gdl.model.comparables.PropertySelector;
import org.s1ck.gdl.model.comparables.time.MaxTimePoint;
import org.s1ck.gdl.model.comparables.time.MinTimePoint;
import org.s1ck.gdl.model.comparables.time.TimeLiteral;
import org.s1ck.gdl.model.comparables.time.TimeSelector;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.overlaps(Interval(1970-01-01,1970-01-02))");
        assertPredicateEquals(loader.getPredicates().get(),
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
    }

    @Test
    public void afterTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.val_from.after(1970-01-01T00:00:01) AND a.tx_to.after(1970-01-01T00:00:01)");
        Predicate result = loader.getPredicates().get();
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
    }

    @Test
    public void fromToTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.fromTo(1970-01-01, b.tx_to)");
        Predicate result = loader.getPredicates().get();
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
    }

    @Test
    public void betweenTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.between(b.tx_from, 2020-04-28T10:39:15)");
        Predicate result = loader.getPredicates().get();
        Predicate expected = new And(
                new Comparison(
                        new TimeSelector("a", TimeSelector.TimeField.TX_FROM),
                        Comparator.LTE,
                        new TimeLiteral("2020-04-28T10:39:15")
                ),
                new Comparison(
                        new TimeSelector("a", TimeSelector.TimeField.TX_TO),
                        Comparator.GT,
                        new TimeSelector("b", TimeSelector.TimeField.TX_FROM)
                )
        );
        assertPredicateEquals(result, expected);
    }

    @Test
    public void precedesTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.precedes(b.val)");
        Predicate result = loader.getPredicates().get();
        Predicate expected = new Comparison(
                new TimeSelector("a", TimeSelector.TimeField.TX_TO),
                Comparator.LTE,
                new TimeSelector("b", TimeSelector.TimeField.VAL_FROM)
        );
        assertPredicateEquals(result, expected);

        // timestamp as caller
        loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx_to.precedes(b.val)");
        result = loader.getPredicates().get();
        expected = new Comparison(
                new TimeSelector("a", TimeSelector.TimeField.TX_TO),
                Comparator.LTE,
                new TimeSelector("b", TimeSelector.TimeField.VAL_FROM)
        );
        assertPredicateEquals(result, expected);
    }

    @Test
    public void succeedsTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.val.succeeds(b.tx)");
        Predicate result = loader.getPredicates().get();
        Predicate expected = new Comparison(
                new TimeSelector("a", TimeSelector.TimeField.VAL_FROM),
                Comparator.GTE,
                new TimeSelector("b", TimeSelector.TimeField.TX_TO)
        );
        assertPredicateEquals(result, expected);

        // timestamp as caller
        loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx_to.succeeds(b.val)");
        result = loader.getPredicates().get();
        expected = new Comparison(
                new TimeSelector("a", TimeSelector.TimeField.TX_TO),
                Comparator.GTE,
                new TimeSelector("b", TimeSelector.TimeField.VAL_TO)
        );
        assertPredicateEquals(result, expected);
    }

    @Test
    public void asOfTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE e.asOf(1970-01-01)");
        Predicate result = loader.getPredicates().get();
        Predicate expected = new And(
                new Comparison(
                        new TimeSelector("e", TimeSelector.TimeField.TX_FROM),
                        Comparator.LTE,
                        new TimeLiteral("1970-01-01")
                ),
                new Comparison(
                        new TimeSelector("e", TimeSelector.TimeField.TX_TO),
                        Comparator.GTE,
                        new TimeLiteral("1970-01-01")
                )
        );
        assertPredicateEquals(result, expected);
    }

    @Test
    public void globalPredicateTest(){
        TimeLiteral l1 = new TimeLiteral("1970-01-01");
        TimeLiteral l2 = new TimeLiteral("2020-05-01");

        TimeSelector aFrom = new TimeSelector("a", TimeSelector.TimeField.TX_FROM);
        TimeSelector aTo = new TimeSelector("a", TimeSelector.TimeField.TX_TO);
        TimeSelector bFrom = new TimeSelector("b", TimeSelector.TimeField.TX_FROM);
        TimeSelector bTo = new TimeSelector("b", TimeSelector.TimeField.TX_TO);
        TimeSelector eFrom = new TimeSelector("e", TimeSelector.TimeField.TX_FROM);
        TimeSelector eTo = new TimeSelector("e", TimeSelector.TimeField.TX_TO);
        // only lhs global
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE tx.between(1970-01-01, 2020-05-01)");
        Predicate result1 = loader.getPredicates().get();
        Predicate expected1 = new And(
                // all froms <= l2
                new And(
                        new And(
                                new Comparison(eFrom, Comparator.LTE, l2),
                                new Comparison(aFrom, Comparator.LTE, l2)
                        ),
                        new Comparison(bFrom, Comparator.LTE, l2)
                ),
                new And(
                        new And(
                                new Comparison(eTo, Comparator.GT, l1),
                                new Comparison(aTo, Comparator.GT, l1)
                        ),
                        new Comparison(bTo, Comparator.GT, l1)
                )
        );
        assertPredicateEquals(expected1, result1);

        //only rhs global
        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE 1970-01-01.precedes(tx)");
        Predicate result2 = loader.getPredicates().get();
        Or expected2 = new Or(
                new Or(
                        new Comparison(l1, Comparator.LTE, eFrom),
                        new Comparison(l1, Comparator.LTE, aFrom)
                ),
                new Comparison(l1, Comparator.LTE, bFrom)
        );
        assertPredicateEquals(expected2, result2);

        //both sides global

        loader = getLoaderFromGDLString("MATCH (a)-[e]->(b) " +
                "WHERE val_to.after(tx_from)");
        TimeSelector aValTo = new TimeSelector("a", TimeSelector.TimeField.VAL_TO);
        TimeSelector bValTo = new TimeSelector("b", TimeSelector.TimeField.VAL_TO);
        TimeSelector eValTo = new TimeSelector("e", TimeSelector.TimeField.VAL_TO);
        Predicate result3 = loader.getPredicates().get();
        Predicate expected3 = new And(
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
        assertPredicateEquals(expected3, result3);
    }


    @Test
    public void graphTimeStampTest(){

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
        GDLLexer lexer = new GDLLexer(new ANTLRInputStream(gdlString));
        GDLParser parser = new GDLParser(new CommonTokenStream(lexer));

        ParseTreeWalker walker = new ParseTreeWalker();
        GDLLoader loader = new GDLLoader(DEFAULT_GRAPH_LABEL, DEFAULT_VERTEX_LABEL, DEFAULT_EDGE_LABEL);
        walker.walk(loader, parser.database());
        return loader;
    }
}
