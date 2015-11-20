package org.s1ck.gdl;

import org.junit.Test;
import org.s1ck.gdl.model.Edge;
import org.s1ck.gdl.model.Graph;
import org.s1ck.gdl.model.Vertex;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class GDLHandlerTest {

  @Test
  public void initFromStringTest() {
    GDLHandler handler = new GDLHandler.Builder().buildFromString("[()]");
    assertEquals("wrong number of graphs", 1, handler.getVertices().size());
    assertEquals("wrong number of vertices", 1, handler.getVertices().size());
  }

  @Test
  public void initFromStreamTest() throws IOException {
    InputStream inputStream = GDLHandler.class.getResourceAsStream("/single_graph.gdl");
    GDLHandler handler = new GDLHandler.Builder().buildFromStream(inputStream);
    assertEquals("wrong number of graphs", 1, handler.getVertices().size());
    assertEquals("wrong number of vertices", 1, handler.getVertices().size());
  }

  @Test
  public void initFromFileTest() throws IOException {
    String fileName = GDLHandler.class.getResource("/single_graph.gdl").getFile();
    GDLHandler handler = new GDLHandler.Builder().buildFromFile(fileName);
    assertEquals("wrong number of graphs", 1, handler.getVertices().size());
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
    assertEquals("Vertex has wrong label", "V", v.getLabel());
    assertEquals("Edge has wrong label", "E", e.getLabel());
  }
}
