package org.gradoop.grala;

public class GDLLoader extends GDLBaseListener {

  private int graphId = 0;
  private int pathCount = 0;

  @Override
  public void exitGraph(GDLParser.GraphContext graph) {
    System.out.println(String.format("graph [%d] %s {%s}", graphId++,
      getHeaderAsString(graph.header()),
      getPropertiesAsString(graph.properties())));

    for (GDLParser.PathContext path : graph.path()) {
      printPath(path, pathCount++);
    }
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
