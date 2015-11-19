/*
 * This file is part of GDL.
 *
 * GDL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GDL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GDL.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.s1ck.gdl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.s1ck.gdl.model.Edge;
import org.s1ck.gdl.model.Graph;
import org.s1ck.gdl.model.GraphElement;
import org.s1ck.gdl.model.Vertex;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class GDLLoader extends GDLBaseListener {

  private static final String DEFAULT_GRAPH_LABEL = "__GRAPH";
  private static final String DEFAULT_VERTEX_LABEL = "__VERTEX";
  private static final String DEFAULT_EDGE_LABEL = "__EDGE";

  // used to cache elements which are used with variables
  private final Map<String, Graph> graphCache;
  private final Map<String, Vertex> vertexCache;
  private final Map<String, Edge> edgeCache;

  // used to hold the final database elements
  private final Set<Graph> graphs;
  private final Set<Vertex> vertices;
  private final Set<Edge> edges;

  private final String defaultGraphLabel;
  private final String defaultVertexLabel;
  private final String defaultEdgeLabel;

  // needs to be replaced by GradoopId factory
  private long currentGraphId = 0L;
  private long currentVertexId = 0L;
  private long currentEdgeId = 0L;

  // flag that tells if the parser is inside a logical graph
  private boolean inGraph = false;
  // used to track vertex and edge ids for correct source and target binding
  private Vertex lastSeenVertex;
  private Edge lastSeenEdge;

  /**
   * Initializes a new GDL Loader.
   */
  public GDLLoader() {
    this(null, null, null);
  }

  /**
   * Initializes a new GDL Loader.
   *
   * @param defaultGraphLabel   graph label to be used if no label is given in the GDL script
   * @param defaultVertexLabel  vertex label to be used if no label is given in the GDL script
   * @param defaultEdgeLabel    edge label to be used if no label is given in the GDL script
   */
  public GDLLoader(String defaultGraphLabel, String defaultVertexLabel, String defaultEdgeLabel) {
    this.defaultGraphLabel = (defaultGraphLabel != null) ?
      defaultGraphLabel : DEFAULT_GRAPH_LABEL;
    this.defaultVertexLabel = (defaultVertexLabel != null) ?
      defaultVertexLabel : DEFAULT_VERTEX_LABEL;
    this.defaultEdgeLabel = (defaultEdgeLabel != null) ?
      defaultEdgeLabel : DEFAULT_EDGE_LABEL;

    graphCache = Maps.newHashMap();
    vertexCache = Maps.newHashMap();
    edgeCache = Maps.newHashMap();

    graphs = Sets.newHashSet();
    vertices = Sets.newHashSet();
    edges = Sets.newHashSet();
  }

  /**
   * Returns a collection of all graphs defined in the GDL script.
   *
   * @return graph collection
   */
  public Collection<Graph> getGraphs() {
    return graphs;
  }

  /**
   * Returns a collection of all vertices defined in the GDL script.
   *
   * @return vertex collection
   */
  public Collection<Vertex> getVertices() {
    return vertices;
  }

  /**
   * Returns a collection of all edges defined in the GDL script.
   *
   * @return edge collection
   */
  public Collection<Edge> getEdges() {
    return edges;
  }

  /**
   * Returns the graph cache that contains a mapping from variables used in the GDL script to
   * graph instances.
   *
   * @return immutable graph cache
   */
  public ImmutableMap<String, Graph> getGraphCache() {
    return new ImmutableMap.Builder<String, Graph>().putAll(graphCache).build();
  }

  /**
   * Returns the vertex cache that contains a mapping from variables used in the GDL script to
   * vertex instances.
   *
   * @return immutable vertex cache
   */
  public ImmutableMap<String, Vertex> getVertexCache() {
    return new ImmutableMap.Builder<String, Vertex>().putAll(vertexCache).build();
  }

  /**
   * Returns the edge cache that contains a mapping from variables used in the GDL script to edge
   * instances.
   *
   * @return immutable edge cache
   */
  public Map<String, Edge> getEdgeCache() {
    return new ImmutableMap.Builder<String, Edge>().putAll(edgeCache).build();
  }

  /**
   * Called when parser enters a graph context.
   *
   * Checks if the graph has already been created (using its variable). If not, a new graph is
   * created and added to the graph cache.
   *
   * @param graphContext graph context
   */
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

  /**
   * Called when parser enters a vertex context.
   *
   * Checks if the vertex has already been created (using its variable). If not, a new vertex is
   * created and added to the vertex cache.
   *
   * @param vertexContext vertex context
   */
  @Override
  public void enterVertex(GDLParser.VertexContext vertexContext) {
    String variable = getVariable(vertexContext.header());
    Vertex v;
    if (variable != null && vertexCache.containsKey(variable)) {
      v = vertexCache.get(variable);
    } else {
      v = initNewVertex(vertexContext);
      vertices.add(v);

      if (variable != null) {
        vertexCache.put(variable, v);
      }
    }
    updateGraphElement(v);
    setLastSeenVertex(v);
    updateLastSeenEdge(v);
  }

  /**
   * Called when parser enters an incoming edge context.
   *
   * @param incomingEdgeContext incoming edge context
   */
  @Override
  public void enterIncomingEdge(GDLParser.IncomingEdgeContext incomingEdgeContext) {
    processEdge(incomingEdgeContext.edgeBody(), true);
  }

  /**
   * Called when parser enters an outgoing edge context.
   *
   * @param outgoingEdgeContext outgoing edge context
   */
  @Override
  public void enterOutgoingEdge(GDLParser.OutgoingEdgeContext outgoingEdgeContext) {
    processEdge(outgoingEdgeContext.edgeBody(), false);
  }

  /**
   * Processes incoming and outgoing edges.
   *
   * Checks if the edge has already been created (using its variable). If not, a new edge is created
   * and added to the edge cache.
   *
   * @param edgeBodyContext edge body context
   * @param isIncoming      true, if edge is incoming, false for outgoing edge
   */
  private void processEdge(GDLParser.EdgeBodyContext edgeBodyContext, boolean isIncoming) {
    String variable = null;
    Edge e;

    if (edgeBodyContext != null) {
      variable = getVariable(edgeBodyContext.header());
    }
    if (variable != null && edgeCache.containsKey(variable)) {
      e = edgeCache.get(variable);
    } else {
      e = initNewEdge(edgeBodyContext, isIncoming);
      edges.add(e);

      if (variable != null) {
        edgeCache.put(variable, e);
      }
    }
    updateGraphElement(e);
    setLastSeenEdge(e);
  }

  // --------------------------------------------------------------------------------------------
  //  Init handlers
  // --------------------------------------------------------------------------------------------

  /**
   * Initializes a new graph from a given graph context.
   *
   * @param graphContext graph context
   * @return new graph
   */
  private Graph initNewGraph(GDLParser.GraphContext graphContext) {
    Graph g = new Graph();
    g.setId(getNewGraphId());
    String label = getLabel(graphContext.header());
    g.setLabel(label != null ? label : defaultGraphLabel);
    g.setProperties(getProperties(graphContext.properties()));

    return g;
  }

  /**
   * Initializes a new vertex from a given vertex context.
   *
   * @param vertexContext vertex context
   * @return new vertex
   */
  private Vertex initNewVertex(GDLParser.VertexContext vertexContext) {
    Vertex v = new Vertex();
    v.setId(getNewVertexId());
    String label = getLabel(vertexContext.header());
    v.setLabel(label != null ? label : defaultVertexLabel);
    v.setProperties(getProperties(vertexContext.properties()));

    return v;
  }

  /**
   * Initializes a new edge from the given edge body context.
   *
   * @param edgeBodyContext edge body context
   * @param isIncoming      true, if it's an incoming edge, false for outgoing edge
   * @return new edge
   */
  private Edge initNewEdge(GDLParser.EdgeBodyContext edgeBodyContext, boolean isIncoming) {
    boolean hasBody = edgeBodyContext != null;
    Edge e = new Edge();
    e.setId(getNewEdgeId());
    e.setSourceVertexId(getSourceVertexId(isIncoming));
    e.setTargetVertexId(getTargetVertexId(isIncoming));

    String label = (hasBody ? getLabel(edgeBodyContext.header()) : null);
    e.setLabel(label != null ? label : defaultEdgeLabel);

    e.setProperties(hasBody ? getProperties(edgeBodyContext.properties()) : null);

    return e;
  }

  // --------------------------------------------------------------------------------------------
  //  Update handlers
  // --------------------------------------------------------------------------------------------

  /**
   * If the parser is currently inside a logical graph, the given element is added to that graph.
   *
   * @param graphElement graph element ({@link Vertex}, {@link Edge})
   */
  private void updateGraphElement(GraphElement graphElement) {
    if (inGraph) {
      graphElement.addToGraph(getCurrentGraphId());
    }
  }


  // --------------------------------------------------------------------------------------------
  //  Payload handlers
  // --------------------------------------------------------------------------------------------

  /**
   * Returns the element variable from a given header context.
   *
   * @param header header context
   * @return element variable or {@code null} if context was null
   */
  private String getVariable(GDLParser.HeaderContext header) {
    if (header != null && header.Identifier() != null) {
      return header.Identifier().getText();
    }
    return null;
  }

  /**
   * Returns the element label from a given header context.
   *
   * @param header header context
   * @return element label or {@code null} if context was null
   */
  private String getLabel(GDLParser.HeaderContext header) {
    if (header != null && header.Label() != null) {
      return header.Label().getText();
    }
    return null;
  }

  /**
   * Returns the properties map from a given properties context.
   *
   * @param propertiesContext properties context
   * @return properties map or {@code null} if context was null
   */
  private Map<String, Object> getProperties(GDLParser.PropertiesContext propertiesContext) {
    if (propertiesContext != null) {
      Map<String, Object> properties = Maps.newHashMap();
      for (GDLParser.PropertyContext property : propertiesContext.property()) {
        properties.put(property.Identifier().getText(), getPropertyValue(property.literal()));
      }
      return properties;
    }
    return null;
  }

  /**
   * Returns the corresponding value for a given literal.
   *
   * @param literalContext literal context
   * @return parsed value
   */
  private Object getPropertyValue(GDLParser.LiteralContext literalContext) {
    if (literalContext.StringLiteral() != null) {
      return literalContext.StringLiteral().getText().replaceAll("^.|.$", "");
    } else if (literalContext.BooleanLiteral() != null) {
      return Boolean.parseBoolean(literalContext.BooleanLiteral().getText());
    } else if (literalContext.IntegerLiteral() != null) {
      return Integer.parseInt(literalContext.IntegerLiteral().getText());
    } else if(literalContext.FloatLiteral() != null) {
      return Float.parseFloat(literalContext.FloatLiteral().getText());
    }
    return null;
  }

  // --------------------------------------------------------------------------------------------
  //  Identifier management
  // --------------------------------------------------------------------------------------------

  /**
   * Returns the current graph identifier.
   *
   * @return current graph identifier
   */
  private Long getCurrentGraphId() {
    return currentGraphId;
  }

  /**
   * Creates and returns an new graph identifier.
   *
   * @return new graph identifier
   */
  private Long getNewGraphId() {
    return ++currentGraphId;
  }

  /**
   * Creates and returns a new vertex identifier.
   *
   * @return new vertex identifier
   */
  private Long getNewVertexId() {
    return ++currentVertexId;
  }

  /**
   * Creates and returns a new edge identifier.
   *
   * @return new edge identifier
   */
  private Long getNewEdgeId() {
    return ++currentEdgeId;
  }

  // --------------------------------------------------------------------------------------------
  //  Helper
  // --------------------------------------------------------------------------------------------

  /**
   * Updates the source or target vertex identifier of the last seen edge.
   *
   * @param v current vertex
   */
  private void updateLastSeenEdge(Vertex v) {
    Edge lastSeenEdge = getLastSeenEdge();
    if (lastSeenEdge != null) {
      if (lastSeenEdge.getSourceVertexId() == null) {
        lastSeenEdge.setSourceVertexId(v.getId());
      } else if (lastSeenEdge.getTargetVertexId() == null) {
        lastSeenEdge.setTargetVertexId(v.getId());
      }
    }
  }

  /**
   * Returns the vertex that was last seen by the parser.
   *
   * @return vertex seen last
   */
  private Vertex getLastSeenVertex() {
    return lastSeenVertex;
  }

  /**
   * Sets the last seen vertex.
   *
   * @param v vertex
   */
  private void setLastSeenVertex(Vertex v) {
    lastSeenVertex = v;
  }

  /**
   * Returns the edge that was last seen by the parser.
   *
   * @return edge seen last
   */
  private Edge getLastSeenEdge() {
    return lastSeenEdge;
  }

  /**
   * Sets the last seen edge.
   *
   * @param e edge
   */
  private void setLastSeenEdge(Edge e) {
    lastSeenEdge = e;
  }

  /**
   * Returns the source vertex identifier of an edge.
   *
   * @param isIncoming true, if edge is an incoming edge, false for outgoing edge
   * @return the source vertex identifier or {@code null} if vertex has not been parsed yet
   */
  private Long getSourceVertexId(boolean isIncoming) {
    return isIncoming ? null : getLastSeenVertex().getId();
  }

  /**
   * Returns the target vertex identifier of an edge.
   *
   * @param isIncoming true, if edge is an incoming edge, false for outgoing edge
   * @return the target vertex identifier or {@code null} if vertex has not been parsed yet
   */
  private Long getTargetVertexId(boolean isIncoming) {
    return isIncoming ? getLastSeenVertex().getId() : null;
  }
}
