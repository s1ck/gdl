package org.s1ck.gdl;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;
import org.s1ck.gdl.model.Edge;
import org.s1ck.gdl.model.Element;
import org.s1ck.gdl.model.Graph;
import org.s1ck.gdl.model.Vertex;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class GDLLoaderTest {

  // contains all valid property types
  private static final String PROPERTIES =
    "{key1 = \"value\", key2 = 12, key3 = true, key4 = -10, key5 =0}";

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
    GDLLoader loader = getLoaderFromGDLString("()-[e:KNOWS]->()");

    Edge e = loader.getEdgeCache().get("e");
    assertEquals("edge has wrong label", "KNOWS", e.getLabel());
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

  private GDLLoader getLoaderFromGDLString(String gdlString) {
    GDLLexer lexer = new GDLLexer(new ANTLRInputStream(gdlString));
    GDLParser parser = new GDLParser(new CommonTokenStream(lexer));

    ParseTreeWalker walker = new ParseTreeWalker();
    GDLLoader loader = new GDLLoader();
    walker.walk(loader, parser.database());
    return loader;
  }

  private void validateProperties(Element element) {
    assertEquals("wrong number of properties", 5, element.getProperties().size());
    assertEquals("wrong value for key1", "value", element.getProperties().get("key1"));
    assertEquals("wrong value for key2", 12, element.getProperties().get("key2"));
    assertEquals("wrong value for key3", true, element.getProperties().get("key3"));
    assertEquals("wrong value for key4", -10, element.getProperties().get("key4"));
    assertEquals("wrong value for key5", 0, element.getProperties().get("key5"));
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

  @Test
  public void testGDLLoader() throws IOException {
    String file = "/social_network.gdl";
    InputStream inputStream = getClass().getResourceAsStream(file);

    GDLLexer lexer = new GDLLexer(new ANTLRInputStream(inputStream));
    GDLParser parser = new GDLParser(new CommonTokenStream(lexer));

    ParseTreeWalker walker = new ParseTreeWalker();
    GDLLoader loader = new GDLLoader();

    walker.walk(loader, parser.database());

    for (Graph g : loader.getGraphs()) {
      System.out.println(g);
    }

    for (Vertex vertex : loader.getVertices()) {
      System.out.println(vertex);
    }

    for (Edge edge : loader.getEdges()) {
      System.out.println(edge);
    }

    System.out.println(loader.getGraphCache());
    System.out.println(loader.getVertexCache());
    System.out.println(loader.getEdgeCache());
  }
}
