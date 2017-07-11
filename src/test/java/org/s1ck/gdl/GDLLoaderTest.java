package org.s1ck.gdl;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;
import org.s1ck.gdl.exceptions.InvalidReferenceException;
import org.s1ck.gdl.model.Edge;
import org.s1ck.gdl.model.Element;
import org.s1ck.gdl.model.Graph;
import org.s1ck.gdl.model.GraphElement;
import org.s1ck.gdl.model.Vertex;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class GDLLoaderTest {

  // --------------------------------------------------------------------------------------------
  //  Vertex only tests
  // --------------------------------------------------------------------------------------------

  @Test
  public void readVertexTest() {
    GDLLoader loader = getLoaderFromGDLString("()");

    validateCollectionSizes(loader, 0, 1, 0);
    validateCacheSizes(loader,
      0, 0, 0,
      0, 1, 0);

    Optional<Vertex> vertex = loader.getVertices().stream().findFirst();
    assertTrue(vertex.isPresent());
    assertEquals(1, vertex.get().getLabels().size());
    assertEquals(loader.getDefaultVertexLabel(), vertex.get().getLabels().get(0));
  }

  @Test
  public void readVertexWithVariableTest() {
    GDLLoader loader = getLoaderFromGDLString("(var)");

    validateCollectionSizes(loader, 0, 1, 0);
    validateCacheSizes(loader,
      0, 1, 0,
      0, 0, 0);
    assertTrue("vertex not cached", loader.getVertexCache().containsKey("var"));
    assertNotNull("vertex was null", loader.getVertexCache().get("var"));
  }

  @Test
  public void readVertexWithLabelTest() {
    GDLLoader loader = getLoaderFromGDLString("(var:Label)");
    Vertex v = loader.getVertexCache().get("var");
    assertEquals("vertex has wrong label", "Label", v.getLabels().get(0));
  }

  @Test
  public void readVertexWithMultipleLabelsTest() {
    GDLLoader loader = getLoaderFromGDLString("(var:Label1:Label2:Label3)");
    Vertex v = loader.getVertexCache().get("var");
    assertEquals(
      "vertex has wrong label",
      Arrays.asList("Label1", "Label2", "Label3"), v
      .getLabels()
    );
  }

  @Test
  public void readVertexWithCamelCaseLabelTest() {
    GDLLoader loader = getLoaderFromGDLString("(var:BlogPost)");
    Vertex v = loader.getVertexCache().get("var");
    assertEquals("vertex has wrong label", "BlogPost", v.getLabel());
  }

  @Test
  public void readVertexWithPropertiesTest() {
    GDLLoader loader = getLoaderFromGDLString(String.format("(var %s)", PROPERTIES_STRING));

    validateProperties(loader.getVertexCache().get("var"));
  }

  @Test
  public void readVertexWithVariablesTest() {
    GDLLoader loader = getLoaderFromGDLString("(var)");

    Vertex v = loader.getVertices().iterator().next();
    assertEquals("vertex has wrong variable", "var", v.getVariable());
  }

  // --------------------------------------------------------------------------------------------
  //  Edge only tests
  // --------------------------------------------------------------------------------------------

  @Test
  public void readEdgeTest() {
    GDLLoader loader = getLoaderFromGDLString("()-->()");
    validateCollectionSizes(loader, 0, 2, 1);
    validateCacheSizes(loader,
      0, 0, 0,
      0, 2, 1);

    Optional<Edge> edge = loader.getEdges().stream().findFirst();
    assertTrue(edge.isPresent());
    assertFalse("edge should not have variable length", edge.get().hasVariableLength());
    assertEquals("edge has wrong label", loader.getDefaultEdgeLabel(), edge.get().getLabel());
  }

  @Test
  public void readOutgoingEdgeWithVariablesTest() {
    GDLLoader loader = getLoaderFromGDLString("(v1)-[e1]->(v2)");
    validateCollectionSizes(loader, 0, 2, 1);
    validateCacheSizes(loader,
      0, 2, 1,
      0, 0, 0);

    assertTrue("edge not cached", loader.getEdgeCache().containsKey("e1"));
    Edge e1 = loader.getEdgeCache().get("e1");
    assertNotNull("e was null", e1);
    assertFalse("edge should not have variable length", e1.hasVariableLength());
    assertEquals(loader.getDefaultEdgeLabel(), e1.getLabel());
    Vertex v1 = loader.getVertexCache().get("v1");
    Vertex v2 = loader.getVertexCache().get("v2");

    assertEquals("wrong source vertex identifier", (Long) v1.getId(), e1.getSourceVertexId());
    assertEquals("wrong target vertex identifier", (Long) v2.getId(), e1.getTargetVertexId());
  }

  @Test
  public void readIncomingEdgeWithVariablesTest() {
    GDLLoader loader = getLoaderFromGDLString("(v1)<-[e1]-(v2)");

    assertEquals("wrong number of edges", 1, loader.getEdges().size());
    assertEquals("wrong number of cached edges", 1, loader.getEdgeCache().size());
    assertTrue("edge not cached", loader.getEdgeCache().containsKey("e1"));
    Edge e1 = loader.getEdgeCache().get("e1");
    assertNotNull("e was null", e1);
    assertFalse("edge should not have variable length", e1.hasVariableLength());

    Vertex v1 = loader.getVertexCache().get("v1");
    Vertex v2 = loader.getVertexCache().get("v2");

    assertEquals("wrong source vertex identifier", (Long) v1.getId(), e1.getTargetVertexId());
    assertEquals("wrong target vertex identifier", (Long) v2.getId(), e1.getSourceVertexId());
  }

  @Test
  public void readEdgeWithNoLabelTest() throws Exception {
    GDLLoader loader = getLoaderFromGDLString("()-[e]->()");
    Edge e = loader.getEdgeCache().get("e");
    assertEquals("edge has wrong label", loader.getDefaultEdgeLabel(), e.getLabel());
  }

  @Test
  public void readEdgeWithLabelTest() {
    GDLLoader loader = getLoaderFromGDLString("()-[e:knows]->()");
    Edge e = loader.getEdgeCache().get("e");
    assertFalse("edge should not have variable length", e.hasVariableLength());
    assertEquals("edge has the wrong number of labels", 1, e.getLabels().size());
    assertEquals("edge has wrong label", "knows", e.getLabel());
  }

  @Test
  public void readEdgeWithCamelCaseLabelTest() {
    GDLLoader loader = getLoaderFromGDLString("()-[e:hasInterest]->()");
    Edge e = loader.getEdgeCache().get("e");
    assertEquals("edge has wrong label", "hasInterest", e.getLabel());
  }

  @Test(expected = RuntimeException.class)
  public void readEdgeWithMultipleLabelsTest() {
    getLoaderFromGDLString("()-[e:hasInterest:foobar]->()");
  }

  @Test
  public void readEdgeWithPropertiesTest() {
    GDLLoader loader = getLoaderFromGDLString(String.format("()-[e %s]->()", PROPERTIES_STRING));
    validateProperties(loader.getEdgeCache().get("e"));
  }

  @Test
  public void readEdgeWithVariablesTest() {
    GDLLoader loader = getLoaderFromGDLString("()-[e]->()");
    Edge e = loader.getEdges().iterator().next();
    assertEquals("edge has wrong variable", "e", e.getVariable());
  }

  @Test
  public void readEdgeWithNoRangeExpression() {
    GDLLoader loader = getLoaderFromGDLString("()-[e]->()");
    Edge e = loader.getEdgeCache().get("e");

    assertFalse("edge should not have variable length", e.hasVariableLength());
    assertEquals("wrong lower bound", 1, e.getLowerBound());
    assertEquals("wrong upper bound", 1, e.getUpperBound());
  }

  @Test
  public void readEdgeWithLowerBoundTest() {
    GDLLoader loader = getLoaderFromGDLString("()-[e*2]->()");
    Edge e = loader.getEdgeCache().get("e");

    assertTrue("edge should have variable length", e.hasVariableLength());
    assertEquals("wrong lower bound", 2, e.getLowerBound());
    assertEquals("wrong lower bound", 0, e.getUpperBound());
  }

  @Test
  public void readEdgeWithUpperBoundTest() {
    GDLLoader loader = getLoaderFromGDLString("()-[e*..5]->()");
    Edge e = loader.getEdgeCache().get("e");

    assertTrue("edge should have variable length", e.hasVariableLength());
    assertEquals("wrong lower bound", 0, e.getLowerBound());
    assertEquals("wrong lower bound", 5, e.getUpperBound());
  }

  @Test
  public void readEdgeWithLowerAndUpperBoundTest() {
    GDLLoader loader = getLoaderFromGDLString("()-[e*3..5]->()");
    Edge e = loader.getEdgeCache().get("e");

    assertTrue("edge should have variable length", e.hasVariableLength());
    assertEquals("wrong lower bound", 3, e.getLowerBound());
    assertEquals("wrong lower bound", 5, e.getUpperBound());
  }

  @Test
  public void readEdgeWithUnboundLengthTest() {
    GDLLoader loader = getLoaderFromGDLString("()-[e*]->()");
    Edge e = loader.getEdgeCache().get("e");

    assertTrue("edge should have variable length", e.hasVariableLength());
    assertEquals("wrong lower bound", 0, e.getLowerBound());
    assertEquals("wrong lower bound", 0, e.getUpperBound());
  }

  // --------------------------------------------------------------------------------------------
  //  Graph only tests
  // --------------------------------------------------------------------------------------------

  @Test
  public void readEmptyGraphTest() {
    GDLLoader loader = getLoaderFromGDLString("[]");
    validateCollectionSizes(loader, 1, 0, 0);
    validateCacheSizes(loader,
      0, 0, 0,
      1, 0, 0);

    Optional<Graph> graph = loader.getGraphs().stream().findFirst();
    assertTrue(graph.isPresent());
    assertEquals(1, graph.get().getLabels().size());
    assertEquals(loader.getDefaultGraphLabel(), graph.get().getLabels().get(0));
  }

  @Test
  public void readSimpleGraphTest() {
    GDLLoader loader = getLoaderFromGDLString("[()]");
    validateCollectionSizes(loader, 1, 1, 0);
    validateCacheSizes(loader,
      0, 0, 0,
      1, 1, 0);
  }

  @Test
  public void readGraphWithVariableTest() {
    GDLLoader loader = getLoaderFromGDLString("g[()]");
    validateCollectionSizes(loader, 1, 1, 0);
    validateCacheSizes(loader,
      1, 0, 0,
      0, 1, 0);
    assertTrue("graph not cached", loader.getGraphCache().containsKey("g"));
    Graph g = loader.getGraphCache().get("g");
    assertNotNull("graph was null", g);
    assertEquals(loader.getDefaultGraphLabel(), g.getLabel());
  }

  @Test
  public void readGraphWithLabelTest() {
    GDLLoader loader = getLoaderFromGDLString("g:Label[()]");
    validateCollectionSizes(loader, 1, 1, 0);
    validateCacheSizes(loader,
      1, 0, 0,
      0, 1, 0);
    Graph g = loader.getGraphCache().get("g");
    assertEquals("graph has the wrong number of labels", 1, g.getLabels().size());
    assertEquals("graph has wrong label", "Label", g.getLabel());
  }

  @Test
  public void readGraphWithCamelCaseLabelTest() {
    GDLLoader loader = getLoaderFromGDLString("g:LabelParty[()]");
    Graph g = loader.getGraphCache().get("g");
    assertEquals("graph has wrong label", "LabelParty", g.getLabel());
  }

  @Test
  public void readGraphWithMultipleLabelsTest() {
    GDLLoader loader = getLoaderFromGDLString("g:Label1:Label2[()]");
    Graph g = loader.getGraphCache().get("g");
    assertEquals(
      "graph has wrong label",
      Arrays.asList("Label1", "Label2"),
      g.getLabels()
    );
  }

  @Test
  public void readGraphWithPropertiesTest() {
    GDLLoader loader = getLoaderFromGDLString(String.format("g%s[()]", PROPERTIES_STRING));
    validateCollectionSizes(loader, 1, 1, 0);
    validateCacheSizes(loader,
      1, 0, 0,
      0, 1, 0);
    validateProperties(loader.getGraphCache().get("g"));
  }

  @Test
  public void readGraphWithPropertiesOnly() {
    GDLLoader loader = getLoaderFromGDLString(String.format("%s[()]", PROPERTIES_STRING));
    validateCollectionSizes(loader, 1, 1, 0);
    validateCacheSizes(loader,
      0, 0, 0,
      1, 1, 0);
    validateProperties(loader.getGraphs().iterator().next());
  }

  @Test
  public void readGraphWithVariablesTest() {
    GDLLoader loader = getLoaderFromGDLString("g[()]");

    Graph g = loader.getGraphs().iterator().next();
    assertEquals("edge has wrong variable", "g", g.getVariable());
  }

  @Test
  public void readFragmentedGraphTest() {
    GDLLoader loader = getLoaderFromGDLString("g[()],g[()]");
    validateCollectionSizes(loader, 1, 2, 0);
    validateCacheSizes(loader, 1, 0, 0,
      0, 2, 0);
  }

  // --------------------------------------------------------------------------------------------
  //  Path cases
  // --------------------------------------------------------------------------------------------

  @Test
  public void pathTest() {
    GDLLoader loader = getLoaderFromGDLString("(v1)-[e1]->(v2)<-[e2]-(v3)");
    validateCollectionSizes(loader, 0, 3, 2);
    validateCacheSizes(loader,
      0, 3, 2,
      0, 0, 0);
    Vertex v1 = loader.getVertexCache().get("v1");
    Vertex v2 = loader.getVertexCache().get("v2");
    Vertex v3 = loader.getVertexCache().get("v3");
    Edge e1 = loader.getEdgeCache().get("e1");
    Edge e2 = loader.getEdgeCache().get("e2");

    assertEquals("edge e1 has wrong source vertex identifier",
      (Long) v1.getId(), e1.getSourceVertexId());
    assertEquals("edge e1 has wrong target vertex identifier",
      (Long) v2.getId(), e1.getTargetVertexId());
    assertEquals("edge e2 has wrong source vertex identifier",
      (Long) v3.getId(), e2.getSourceVertexId());
    assertEquals("edge e2 has wrong target vertex identifier",
      (Long) v2.getId(), e2.getTargetVertexId());
  }

  // --------------------------------------------------------------------------------------------
  //  MATCH ... WHERE ... tests
  // --------------------------------------------------------------------------------------------

  @Test
  public void testNonPredicateMatch() throws Exception {
    String query = "MATCH (n)-[e]->(m)";

    GDLLoader loader = getLoaderFromGDLString(query);
    validateCollectionSizes(loader, 0, 2, 1);

    assertFalse(loader.getPredicates().isPresent());
  }

  @Test
  public void testSimpleWhereClause() {
    String query = "MATCH (alice)-[r]->(bob)" +
      "WHERE alice.age > 50";

    GDLLoader loader = getLoaderFromGDLString(query);
    validateCollectionSizes(loader, 0, 2, 1);

    assertEquals("alice.age > 50", loader.getPredicates().get().toString());
  }

  @Test
  public void testNotClause() {
    String query = "MATCH (alice)-[r]->(bob)" +
      "WHERE NOT alice.age > 50";

    GDLLoader loader = getLoaderFromGDLString(query);
    validateCollectionSizes(loader, 0, 2, 1);

    assertEquals("(NOT alice.age > 50)", loader.getPredicates().get().toString());
  }

  @Test
  public void testComplexWhereClause() {
    String query = "MATCH (alice)-[r]->(bob)" +
      "WHERE (alice.age > bob.age OR (alice.age < 30 AND bob.name = \"Bob\")) AND alice.id != bob.id";

    GDLLoader loader = getLoaderFromGDLString(query);
    validateCollectionSizes(loader, 0, 2, 1);

    assertEquals("((alice.age > bob.age OR (alice.age < 30 AND bob.name = Bob)) AND alice.id != bob.id)",
      loader.getPredicates().get().toString());
  }

  @Test
  public void testEmbeddedWhereClause() {
    String query = "MATCH (alice {age : 50})-[r:knows]->(bob:User)";

    GDLLoader loader = getLoaderFromGDLString(query);
    validateCollectionSizes(loader, 0, 2, 1);

    assertEquals("((alice.age = 50 AND bob.__label__ = User) AND r.__label__ = knows)",
      loader.getPredicates().get().toString());
  }

  @Test
  public void testEmbeddedAndExplicitWhereClause() {
    GDLLoader loader = getLoaderFromGDLString(
      "MATCH (p:Person)-[e1:likes {love: TRUE}]->(other:Person) " +
        "WHERE p.age >= other.age");

    validateCollectionSizes(loader, 0, 2, 1);

    Vertex p = loader.getVertexCache().get("p");

    assertEquals("((((p.age >= other.age" +
        " AND p.__label__ = Person)" +
        " AND other.__label__ = Person)" +
        " AND e1.__label__ = likes)" +
        " AND e1.love = true)",
      loader.getPredicates().get().toString());

    assertEquals("vertex p has wrong label","Person", p.getLabel());
  }

  @Test(expected=InvalidReferenceException.class)
  public void testThrowExceptionOnInvalidVariableReference() {
    getLoaderFromGDLString("MATCH (a) where b.age = 42");
  }

  // --------------------------------------------------------------------------------------------
  //  Combined tests
  // --------------------------------------------------------------------------------------------

  @Test
  public void testGraphWithContentTest() {
    GDLLoader loader = getLoaderFromGDLString("g[(alice)-[r]->(bob),(alice)-[s]->(eve)]");
    validateCollectionSizes(loader, 1, 3, 2);
    validateCacheSizes(loader,
      1, 3, 2,
      0, 0 ,0);
    Graph g = loader.getGraphCache().get("g");
    List<GraphElement> graphElements = Arrays.asList(
      loader.getVertexCache().get("alice"),
      loader.getVertexCache().get("bob"),
      loader.getVertexCache().get("eve"),
      loader.getEdgeCache().get("r"),
      loader.getEdgeCache().get("s")
    );
    for (GraphElement graphElement : graphElements) {
      assertEquals("element has wrong graphs size", 1, graphElement.getGraphs().size());
      assertTrue("element was not in graph", graphElement.getGraphs().contains(g.getId()));
    }
  }

  @Test
  public void testGraphsWithOverlappingContent() {
    GDLLoader loader = getLoaderFromGDLString("g1[(alice)-[r]->(bob)],g2[(alice)-[s]->(bob)]");
    validateCollectionSizes(loader, 2, 2, 2);
    validateCacheSizes(loader,
      2, 2, 2,
      0, 0 , 0);
    Graph g1 = loader.getGraphCache().get("g1");
    Graph g2 = loader.getGraphCache().get("g2");

    List<Vertex> overlapElements = Arrays.asList(
      loader.getVertexCache().get("alice"),
      loader.getVertexCache().get("bob")
    );

    for (Vertex vertex : overlapElements) {
      assertEquals("vertex has wrong graph size", 2, vertex.getGraphs().size());
      assertTrue("vertex was not in graph g1", vertex.getGraphs().contains(g1.getId()));
      assertTrue("vertex was not in graph g2", vertex.getGraphs().contains(g2.getId()));
    }

    assertEquals("edge r has wrong graph size",
      1, loader.getEdgeCache().get("r").getGraphs().size());
    assertTrue("edge r was not in graph g1",
      loader.getEdgeCache().get("r").getGraphs().contains(g1.getId()));
    assertEquals("edge s has wrong graph size",
      1, loader.getEdgeCache().get("s").getGraphs().size());
    assertTrue("edge s was not in graph g2",
      loader.getEdgeCache().get("s").getGraphs().contains(g2.getId()));
  }

  @Test
  public void testFragmentedGraphWithVariables() {
    GDLLoader loader = getLoaderFromGDLString(
      "g[(a)-->(b)],g[(a)-[e]->(b)],g[(a)-[f]->(b)],h[(a)-[f]->(b)]");
    validateCollectionSizes(loader, 2, 2, 3);
    validateCacheSizes(loader,
      2, 2, 2,
      0, 0, 1);

    Graph g = loader.getGraphCache().get("g");
    Graph h = loader.getGraphCache().get("h");
    Vertex a = loader.getVertexCache().get("a");
    Vertex b = loader.getVertexCache().get("b");
    Edge e = loader.getEdgeCache().get("e");
    Edge f = loader.getEdgeCache().get("f");

    assertEquals("vertex a has wrong graph size", 2, a.getGraphs().size());
    assertEquals("vertex b has wrong graph size", 2, b.getGraphs().size());
    assertTrue("vertex a was not in g", a.getGraphs().contains(g.getId()));
    assertTrue("vertex a was not in h", a.getGraphs().contains(h.getId()));
    assertTrue("vertex b was not in g", b.getGraphs().contains(g.getId()));
    assertTrue("vertex b was not in h", b.getGraphs().contains(h.getId()));
    assertEquals("edge e has wrong graph size", 1, e.getGraphs().size());
    assertEquals("edge f has wrong graph size", 2, f.getGraphs().size());
    assertTrue("edge e was not in g", e.getGraphs().contains(g.getId()));
    assertTrue("edge f was not in g", f.getGraphs().contains(g.getId()));
    assertTrue("edge f was not in h", f.getGraphs().contains(h.getId()));
  }

  // --------------------------------------------------------------------------------------------
  //  Special cases
  // --------------------------------------------------------------------------------------------

  @Test
  public void readNullValueTest() {
    GDLLoader loader = getLoaderFromGDLString("(v{name:NULL})");
    validateCollectionSizes(loader, 0, 1, 0);
    validateCacheSizes(loader,
      0, 1, 0,
      0, 0, 0);
    Vertex a = loader.getVertexCache().get("v");
    assertTrue("missing property at vertex", a.getProperties().containsKey("name"));
    assertNull("property value was not null", a.getProperties().get("name"));
  }

  @Test
  public void readEmptyGDLTest() {
    GDLLoader loader = getLoaderFromGDLString("");
    validateCollectionSizes(loader, 0, 0, 0);
    validateCacheSizes(loader,
      0, 0, 0,
      0, 0, 0);
  }

  @Test
  public void loopTest() {
    GDLLoader loader = getLoaderFromGDLString("(v)-[e]->(v)");
    validateCollectionSizes(loader, 0, 1, 1);
    validateCacheSizes(loader,
      0, 1, 1,
      0, 0, 0);
    Vertex v = loader.getVertexCache().get("v");
    Edge e = loader.getEdgeCache().get("e");
    assertEquals("wrong source vertex identifier", (Long) v.getId(), e.getSourceVertexId());
    assertEquals("wrong target vertex identifier", (Long) v.getId(), e.getTargetVertexId());
  }

  @Test
  public void cycleTest() {
    GDLLoader loader = getLoaderFromGDLString("(v1)-[e1]->(v2)<-[e2]-(v1)");
    validateCollectionSizes(loader, 0, 2, 2);
    validateCacheSizes(loader,
      0, 2, 2,
      0, 0, 0);
    Vertex v1 = loader.getVertexCache().get("v1");
    Vertex v2 = loader.getVertexCache().get("v2");
    Edge e1 = loader.getEdgeCache().get("e1");
    Edge e2 = loader.getEdgeCache().get("e2");

    assertEquals("edge e1 has wrong source vertex identifier",
      (Long) v1.getId(), e1.getSourceVertexId());
    assertEquals("edge e1 has wrong target vertex identifier",
      (Long) v2.getId(), e1.getTargetVertexId());
    assertEquals("edge e2 has wrong source vertex identifier",
      (Long) v1.getId(), e2.getSourceVertexId());
    assertEquals("edge e2 has wrong target vertex identifier",
      (Long) v2.getId(), e2.getTargetVertexId());
  }

  // --------------------------------------------------------------------------------------------
  //  Test helpers
  // --------------------------------------------------------------------------------------------

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

  private GDLLoader getLoaderFromFile(String fileName) throws IOException {
    InputStream inputStream = getClass().getResourceAsStream(fileName);

    GDLLexer lexer = new GDLLexer(new ANTLRInputStream(inputStream));
    GDLParser parser = new GDLParser(new CommonTokenStream(lexer));

    ParseTreeWalker walker = new ParseTreeWalker();
    GDLLoader loader = new GDLLoader(DEFAULT_GRAPH_LABEL, DEFAULT_VERTEX_LABEL, DEFAULT_EDGE_LABEL);
    walker.walk(loader, parser.database());
    return loader;
  }

  // string representation of all valid properties
  private static String PROPERTIES_STRING;

  // contains all valid properties
  private static final List<PropertyTriple<?>> PROPERTIES_LIST = new ArrayList<>();

  /**
   * Represents a property for testing.
   *
   * @param <T>
   */
  private static class PropertyTriple<T> {
    private String key;
    private String value;
    private T expected;

    public PropertyTriple(String key, String value, T expected) {
      this.key = key;
      this.value = value;
      this.expected = expected;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    public T getExpected() {
      return expected;
    }

    @Override
    public String toString() {
      return String.format("%s:%s", getKey(), getValue());
    }
  }

  static {
    PROPERTIES_LIST.add(new PropertyTriple<>("_", "true", true));
    PROPERTIES_LIST.add(new PropertyTriple<>("_k1", "true", true));
    PROPERTIES_LIST.add(new PropertyTriple<>("__k1", "true", true));
    PROPERTIES_LIST.add(new PropertyTriple<>("_k_1", "true", true));
    PROPERTIES_LIST.add(new PropertyTriple<>("k1", "\"value\"", "value"));
    PROPERTIES_LIST.add(new PropertyTriple<>("k2", "true", true));
    PROPERTIES_LIST.add(new PropertyTriple<>("k3", "false", false));
    PROPERTIES_LIST.add(new PropertyTriple<>("k4", "TRUE", true));
    PROPERTIES_LIST.add(new PropertyTriple<>("k5", "FALSE", false));
    PROPERTIES_LIST.add(new PropertyTriple<>("k6", "0", 0));
    PROPERTIES_LIST.add(new PropertyTriple<>("k7", "0L", 0L));
    PROPERTIES_LIST.add(new PropertyTriple<>("k8", "0l", 0L));
    PROPERTIES_LIST.add(new PropertyTriple<>("k9", "42", 42));
    PROPERTIES_LIST.add(new PropertyTriple<>("k10", "42L", 42L));
    PROPERTIES_LIST.add(new PropertyTriple<>("k11", "42l", 42L));
    PROPERTIES_LIST.add(new PropertyTriple<>("k12", "-42", -42));
    PROPERTIES_LIST.add(new PropertyTriple<>("k13", "-42L", -42L));
    PROPERTIES_LIST.add(new PropertyTriple<>("k14", "-42l", -42L));
    PROPERTIES_LIST.add(new PropertyTriple<>("k15", "0.0", 0.0f));
    PROPERTIES_LIST.add(new PropertyTriple<>("k16", "0.0f", 0.0f));
    PROPERTIES_LIST.add(new PropertyTriple<>("k17", "0.0F", 0.0f));
    PROPERTIES_LIST.add(new PropertyTriple<>("k18", "0.0d", 0.0d));
    PROPERTIES_LIST.add(new PropertyTriple<>("k19", "0.0D", 0.0D));
    PROPERTIES_LIST.add(new PropertyTriple<>("k20", "-0.0", -0.0f));
    PROPERTIES_LIST.add(new PropertyTriple<>("k21", "-0.0f", -0.0f));
    PROPERTIES_LIST.add(new PropertyTriple<>("k22", "-0.0F", -0.0F));
    PROPERTIES_LIST.add(new PropertyTriple<>("k23", "-0.0d", -0.0d));
    PROPERTIES_LIST.add(new PropertyTriple<>("k24", "-0.0D", -0.0D));
    PROPERTIES_LIST.add(new PropertyTriple<>("k25", ".0", .0f));
    PROPERTIES_LIST.add(new PropertyTriple<>("k26", ".0f", .0f));
    PROPERTIES_LIST.add(new PropertyTriple<>("k27", ".0F", .0F));
    PROPERTIES_LIST.add(new PropertyTriple<>("k28", ".0d", .0d));
    PROPERTIES_LIST.add(new PropertyTriple<>("k29", ".0D", .0D));
    PROPERTIES_LIST.add(new PropertyTriple<>("k30", "3.14", 3.14f));
    PROPERTIES_LIST.add(new PropertyTriple<>("k31", "3.14f", 3.14f));
    PROPERTIES_LIST.add(new PropertyTriple<>("k32", "3.14F", 3.14F));
    PROPERTIES_LIST.add(new PropertyTriple<>("k33", "3.14d", 3.14d));
    PROPERTIES_LIST.add(new PropertyTriple<>("k34", "3.14D", 3.14D));
    PROPERTIES_LIST.add(new PropertyTriple<>("k35", "-3.14", -3.14f));
    PROPERTIES_LIST.add(new PropertyTriple<>("k36", "-3.14f", -3.14f));
    PROPERTIES_LIST.add(new PropertyTriple<>("k37", "-3.14F", -3.14F));
    PROPERTIES_LIST.add(new PropertyTriple<>("k38", "-3.14d", -3.14d));
    PROPERTIES_LIST.add(new PropertyTriple<>("k39", "-3.14D", -3.14D));

    Iterator<PropertyTriple<?>> iterator = PROPERTIES_LIST.iterator();
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    while (iterator.hasNext()) {
      sb.append(iterator.next().toString());
      if (iterator.hasNext()) {
        sb.append(",");
      }
    }
    sb.append("}");

    PROPERTIES_STRING = sb.toString();
  }

  private void validateProperties(Element element) {
    assertEquals("wrong number of properties", PROPERTIES_LIST.size(), element.getProperties().size());

    for (PropertyTriple<?> expectedProperty : PROPERTIES_LIST) {
      assertEquals("wrong property for key: " + expectedProperty.getKey(),
        expectedProperty.getExpected(), element.getProperties().get(expectedProperty.getKey()));
    }
  }

  private void validateCollectionSizes(GDLLoader loader,
    int expectedGraphCount,
    int expectedVertexCount,
    int expectedEdgeCount) {
    assertEquals("wrong number of graphs", expectedGraphCount, loader.getGraphs().size());
    assertEquals("wrong number of vertices", expectedVertexCount, loader.getVertices().size());
    assertEquals("wrong number of edges", expectedEdgeCount, loader.getEdges().size());
  }

  private void validateCacheSizes(GDLLoader loader,
    int expectedUserGraphCacheSize, int expectedUserVertexCacheSize, int expectedUserEdgeCacheSize,
    int expectedAutoGraphCacheSize, int expectedAutoVertexCacheSize, int expectedAutoEdgeCacheSize) {

    // default (user-defined)
    assertEquals("wrong number of cached user-defined graphs",
      expectedUserGraphCacheSize, loader.getGraphCache().size());
    assertEquals("wrong number of cached user-defined vertices",
      expectedUserVertexCacheSize, loader.getVertexCache().size());
    assertEquals("wrong number of cached user-defined edges",
      expectedUserEdgeCacheSize, loader.getEdgeCache().size());

    // user-defined
    assertEquals("wrong number of cached user-defined graphs",
      expectedUserGraphCacheSize, loader.getGraphCache(true, false).size());
    assertEquals("wrong number of cached user-defined vertices",
      expectedUserVertexCacheSize, loader.getVertexCache(true, false).size());
    assertEquals("wrong number of cached user-defined edges",
      expectedUserEdgeCacheSize, loader.getEdgeCache(true, false).size());

    // auto-generated
    assertEquals("wrong number of cached auto-defined graphs",
      expectedAutoGraphCacheSize, loader.getGraphCache(false, true).size());
    assertEquals("wrong number of cached auto-defined vertices",
      expectedAutoVertexCacheSize, loader.getVertexCache(false, true).size());
    assertEquals("wrong number of cached auto-defined edges",
      expectedAutoEdgeCacheSize, loader.getEdgeCache(false, true).size());

    // all
    assertEquals("wrong number of cached auto-defined graphs",
      expectedUserGraphCacheSize + expectedAutoGraphCacheSize,
      loader.getGraphCache(true, true).size());
    assertEquals("wrong number of cached auto-defined vertices",
      expectedUserVertexCacheSize + expectedAutoVertexCacheSize,
      loader.getVertexCache(true, true).size());
    assertEquals("wrong number of cached auto-defined edges",
      expectedUserEdgeCacheSize + expectedAutoEdgeCacheSize,
      loader.getEdgeCache(true, true).size());
  }
}
