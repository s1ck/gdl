package org.s1ck.gdl;

import com.google.common.collect.Lists;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;
import org.s1ck.gdl.model.Edge;
import org.s1ck.gdl.model.Element;
import org.s1ck.gdl.model.Graph;
import org.s1ck.gdl.model.GraphElement;
import org.s1ck.gdl.model.Vertex;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

public class GDLLoaderTest {

  // --------------------------------------------------------------------------------------------
  //  Vertex only tests
  // --------------------------------------------------------------------------------------------

  @Test
  public void readVertexTest() {
    GDLLoader loader = getLoaderFromGDLString("()");

    validateCollectionSizes(loader, 0, 1, 0);
    validateCacheSizes(loader, 0, 0, 0);
  }

  @Test
  public void readVertexWithVariableTest() {
    GDLLoader loader = getLoaderFromGDLString("(var)");

    validateCollectionSizes(loader, 0, 1, 0);
    validateCacheSizes(loader, 0, 1, 0);
    assertTrue("vertex not cached", loader.getVertexCache().containsKey("var"));
    assertNotNull("vertex was null", loader.getVertexCache().get("var"));
  }

  @Test
  public void readVertexWithLabelTest() {
    GDLLoader loader = getLoaderFromGDLString("(var:Label)");
    Vertex v = loader.getVertexCache().get("var");
    assertEquals("vertex has wrong label", "Label", v.getLabel());
  }

  @Test
  public void readVertexWithCamelCaseLabelTest() {
    GDLLoader loader = getLoaderFromGDLString("(var:BlogPost)");
    Vertex v = loader.getVertexCache().get("var");
    assertEquals("vertex has wrong label", "BlogPost", v.getLabel());
  }

  @Test
  public void readVertexWithPropertiesTest() {
    GDLLoader loader = getLoaderFromGDLString(String.format("(var %s)", PROPERTIES));

    validateProperties(loader.getVertexCache().get("var"));
  }

  // --------------------------------------------------------------------------------------------
  //  Edge only tests
  // --------------------------------------------------------------------------------------------

  @Test
  public void readEdgeTest() {
    GDLLoader loader = getLoaderFromGDLString("()-->()");
    validateCollectionSizes(loader, 0, 2, 1);
    validateCacheSizes(loader, 0, 0, 0);
  }

  @Test
  public void readOutgoingEdgeWithVariablesTest() {
    GDLLoader loader = getLoaderFromGDLString("(v1)-[e1]->(v2)");
    validateCollectionSizes(loader, 0, 2, 1);
    validateCacheSizes(loader, 0, 2, 1);

    assertTrue("edge not cached", loader.getEdgeCache().containsKey("e1"));
    Edge e1 = loader.getEdgeCache().get("e1");
    assertNotNull("e was null", e1);
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
    Vertex v1 = loader.getVertexCache().get("v1");
    Vertex v2 = loader.getVertexCache().get("v2");

    assertEquals("wrong source vertex identifier", (Long) v1.getId(), e1.getTargetVertexId());
    assertEquals("wrong target vertex identifier", (Long) v2.getId(), e1.getSourceVertexId());
  }

  @Test
  public void readEdgeWithLabelTest() {
    GDLLoader loader = getLoaderFromGDLString("()-[e:knows]->()");

    Edge e = loader.getEdgeCache().get("e");
    assertEquals("edge has wrong label", "knows", e.getLabel());
  }

  @Test
  public void readEdgeWithCamelCaseLabelTest() {
    GDLLoader loader = getLoaderFromGDLString("()-[e:hasInterest]->()");

    Edge e = loader.getEdgeCache().get("e");
    assertEquals("edge has wrong label", "hasInterest", e.getLabel());
  }

  @Test
  public void readEdgeWithPropertiesTest() {
    GDLLoader loader = getLoaderFromGDLString(String.format("()-[e %s]->()", PROPERTIES));

    validateProperties(loader.getEdgeCache().get("e"));
  }

  // --------------------------------------------------------------------------------------------
  //  Graph only tests
  // --------------------------------------------------------------------------------------------

  @Test
  public void readGraphTest() {
    GDLLoader loader = getLoaderFromGDLString("[()]");
    validateCollectionSizes(loader, 1, 1, 0);
    validateCacheSizes(loader, 0, 0, 0);
  }

  @Test
  public void readGraphWithVariableTest() {
    GDLLoader loader = getLoaderFromGDLString("g[()]");
    validateCollectionSizes(loader, 1, 1, 0);
    validateCacheSizes(loader, 1, 0, 0);
    assertTrue("graph not cached", loader.getGraphCache().containsKey("g"));
    assertNotNull("graph was null", loader.getGraphCache().get("g"));
  }

  @Test
  public void readGraphWithLabelTest() {
    GDLLoader loader = getLoaderFromGDLString("g:Label[()]");
    validateCollectionSizes(loader, 1, 1, 0);
    validateCacheSizes(loader, 1, 0, 0);
    Graph g = loader.getGraphCache().get("g");
    assertEquals("graph has wrong label", "Label", g.getLabel());
  }

  @Test
  public void readGraphWithCamelCaseLabelTest() {
    GDLLoader loader = getLoaderFromGDLString("g:LabelParty[()]");
    Graph g = loader.getGraphCache().get("g");
    assertEquals("graph has wrong label", "LabelParty", g.getLabel());
  }

