package org.s1ck.gdl;

import org.junit.Test;
import org.s1ck.gdl.model.Edge;
import org.s1ck.gdl.model.Graph;
import org.s1ck.gdl.model.Vertex;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

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

  @Test
  public void customIdSupplierTest() {
    AtomicLong nextGraphId = new AtomicLong(42);
    AtomicLong nextVertexId = new AtomicLong(42);
    AtomicLong nextEdgeId = new AtomicLong(42);

    GDLHandler handler = new GDLHandler.Builder()
            .setNextGraphId((ignored) -> nextGraphId.getAndAdd(42))
            .setNextVertexId((ignored) -> nextVertexId.getAndAdd(42))
            .setNextEdgeId((ignored) -> nextEdgeId.getAndAdd(42))
            .buildFromString("g1[(v1)-[e1]->(v2)], g2[(v3)-[e2]->(v4)]");

    assertEquals("wrong number of graphs", 2, handler.getGraphs().size());
    assertEquals("wrong number of vertices", 4, handler.getVertices().size());
    assertEquals("wrong number of vertices", 2, handler.getEdges().size());

    assertEquals("wrong id for g1", 42L, handler.getGraphCache().get("g1").getId());
    assertEquals("wrong id for g2", 84L, handler.getGraphCache().get("g2").getId());
    assertEquals("wrong id for v1", 42L, handler.getVertexCache().get("v1").getId());
    assertEquals("wrong id for v2", 84L, handler.getVertexCache().get("v2").getId());
    assertEquals("wrong id for v3", 126L, handler.getVertexCache().get("v3").getId());
    assertEquals("wrong id for v4", 168L, handler.getVertexCache().get("v4").getId());
    assertEquals("wrong id for e1", 42L, handler.getEdgeCache().get("e1").getId());
    assertEquals("wrong id for e1", 84L, handler.getEdgeCache().get("e2").getId());
  }

  @Test
  public void customVariableBasedIdSupplierTest() {
    AtomicLong nextGraphId = new AtomicLong(42);
    AtomicLong nextVertexId = new AtomicLong(42);
    AtomicLong nextEdgeId = new AtomicLong(42);

    GDLHandler handler = new GDLHandler.Builder()
            .setNextGraphId((ignore) -> nextGraphId.getAndAdd(42))
            .setNextVertexId((variable) -> variable.map(v -> {
              long vertexId = 0;
              switch (v) {
                case "v1":
                  vertexId = 1337L;
                  break;
                case "v2":
                  vertexId = 1338L;
                  break;
                case "v3":
                  vertexId = 1339L;
                  break;
              }
              return vertexId;
            }).orElseGet(() -> nextVertexId.getAndAdd(1)))
            .setNextEdgeId((ignore) -> nextEdgeId.getAndAdd(42))
            .buildFromString("g1[(v1)-[e1]->(v2)], g2[(v3)-[e2]->(v4), ()]");

    assertEquals("wrong number of graphs", 2, handler.getGraphs().size());
    assertEquals("wrong number of vertices", 5, handler.getVertices().size());
    assertEquals("wrong number of vertices", 2, handler.getEdges().size());

    assertEquals("wrong id for g1", 42L, handler.getGraphCache().get("g1").getId());
    assertEquals("wrong id for g2", 84L, handler.getGraphCache().get("g2").getId());
    assertEquals("wrong id for v1", 1337L, handler.getVertexCache().get("v1").getId());
    assertEquals("wrong id for v2", 1338L, handler.getVertexCache().get("v2").getId());
    assertEquals("wrong id for v3", 1339L, handler.getVertexCache().get("v3").getId());
    assertEquals("wrong id for v4", 0L, handler.getVertexCache().get("v4").getId());
    assertEquals("wrong id for e1", 42L, handler.getEdgeCache().get("e1").getId());
    assertEquals("wrong id for e1", 84L, handler.getEdgeCache().get("e2").getId());
    // the remaining anonymous node must have id 42
    assertEquals("wrong id for anonymous node", 1, handler.getVertices().stream().filter(v -> v.getId() == 42L).count());
  }
}
