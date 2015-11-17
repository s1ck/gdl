package org.gradoop.grala;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.gradoop.grala.model.Edge;
import org.gradoop.grala.model.Graph;
import org.gradoop.grala.model.GraphElement;
import org.gradoop.grala.model.Vertex;

import java.util.Map;
import java.util.Set;

public class GDLLoader extends GDLBaseListener {

  private static final String DEFAULT_GRAPH_LABEL = "__GRAPH";
  private static final String DEFAULT_VERTEX_LABEL = "__VERTEX";
  private static final String DEFAULT_EDGE_LABEL = "__EDGE";

  // used to cache elements which are used with variables
  private Map<String, Graph> graphCache;
  private Map<String, Vertex> vertexCache;
  private Map<String, Edge> edgeCache;

  // used to hold the final database elements
  private Set<Graph> graphs;
  private Set<Vertex> vertices;
  private Set<Edge> edges;

  // needs to be replaced by GradoopId factory
  private long currentGraphId = 0L;
  private long currentVertexId = 0L;
  private long currentEdgeId = 0L;

  // flag that tells if the parser is inside a logical graph
  private boolean inGraph = false;

  public GDLLoader() {
    graphCache = Maps.newHashMap();
    vertexCache = Maps.newHashMap();
    edgeCache = Maps.newHashMap();

    graphs = Sets.newHashSet();
    vertices = Sets.newHashSet();
    edges = Sets.newHashSet();
  }

  public Set<Graph> getGraphs() {
    return graphs;
  }

  public Set<Vertex> getVertices() {
    return vertices;
  }

  public Set<Edge> getEdges() {
    return edges;
  }

  public Map<String, Graph> getGraphCache() {
    return graphCache;
  }

  public Map<String, Vertex> getVertexCache() {
    return vertexCache;
  }

  public Map<String, Edge> getEdgeCache() {
    return edgeCache;
  }

  @Override
  public void enterGraph(GDLParser.GraphContext graphContext) {
    inGraph = true;
    String variable = getVariable(graphContext.header());
    if (variable != null && !graphCache.containsKey(variable)) {
      Graph g = initNewGraph(graphContext);
      graphs.add(g);
      graphCache.put(variable, g);
    } else {
      Graph g = initNewGraph(graphContext);
      graphs.add(g);
    }
  }

  @Override
  public void exitGraph(GDLParser.GraphContext ctx) {
    inGraph = false;
  }

  @Override
  public void enterVertex(GDLParser.VertexContext vertexContext) {
    String variable = getVariable(vertexContext.header());
    if (variable != null && vertexCache.containsKey(variable)) {
      updateGraphElement(vertexCache.get(variable));
    } else {
      Vertex v = initNewVertex(vertexContext);
      vertices.add(v);

      if (variable != null) {
        vertexCache.put(variable, v);
      }
    }
  }

  @Override
  public void enterIncomingEdge(GDLParser.IncomingEdgeContext incomingEdgeContext) {
    processEdge(incomingEdgeContext.edgeBody(), true);
  }

  @Override
  public void enterOutgoingEdge(GDLParser.OutgoingEdgeContext outgoingEdgeContext) {
    processEdge(outgoingEdgeContext.edgeBody(), false);
  }

  private void processEdge(GDLParser.EdgeBodyContext edgeBodyContext, boolean isIncoming) {
    String variable = null;

    if (edgeBodyContext != null) {
      variable = getVariable(edgeBodyContext.header());
    }
    if (variable != null && edgeCache.containsKey(variable)) {
      updateGraphElement(edgeCache.get(variable));
    } else {
      Edge e = initNewEdge(edgeBodyContext, isIncoming);
      edges.add(e);

      if (variable != null) {
        edgeCache.put(variable, e);
      }
    }
  }

  // --------------------------------------------------------------------------------------------
  //  Init handlers
  // --------------------------------------------------------------------------------------------

  private Graph initNewGraph(GDLParser.GraphContext graph) {
    Graph g = new Graph();
    g.setId(getNewGraphId());
    String label = getLabel(graph.header());
    g.setLabel(label != null ? label : DEFAULT_GRAPH_LABEL);
    g.setProperties(getProperties(graph.properties()));

    return g;
  }

  private Vertex initNewVertex(GDLParser.VertexContext vertex) {
    Vertex v = new Vertex();
    v.setId(getNewVertexId());
    String label = getLabel(vertex.header());
    v.setLabel(label != null ? label : DEFAULT_VERTEX_LABEL);
    v.setProperties(getProperties(vertex.properties()));
    v.addToGraph(getCurrentGraphId());

    return v;
  }

  private Edge initNewEdge(GDLParser.EdgeBodyContext edgeBodyContext, boolean isIncoming) {
    boolean hasBody = edgeBodyContext != null;
    Edge e = new Edge();
    e.setId(getNewEdgeId());
    e.setSourceVertexId(getSourceVertexId(isIncoming));
    e.setTargetVertexId(getTargetVertexId(isIncoming));

    String label = (hasBody ? getLabel(edgeBodyContext.header()) : null);
    e.setLabel(label != null ? label : DEFAULT_EDGE_LABEL);

    e.setProperties(hasBody ? getProperties(edgeBodyContext.properties()) : null);
    e.addToGraph(getCurrentGraphId());

    return e;
  }

  // --------------------------------------------------------------------------------------------
  //  Update handlers
  // --------------------------------------------------------------------------------------------

  private void updateGraphElement(GraphElement graphElement) {
    graphElement.addToGraph(getCurrentGraphId());
  }


  // --------------------------------------------------------------------------------------------
  //  Payload handlers
  // --------------------------------------------------------------------------------------------

  private String getVariable(GDLParser.HeaderContext header) {
    if (header != null && header.Identifier() != null) {
      return header.Identifier().getText();
    }
    return null;
  }

  private String getLabel(GDLParser.HeaderContext header) {
    if (header != null && header.Label() != null) {
      return header.Label().getText();
    }
    return null;
  }

  private Map<String, Object> getProperties(GDLParser.PropertiesContext propertiesContext) {
    if (propertiesContext != null) {
      Map<String, Object> properties = Maps.newHashMap();
      for (GDLParser.PropertyContext property : propertiesContext.property()) {
        properties.put(property.Identifier().getText(), getPropertyValue(property));
      }
      return properties;
    }
    return null;
  }

  private Object getPropertyValue(GDLParser.PropertyContext propertyContext) {
    GDLParser.LiteralContext value = propertyContext.literal();
    if (value.StringLiteral() != null) {
      return value.StringLiteral().getText();
    } else if (value.BooleanLiteral() != null) {
      return Boolean.parseBoolean(value.BooleanLiteral().getText());
    } else if (value.IntegerLiteral() != null) {
      return Integer.parseInt(value.IntegerLiteral().getText());
    }
    return null;
  }

  // --------------------------------------------------------------------------------------------
  //  Identifier management
  // --------------------------------------------------------------------------------------------

  private Long getCurrentGraphId() {
    return currentGraphId;
  }

  private Long getNewGraphId() {
    return ++currentGraphId;
  }

  private Long getNewVertexId() {
    return ++currentVertexId;
  }

  private Long getNewEdgeId() {
    return ++currentEdgeId;
  }

  // --------------------------------------------------------------------------------------------
  //  Helper
  // --------------------------------------------------------------------------------------------

  private Long getSourceVertexId(boolean isIncoming) {
    return isIncoming ? currentVertexId : currentVertexId + 1;
  }

  private Long getTargetVertexId(boolean isIncoming) {
    return isIncoming ? currentVertexId + 1 : currentVertexId;
  }
}
