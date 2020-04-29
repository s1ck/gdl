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
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GDLLoaderTemporalTest {

    @Test
    public void simpleTimestampFunctionsTest(){
        GDLLoader loader = getLoaderFromGDLString(
                "MATCH (alice)-[e1:knows {since : 2014}]->(bob) (alice)-[e2:knows {since : 2013}]->(eve) " +
                        "WHERE (e1.tx_from.before(2017-01-01) AND e2.val_to.asOf(2018-12-23T15:55:23)) OR e1.knows>2010");
        System.out.println("Edges: "+loader.getEdgeCache());
        System.out.println("Vertices: "+loader.getVertexCache());
        System.out.println("Predicates: "+loader.getPredicates());
        assertTrue(predicateContainedIn(
                new Comparison(new TimeSelector("e1", "tx_from"), Comparator.LT, new TimeLiteral("2017-01-01")),
                loader.getPredicates().get().switchSides()));
        assertTrue(predicateContainedIn(
                new Comparison(new TimeSelector("e2", "val_to"), Comparator.LTE, new TimeLiteral("2018-12-23T15:55:23")),
                loader.getPredicates().get().switchSides()));
        assertTrue(predicateContainedIn(
                new Comparison(new PropertySelector("e1", "knows"), Comparator.GT, new Literal(2010)),
                loader.getPredicates().get().switchSides()));
    }

    @Test
    public void periodLiteralTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.tx.overlaps(Interval(1970-01-01,1970-01-02))");
        assertEquals(loader.getPredicates().get().toString(),
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
                ).toString()
                );
    }

    @Test
    public void afterTest(){
        GDLLoader loader = getLoaderFromGDLString("MATCH (a)-->(b) " +
                "WHERE a.val_from.after(1970-01-01T00:00:01)");
        Predicate result = loader.getPredicates().get();
        Predicate expected = new Comparison(
                new TimeLiteral("1970-01-01T00:00:01"),
                Comparator.LT,
                new TimeSelector("a", "val_from"));
        assertEquals(result.toString(), expected.toString());
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
        assertEquals(result.toString(), expected.toString());
    }

    @Test
    public void graphTimeStampTest(){

    }



    // checks if pred 1 is contained in pred 2
    private boolean predicateContainedIn(Predicate p1, Predicate p2){
        if(p1 instanceof Comparison){
            if(p2 instanceof Comparison){
                // TODO better equals method?
                return p1.toString().equals(p2.toString());
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
                return p1.toString().equals(p2.toString());
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
