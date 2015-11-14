package org.gradoop.grala;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.Test;

public class ParserTest {
  private static final String DEFAULT_GRAPH_LABEL = "G_LABEL" ;
  private static final String DEFAULT_VERTEX_LABEL = "V_LABEL" ;
  private static final String DEFAULT_EDGE_LABEL = "E_LABEL" ;

  @Test
  public void testExampleField() throws Exception {
    GDLLexer l = new GDLLexer(
      new ANTLRInputStream(getClass().getResourceAsStream("/database.gdl")));
    GDLParser p = new GDLParser(new CommonTokenStream(l));
    p.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer,
        Object offendingSymbol, int line, int charPositionInLine, String msg,
        RecognitionException e) {
        throw new IllegalStateException(
          "failed to parse at line " + line + " due to " + msg, e);
      }
    });

//    p.graph();

    int graphCount = 0;
    for (GDLParser.GraphContext graph : p.database().graph()) {
      printGraph(graph, graphCount++);

      int pathCount = 0;
      for (GDLParser.PathContext path : graph.path()) {
        printPath(path, pathCount++);
      }
    }
  }

  private void printGraph(GDLParser.GraphContext graph, int graphId) {
    if(graphId > 0) System.out.println();

    System.out.println(String.format("graph [%d] %s {%s}", graphId,
      getHeaderAsString(graph.header()),
      getPropertiesAsString(graph.properties())));
  }

  private void printPath(GDLParser.PathContext path, int pathId) {
    System.out.println(String.format("path[%d].vertices:", pathId));
    int vertexCount = 0;
    for (GDLParser.VertexContext vertex : path.vertex()) {
      printVertex(vertex, vertexCount++);
    }
    System.out.println(String.format("path[%d].edges:", pathId));
    int edgeCount = 0;
    for (GDLParser.EdgeContext edgeContext : path.edge()) {

      if(edgeContext.incomingEdge() != null) {
        int source = edgeCount + 1;
        int target = edgeCount;
        printEdgeBody(edgeContext.incomingEdge().edgeBody(), edgeCount++, true, source, target);
      } else {
        int source = edgeCount;
        int target = edgeCount + 1;
        printEdgeBody(edgeContext.outgoingEdge().edgeBody(), edgeCount++, false, source, target);
      }
    }
  }

  private void printVertex(GDLParser.VertexContext vertex, int vertexId) {
    System.out.println(String.format("\tvertex [%d] %s {%s}", vertexId,
      getHeaderAsString(vertex.header()),
      getPropertiesAsString(vertex.properties())));
  }

  private void printEdgeBody(GDLParser.EdgeBodyContext edgeBody, int edgeId, boolean isIncoming, int source, int target) {
    System.out.println(String.format(
      "\tedge [%d,%s] source: %d target: %d header: %s properties: {%s}",
      edgeId,
      isIncoming ? "INCOMING" : "OUTGOING",
      source,
      target,
      edgeBody != null ? getHeaderAsString(edgeBody.header()): "empty",
      edgeBody != null ? getPropertiesAsString(edgeBody.properties()) : ""));
  }

  private String getHeaderAsString(GDLParser.HeaderContext header) {
    if (header != null) {
      return String.format("variable: %s label: %s", header.Variable(), header.Label());
    } else {
      return "";
    }
  }

  private String getPropertiesAsString(GDLParser.PropertiesContext properties) {
    if (properties != null) {
      StringBuilder sb = new StringBuilder();
      for (GDLParser.PropertyContext p : properties.property()) {
        sb.append(String.format("%s:%s,", p.Variable(), p.Value()));
      }
      return sb.toString();
    } else {
      return "";
    }
  }
}