  @Test
  public void readGraphWithPropertiesTest() {
    GDLLoader loader = getLoaderFromGDLString(String.format("g%s[()]", PROPERTIES));
    validateCollectionSizes(loader, 1, 1, 0);
    validateCacheSizes(loader, 1, 0, 0);
    validateProperties(loader.getGraphCache().get("g"));
  }

  @Test
  public void readGraphWithPropertiesOnly() {
    GDLLoader loader = getLoaderFromGDLString(String.format("%s[()]", PROPERTIES));
    validateCollectionSizes(loader, 1, 1, 0);
    validateCacheSizes(loader, 0, 0, 0);
    validateProperties(loader.getGraphs().iterator().next());
  }

  @Test
  public void readFragmentedGraphTest() {
    GDLLoader loader = getLoaderFromGDLString("g[()];g[()]");
    validateCollectionSizes(loader, 1, 2, 0);
    validateCacheSizes(loader, 1, 0, 0);
  }

  // --------------------------------------------------------------------------------------------
  //  Path cases
  // --------------------------------------------------------------------------------------------

  @Test
  public void pathTest() {
    GDLLoader loader = getLoaderFromGDLString("(v1)-[e1]->(v2)<-[e2]-(v3)");
    validateCollectionSizes(loader, 0, 3, 2);
    validateCacheSizes(loader, 0, 3, 2);
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
  //  Combined tests
  // --------------------------------------------------------------------------------------------

  @Test
  public void testGraphWithContentTest() {
    GDLLoader loader = getLoaderFromGDLString("g[(alice)-[r]->(bob);(alice)-[s]->(eve)]");
    validateCollectionSizes(loader, 1, 3, 2);
    validateCacheSizes(loader, 1, 3, 2);
    Graph g = loader.getGraphCache().get("g");
    List<GraphElement> graphElements = Lists.newArrayList(
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
    GDLLoader loader = getLoaderFromGDLString("g1[(alice)-[r]->(bob)];g2[(alice)-[s]->(bob)]");
    validateCollectionSizes(loader, 2, 2, 2);
    validateCacheSizes(loader, 2, 2, 2);
    Graph g1 = loader.getGraphCache().get("g1");
    Graph g2 = loader.getGraphCache().get("g2");

    List<Vertex> overlapElements = Lists.newArrayList(
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
      "g[(a)-->(b)];g[(a)-[e]->(b)];g[(a)-[f]->(b)];h[(a)-[f]->(b)]");
    validateCollectionSizes(loader, 2, 2, 3);
    validateCacheSizes(loader, 2, 2, 2);

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
  public void readEmptyGDLTest() {
    GDLLoader loader = getLoaderFromGDLString("");
    validateCollectionSizes(loader, 0, 0, 0);
    validateCacheSizes(loader, 0, 0, 0);
  }

  @Test
  public void loopTest() {
    GDLLoader loader = getLoaderFromGDLString("(v)-[e]->(v)");
    validateCollectionSizes(loader, 0, 1, 1);
    validateCacheSizes(loader, 0, 1, 1);
    Vertex v = loader.getVertexCache().get("v");
    Edge e = loader.getEdgeCache().get("e");
    assertEquals("wrong source vertex identifier", (Long) v.getId(), e.getSourceVertexId());
    assertEquals("wrong target vertex identifier", (Long) v.getId(), e.getTargetVertexId());
  }

  @Test
  public void cycleTest() {
    GDLLoader loader = getLoaderFromGDLString("(v1)-[e1]->(v2)<-[e2]-(v1)");
    validateCollectionSizes(loader, 0, 2, 2);
    validateCacheSizes(loader, 0, 2, 2);
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

  // contains all valid property types
  private static final String PROPERTIES =
    "{" +
      "key1 = \"value\"" +
      ",key2 =   12"      +
      ",key3 =   true"    +
      ",key4 =   -10"     +
      ",key5 =   0"       +
      ",key6 =   3.14"    +
      ",key7 =   .14"     +
      ",key8 =   -3.14"   +
      "}";

  private void validateProperties(Element element) {
    assertEquals("wrong number of properties", 8, element.getProperties().size());
    assertEquals("wrong value for key1", "value", element.getProperties().get("key1"));
    assertEquals("wrong value for key2", 12, element.getProperties().get("key2"));
    assertEquals("wrong value for key3", true, element.getProperties().get("key3"));
    assertEquals("wrong value for key4", -10, element.getProperties().get("key4"));
    assertEquals("wrong value for key5", 0, element.getProperties().get("key5"));
    assertEquals("wrong value for key6", 3.14f, element.getProperties().get("key6"));
    assertEquals("wrong value for key7", .14f, element.getProperties().get("key7"));
    assertEquals("wrong value for key8", -3.14f, element.getProperties().get("key8"));
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
    int expectedGraphCacheSize,
    int expectedVertexCacheSize,
    int expectedEdgeCacheSize) {
    assertEquals("wrong number of cached graphs",
      expectedGraphCacheSize, loader.getGraphCache().size());
    assertEquals("wrong number of cached vertices",
      expectedVertexCacheSize, loader.getVertexCache().size());
    assertEquals("wrong number of cached edges",
      expectedEdgeCacheSize, loader.getEdgeCache().size());
  }
}
