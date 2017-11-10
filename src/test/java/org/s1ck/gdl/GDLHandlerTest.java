package org.s1ck.gdl;

import org.junit.Test;
import org.s1ck.gdl.model.Edge;
import org.s1ck.gdl.model.Graph;
import org.s1ck.gdl.model.Vertex;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GDLHandlerTest {

  @Test
  public void initFromStringTest() {
    GDLHandler handler = new GDLHandler.Builder().buildFromString("[()-->()]");
    assertEquals("wrong number of graphs", 1, handler.getGraphs().size());
    assertEquals("wrong number of vertices", 2, handler.getVertices().size());
    assertEquals("wrong number of edges", 1, handler.getEdges().size());
  }

  @Test
  public void initFromStreamTest() throws IOException {
    InputStream inputStream = GDLHandler.class.getResourceAsStream("/single_graph.gdl");
    GDLHandler handler = new GDLHandler.Builder().buildFromStream(inputStream);
    assertEquals("wrong number of graphs", 1, handler.getGraphs().size());
    assertEquals("wrong number of vertices", 1, handler.getVertices().size());
  }

  @Test
  public void initFromFileTest() throws IOException {
    String fileName = GDLHandler.class.getResource("/single_graph.gdl").getFile();
    GDLHandler handler = new GDLHandler.Builder().buildFromFile(fileName);
    assertEquals("wrong number of graphs", 1, handler.getGraphs().size());
    assertEquals("wrong number of vertices", 1, handler.getVertices().size());
  }

  @Test
  public void initWithDefaultLabelsTest() {
    GDLHandler handler = new GDLHandler.Builder()
      .setDefaultGraphLabel("G")
      .setDefaultVertexLabel("V")
      .setDefaultEdgeLabel("E")
      .buildFromString("g[(v)-[e]->(v)]");

    Graph g = handler.getGraphCache().get("g");
    Vertex v = handler.getVertexCache().get("v");
    Edge e = handler.getEdgeCache().get("e");

    assertEquals("Graph has wrong label", "G", g.getLabel());
    assertEquals("Graph has wrong labels", Collections.singletonList("G"), g.getLabels());
    assertEquals("Vertex has wrong label", "V", v.getLabel());
    assertEquals("Vertex has wrong labels", Collections.singletonList("V"), v.getLabels());
    assertEquals("Edge has wrong label", "E", e.getLabel());
    assertEquals("Edge has wrong labels", Collections.singletonList("E"), e.getLabels());
  }

  @Test
  public void initWithDisabledDefaultLabelsTest() {
    GDLHandler handler = new GDLHandler.Builder()
      .disableDefaultGraphLabel()
      .disableDefaultVertexLabel()
      .disableDefaultEdgeLabel()
      .buildFromString("g[(u)-[e]->(v)]");

    Graph g = handler.getGraphCache().get("g");
    Vertex v = handler.getVertexCache().get("u");
    Edge e = handler.getEdgeCache().get("e");

    assertEquals("Graph has wrong label", null, g.getLabel());
    assertEquals("Graph has wrong labels", Collections.emptyList(), g.getLabels());
    assertEquals("Vertex has wrong label", null, v.getLabel());
    assertEquals("Vertex has wrong labels", Collections.emptyList(), v.getLabels());
    assertEquals("Edge has wrong label", null, e.getLabel());
    assertEquals("Edge has wrong labels", Collections.emptyList(), e.getLabels());
  }

  @Test
  public void appendTest() {
    GDLHandler handler = new GDLHandler.Builder().buildFromString("[()]");
    handler.append("[()]");

    assertEquals("wrong number of graphs", 2, handler.getGraphs().size());
    assertEquals("wrong number of vertices", 2, handler.getVertices().size());
  }

  @Test
  public void appendExistingVertexTest() {
    GDLHandler handler = new GDLHandler.Builder().buildFromString("g[(v)]");
    handler.append("h[(v)]");

    assertEquals("wrong number of graphs", 2, handler.getGraphs().size());
    assertEquals("wrong number of vertices", 1, handler.getVertices().size());

    Graph g = handler.getGraphCache().get("g");
    Graph h = handler.getGraphCache().get("h");
    Vertex v = handler.getVertexCache().get("v");

    assertEquals("Vertex has wrong number of graphs", 2, v.getGraphs().size());
    assertTrue("Vertex is in wrong graph", v.getGraphs().contains(g.getId()));
    assertTrue("Vertex is in wrong graph", v.getGraphs().contains(h.getId()));
  }

  @Test
  public void appendExistingGraphTest() {
    GDLHandler handler = new GDLHandler.Builder().buildFromString("g[(v)]");
    handler.append("g[(u)]");

    assertEquals("wrong number of graphs", 1, handler.getGraphs().size());
    assertEquals("wrong number of vertices", 2, handler.getVertices().size());

    Graph g = handler.getGraphCache().get("g");
    Vertex v = handler.getVertexCache().get("v");
    Vertex u = handler.getVertexCache().get("u");

    assertEquals("Vertex has wrong number of graphs", 1, v.getGraphs().size());
    assertEquals("Vertex has wrong number of graphs", 1, u.getGraphs().size());
    assertTrue("Vertex is in wrong graph", v.getGraphs().contains(g.getId()));
    assertTrue("Vertex is in wrong graph", u.getGraphs().contains(g.getId()));
  }
}
