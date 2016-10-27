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
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.s1ck.gdl.model.*;
import org.s1ck.gdl.model.operators.And;
import org.s1ck.gdl.model.operators.Comparison;
import org.s1ck.gdl.model.operators.Filter;
import org.s1ck.gdl.model.operators.Not;
import org.s1ck.gdl.model.operators.Or;
import org.s1ck.gdl.model.operators.Xor;
import org.s1ck.gdl.model.operators.comparables.ComparableExpression;
import org.s1ck.gdl.model.operators.comparables.Literal;
import org.s1ck.gdl.model.operators.comparables.PropertySelector;

import java.util.*;

public class GDLLoader extends GDLBaseListener {

  // used to cache elements which are used with variables
  private final Map<String, Graph> graphCache;
  private final Map<String, Vertex> vertexCache;
  private final Map<String, Edge> edgeCache;

  // used to hold the final database elements
  private final Set<Graph> graphs;
  private final Set<Vertex> vertices;
  private final Set<Edge> edges;
  private Filter filter;

  private final String defaultGraphLabel;
  private final String defaultVertexLabel;
  private final String defaultEdgeLabel;

  // used to generate sequential ids
  private long nextGraphId = 0L;
  private long nextVertexId = 0L;
  private long nextEdgeId = 0L;

  // flag that tells if the parser is inside a logical graph
  private boolean inGraph = false;
  // holds the graph of the current graph
  private long currentGraphId;

  // used to track vertex and edge ids for correct source and target
  // binding
  private Vertex lastSeenVertex;
  private Edge lastSeenEdge;

  // used to keep track of filter that are yet to be handled
  private ArrayDeque<Filter> currentFilters;

  /**
   * Initializes a new GDL Loader.
   *
   * @param defaultGraphLabel   graph label to be used if no label is given in the GDL script
   * @param defaultVertexLabel  vertex label to be used if no label is given in the GDL script
   * @param defaultEdgeLabel    edge label to be used if no label is given in the GDL script
   */
  GDLLoader(String defaultGraphLabel, String defaultVertexLabel, String defaultEdgeLabel) {
    this.defaultGraphLabel = defaultGraphLabel;
    this.defaultVertexLabel = defaultVertexLabel;
    this.defaultEdgeLabel = defaultEdgeLabel;

    graphCache = Maps.newHashMap();
    vertexCache = Maps.newHashMap();
    edgeCache = Maps.newHashMap();

    graphs = Sets.newHashSet();
    vertices = Sets.newHashSet();
    edges = Sets.newHashSet();
    filter = null;


    currentFilters = new ArrayDeque<>();
  }

  /**
   * Returns a collection of all graphs defined in the GDL script.
   *
   * @return graph collection
   */
  Collection<Graph> getGraphs() {
    return graphs;
  }

  /**
   * Returns a collection of all vertices defined in the GDL script.
   *
   * @return vertex collection
   */
  Collection<Vertex> getVertices() {
    return vertices;
  }

  /**
   * Returns a collection of all edges defined in the GDL script.
   *
   * @return edge collection
   */
  Collection<Edge> getEdges() {
    return edges;
  }

    /**
     * Returns the predicates defined by the query represented as a tree
     *
     * @return filter
     */
  Filter getFilter() { return filter; }
  /**
   * Returns the graph cache that contains a mapping from variables used in the GDL script to
   * graph instances.
   *
   * @return immutable graph cache
   */
  ImmutableMap<String, Graph> getGraphCache() {
    return new ImmutableMap.Builder<String, Graph>().putAll(graphCache).build();
  }

  /**
   * Returns the vertex cache that contains a mapping from variables used in the GDL script to
   * vertex instances.
   *
   * @return immutable vertex cache
   */
  ImmutableMap<String, Vertex> getVertexCache() {
    return new ImmutableMap.Builder<String, Vertex>().putAll(vertexCache).build();
  }

  /**
   * Returns the edge cache that contains a mapping from variables used in the GDL script to edge
   * instances.
   *
   * @return immutable edge cache
   */
  Map<String, Edge> getEdgeCache() {
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
    Graph g;
    if (variable != null) {
      if (!graphCache.containsKey(variable)) {
        g = initNewGraph(graphContext);
        g.setVariable(variable);
        graphs.add(g);
        graphCache.put(variable, g);
      } else {
        g = graphCache.get(variable);
      }
    } else {
      g = initNewGraph(graphContext);
      graphs.add(g);
    }
    currentGraphId = g.getId();}

  @Override
  public void exitGraph(GDLParser.GraphContext ctx) {
    inGraph = false;
  }

  /**
   * When leaving a query context its save to add the pattern predicates to the filters
   *
   * @param ctx query context
   */
  @Override
  public void exitQuery(GDLParser.QueryContext ctx) {
    for(Vertex v : vertices) {
      addFilter(Filter.fromGraphElement(v));
    }
    for(Edge e : edges) {
      addFilter(Filter.fromGraphElement(e));
    }
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
      v.setVariable(variable);
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
   * Called when the parser leaves a WHERE expression
   *
   * Takes care that the filter build from the current expression is stored
   * in the graph
   *
   * @param ctx where context
   */
  @Override
  public void exitWhere(GDLParser.WhereContext ctx) {
    addFilter(new ArrayList<>(Arrays.asList(currentFilters.pop())));
  }

  /**
   * Builds a {@code Comparison} expression from comparison context
   *
   * @param ctx comparison context
   */
  @Override
  public void enterComparisonExpression(GDLParser.ComparisonExpressionContext ctx) {
    currentFilters.add(buildComparison(ctx));
  }

  /**
   * Called when we leave an Expression4.
   *
   * Checks if the expression is preceded by a Not and adds the filter in that case.
   * @param ctx expression context
   */
  @Override
  public void exitExpression4(GDLParser.Expression4Context ctx) {
    if (!ctx.NOT().isEmpty()) {
      Filter not = new Not(currentFilters.pop());
      currentFilters.add(not);
    }
  }

  /**
   * Called when parser leaves Expression5
   *
   * Checks if we have conjunctions like AND, OR, XOR and builds filter for them.
   * @param ctx expression context
   */
  @Override
  public void exitExpression5(GDLParser.Expression5Context ctx) {
    List<TerminalNode> conjuctions = ctx.Conjunction();
    Filter conjunctionReuse;

    for(int i=conjuctions.size()-1;i>=0;i--) {
      Filter rhs = currentFilters.removeLast();
      Filter lhs = currentFilters.removeLast();

      switch(conjuctions.get(i).getText().toLowerCase()) {
        case "and": conjunctionReuse = new And(lhs, rhs);
          break;
        case "or":  conjunctionReuse = new Or(lhs, rhs);
          break;
        default: conjunctionReuse = new Xor(lhs, rhs);
          break;
      }
      currentFilters.add(conjunctionReuse);
    }
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
      e.setVariable(variable);
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

    if(hasBody) {
      String label = getLabel(edgeBodyContext.header());
      e.setLabel(label != null ? label : defaultEdgeLabel);
      e.setProperties(getProperties(edgeBodyContext.properties()));
      e.setLengthRange(parseEdgeLengthContext(edgeBodyContext.edgeLength()));
    }
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
      graphElement.addToGraph(getNextGraphId());
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
    if (header != null && header.label() != null) {
      return header.label().getText().substring(1);
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
    String text;
    if (literalContext.StringLiteral() != null) {
      return literalContext.StringLiteral().getText().replaceAll("^.|.$", "");
    } else if (literalContext.BooleanLiteral() != null) {
      return Boolean.parseBoolean(literalContext.BooleanLiteral().getText());
    } else if (literalContext.IntegerLiteral() != null) {
      text = literalContext.IntegerLiteral().getText().toLowerCase();
      if (text.endsWith("l")) {
        return Long.parseLong(text.substring(0, text.length() - 1));
      }
      return Integer.parseInt(text);
    } else if(literalContext.FloatingPointLiteral() != null) {
      text = literalContext.FloatingPointLiteral().getText().toLowerCase();
      if (text.endsWith("f")) {
        return Float.parseFloat(text.substring(0, text.length() - 1));
      } else if (text.endsWith("d")) {
        return Double.parseDouble(text.substring(0, text.length() - 1));
      }
      return Float.parseFloat(text);
    }
    return null;
  }

  /**
   * Parses an {@code EdgeLengthContext} and returns the indicated Range
   *
   * @param lengthCtx the edges length context
   * @return edge length range
   */
  private Range<Integer> parseEdgeLengthContext(GDLParser.EdgeLengthContext lengthCtx) {
    int lowerBound;
    int upperBound;

    if(lengthCtx != null) {
      int children = lengthCtx.getChildCount();

      if(children == 4) {
        lowerBound = terminalNodeToInt(lengthCtx.IntegerLiteral(0));
        upperBound = terminalNodeToInt(lengthCtx.IntegerLiteral(1));
        return Range.closed(lowerBound, upperBound);

      } else if(children == 3) {
        upperBound = terminalNodeToInt(lengthCtx.IntegerLiteral(0));
        return Range.atMost(upperBound);

      } else if(children == 2){
        lowerBound = terminalNodeToInt(lengthCtx.IntegerLiteral(0));
        return Range.atLeast(lowerBound);
      } else { return Range.all(); }

    }
    return Range.closed(1, 1);
  }

  /**
   * Builds a Comparison filter operator from comparison context
   *
   * @param ctx the comparisson context that will be parsed
   * @return parsed operator
   */
  private Comparison buildComparison(GDLParser.ComparisonExpressionContext ctx) {
    ComparableExpression lhs = buildPropertySelector(ctx.propertyLookup(0));

    ComparableExpression rhs = ctx.literal() != null ?
            new Literal(getPropertyValue(ctx.literal())) : buildPropertySelector(ctx.propertyLookup(1));

    Comparison.Comparator comp = Comparison.Comparator.fromString(ctx .ComparisonOP().getText());

    return new Comparison(lhs, comp, rhs);
  }

  /**
   * Builds an property selector expression like alice.age
   *
   * @param ctx the property lookup context that will be parsed
   * @return parsed property selector expression
   */
  private PropertySelector buildPropertySelector(GDLParser.PropertyLookupContext ctx) {
    GraphElement element;

    String identifier = ctx.Identifier(0).getText();
    String property = ctx.Identifier(1).getText();

    if(vertexCache.containsKey(identifier)) {
      element = vertexCache.get(identifier);
    }
    else if(edgeCache.containsKey(identifier)) {
      element = edgeCache.get(identifier);
    }
    else { return null; } //TODO raise reference error

    return new PropertySelector(element,property);
  }

  // --------------------------------------------------------------------------------------------
  //  Identifier management
  // --------------------------------------------------------------------------------------------

  /**
   * Returns the current graph identifier.
   *
   * @return current graph identifier
   */
  private Long getNextGraphId() {
    return currentGraphId;
  }

  /**
   * Creates and returns an new graph identifier.
   *
   * @return new graph identifier
   */
  private Long getNewGraphId() {
    return nextGraphId++;
  }

  /**
   * Creates and returns a new vertex identifier.
   *
   * @return new vertex identifier
   */
  private Long getNewVertexId() {
    return nextVertexId++;
  }

  /**
   * Creates and returns a new edge identifier.
   *
   * @return new edge identifier
   */
  private Long getNewEdgeId() {
    return nextEdgeId++;
  }

  // --------------------------------------------------------------------------------------------
  //  Helper
  // --------------------------------------------------------------------------------------------

  /**
   * Adds a list of filters to the current filter using AND conjuctions
   *
   * @param newFilters the filters to add
   */
  private void addFilter(ArrayList<Filter> newFilters) {
    for(Filter newFilter : newFilters) {
      if(filter!=null) {
        filter = new And(filter,newFilter);
      } else {
        filter = newFilter;
      }
    }
  }

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
   * Extracts the predicates from a GraphElement and transforms them into a filter
   *
   * @param e
   */

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

  /**
   * Parses a terminal node to an integer.
   *
   * @param node the node which represents an integer
   * @return the parsed integer
   */
  private int terminalNodeToInt(TerminalNode node) {
    return Integer.parseInt(node.getText());
  }
}
