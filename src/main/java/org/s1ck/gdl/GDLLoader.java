/*
 * Copyright 2017 The GDL Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.s1ck.gdl;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.s1ck.gdl.exceptions.InvalidReferenceException;
import org.s1ck.gdl.model.*;
import org.s1ck.gdl.model.comparables.ElementSelector;
import org.s1ck.gdl.model.comparables.time.*;
import org.s1ck.gdl.model.comparables.time.TimeConstant;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.booleans.Not;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.booleans.Xor;
import org.s1ck.gdl.model.comparables.ComparableExpression;
import org.s1ck.gdl.model.comparables.Literal;
import org.s1ck.gdl.model.comparables.PropertySelector;
import org.s1ck.gdl.utils.Comparator;

import java.util.*;
import java.util.stream.Collectors;

import static org.s1ck.gdl.utils.Comparator.*;

class GDLLoader extends GDLBaseListener {

  // used to cache elements which are assigned to user-defined variables
  private final Map<String, Graph> userGraphCache;
  private final Map<String, Vertex> userVertexCache;
  private final Map<String, Edge> userEdgeCache;

  // used to map graphs to their elements
  private Map<Long, ArrayList<GraphElement>> graphElements;

  // used to cache elements which are assigned to auto-generated variables
  private final Map<String, Graph> autoGraphCache;
  private final Map<String, Vertex> autoVertexCache;
  private final Map<String, Edge> autoEdgeCache;

  // used to hold the final database elements
  private final Set<Graph> graphs;
  private final Set<Vertex> vertices;
  private final Set<Edge> edges;

  // stores the predicates tree for that query
  private Predicate predicates;

  private final boolean useDefaultGraphLabel;
  private final boolean useDefaultVertexLabel;
  private final boolean useDefaultEdgeLabel;

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

  // used to track vertex and edge ids for correct source and target binding
  private Vertex lastSeenVertex;
  private Edge lastSeenEdge;

  // used to keep track of filter that are yet to be handled
  private Deque<Predicate> currentPredicates;

  // used to generate variable names if none is given
  private static final String ANONYMOUS_GRAPH_VARIABLE = "__g%d";
  private static final String ANONYMOUS_VERTEX_VARIABLE = "__v%d";
  private static final String ANONYMOUS_EDGE_VARIABLE = "__e%d";

  // should predicates be reformulated to simple comparisons?
  private boolean processPredicates = true;


  /**
   * Initializes a new GDL Loader.
   *
   * @param defaultGraphLabel   graph label to be used if no label is given in the GDL script
   * @param defaultVertexLabel  vertex label to be used if no label is given in the GDL script
   * @param defaultEdgeLabel    edge label to be used if no label is given in the GDL script
   */
  GDLLoader(String defaultGraphLabel, String defaultVertexLabel, String defaultEdgeLabel) {
    this(defaultGraphLabel, defaultVertexLabel, defaultEdgeLabel,
      true, true, true);
  }

  /**
   * Initializes a new GDL Loader.
   *
   * @param defaultGraphLabel   graph label to be used if no label is given in the GDL script
   * @param defaultVertexLabel  vertex label to be used if no label is given in the GDL script
   * @param defaultEdgeLabel    edge label to be used if no label is given in the GDL script
   * @param processPredicates   true iff predicates should be reformulated to simple (atomic) comparisons
   */
  GDLLoader(String defaultGraphLabel, String defaultVertexLabel, String defaultEdgeLabel,
            boolean processPredicates) {
    this(defaultGraphLabel, defaultVertexLabel, defaultEdgeLabel);
    this.processPredicates = processPredicates;
  }

  /**
   * Initializes a new GDL Loader.
   *
   * @param defaultGraphLabel     graph label to be used if no label is given in the GDL script
   * @param defaultVertexLabel    vertex label to be used if no label is given in the GDL script
   * @param defaultEdgeLabel      edge label to be used if no label is given in the GDL script
   * @param useDefaultGraphLabel  enable default graph label
   * @param useDefaultVertexLabel enable default vertex label
   * @param useDefaultEdgeLabel   enable default edge label
   */
  GDLLoader(String defaultGraphLabel, String defaultVertexLabel, String defaultEdgeLabel,
    boolean useDefaultGraphLabel, boolean useDefaultVertexLabel, boolean useDefaultEdgeLabel) {

    this.useDefaultGraphLabel = useDefaultGraphLabel;
    this.useDefaultVertexLabel = useDefaultVertexLabel;
    this.useDefaultEdgeLabel = useDefaultEdgeLabel;

    this.defaultGraphLabel  = defaultGraphLabel;
    this.defaultVertexLabel = defaultVertexLabel;
    this.defaultEdgeLabel   = defaultEdgeLabel;

    userGraphCache = new HashMap<>();
    userVertexCache = new HashMap<>();
    userEdgeCache = new HashMap<>();

    graphElements = new HashMap<>();

    autoGraphCache = new HashMap<>();
    autoVertexCache = new HashMap<>();
    autoEdgeCache = new HashMap<>();

    graphs    = new HashSet<>();
    vertices  = new HashSet<>();
    edges     = new HashSet<>();

    currentPredicates = new ArrayDeque<>();
  }

  /**
   * Initializes a new GDL Loader.
   *
   * @param defaultGraphLabel     graph label to be used if no label is given in the GDL script
   * @param defaultVertexLabel    vertex label to be used if no label is given in the GDL script
   * @param defaultEdgeLabel      edge label to be used if no label is given in the GDL script
   * @param useDefaultGraphLabel  enable default graph label
   * @param useDefaultVertexLabel enable default vertex label
   * @param useDefaultEdgeLabel   enable default edge label
   */
  GDLLoader(String defaultGraphLabel, String defaultVertexLabel, String defaultEdgeLabel,
            boolean useDefaultGraphLabel, boolean useDefaultVertexLabel, boolean useDefaultEdgeLabel,
            boolean processPredicates) {

    this(defaultGraphLabel, defaultVertexLabel, defaultEdgeLabel, useDefaultGraphLabel, useDefaultVertexLabel,
            useDefaultEdgeLabel);
    this.processPredicates = processPredicates;
  }

  /**
   * Returns the default graph label.
   *
   * @return default graph label
   */
  public String getDefaultGraphLabel() {
    return defaultGraphLabel;
  }

  /**
   * Returns the default vertex label
   *
   * @return default vertex label
   */
  public String getDefaultVertexLabel() {
    return defaultVertexLabel;
  }

  /**
   * Returns the default edge label
   *
   * @return default edge label
   */
  public String getDefaultEdgeLabel() {
    return defaultEdgeLabel;
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
     * Returns the predicates defined by the query.
     *
     * @return predicates
     */
  Optional<Predicate> getPredicates() {
    return predicates != null ? Optional.of(predicates) : Optional.empty();
  }
  /**
   * Returns a cache that contains a mapping from user-defined variables used in the GDL script to
   * graph instances.
   *
   * @return immutable graph cache
   */
  Map<String, Graph> getGraphCache() {
    return getGraphCache(true, false);
  }

  /**
   * Returns a cache containing a mapping from variables to graphs.
   *
   * @param includeUserDefined include user-defined variables
   * @param includeAutoGenerated include auto-generated variables
   *
   * @return immutable graph cache
   */
  Map<String, Graph> getGraphCache(boolean includeUserDefined, boolean includeAutoGenerated) {
    return getCache(userGraphCache, autoGraphCache, includeUserDefined, includeAutoGenerated);
  }

  /**
   * Returns a cache that contains a mapping from user-defined variables used in the GDL script to
   * vertex instances.
   *
   * @return immutable vertex cache
   */
  Map<String, Vertex> getVertexCache() {
    return getVertexCache(true, false);
  }

  /**
   * Returns a cache containing a mapping from variables to vertices.
   *
   * @param includeUserDefined include user-defined variables
   * @param includeAutoGenerated include auto-generated variables
   *
   * @return immutable vertex cache
   */
  Map<String, Vertex> getVertexCache(boolean includeUserDefined, boolean includeAutoGenerated) {
    return getCache(userVertexCache, autoVertexCache, includeUserDefined, includeAutoGenerated);
  }

  /**
   * Returns a cache that contains a mapping from user-defined variables used in the GDL script to
   * edge instances.
   *
   * @return immutable edge cache
   */
  Map<String, Edge> getEdgeCache() {
    return getEdgeCache(true, false);
  }

  /**
   * Returns a cache containing a mapping from variables to edges.
   *
   * @param includeUserDefined include user-defined variables
   * @param includeAutoGenerated include auto-generated variables
   *
   * @return immutable edge cache
   */
  Map<String, Edge> getEdgeCache(boolean includeUserDefined, boolean includeAutoGenerated) {
    return getCache(userEdgeCache, autoEdgeCache, includeUserDefined, includeAutoGenerated);
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
    if (variable != null && userGraphCache.containsKey(variable)) {
      g = userGraphCache.get(variable);
    } else {
      g = initNewGraph(graphContext);

      if (variable != null) {
        userGraphCache.put(variable, g);
      } else {
        variable = String.format(ANONYMOUS_GRAPH_VARIABLE, g.getId());
        autoGraphCache.put(variable, g);
      }
      g.setVariable(variable);
      graphs.add(g);
    }
    currentGraphId = g.getId();
  }

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
    if(processPredicates) {
      postprocessPredicates();
    }
    ArrayList<String> vars = new ArrayList<>();
    vars.addAll(userEdgeCache.keySet());
    vars.addAll(userVertexCache.keySet());
    vars.addAll(autoEdgeCache.keySet());
    vars.addAll(autoVertexCache.keySet());
    vars.remove(TimeSelector.GLOBAL_SELECTOR);
    if(predicates!=null) {
      predicates = predicates.replaceGlobalByLocal(vars);
    }
    for(Vertex v : vertices) {
      addPredicates(Predicate.fromGraphElement(v, getDefaultVertexLabel()));
    }
    for(Edge e : edges) {
      addPredicates(Predicate.fromGraphElement(e, getDefaultEdgeLabel()));
    }
  }

  /**
   * Reformulates the predicates when leaving the query. First, complex temporal expressions
   * like {@code MIN(t1,t2)<MAX(t2,t3)} are reduced to comparisons of simple timestamps.
   * Then, global time comparisons referring to the whole pattern are reduced to comparisons of
   * variables.
   */
  private void postprocessPredicates(){
    if(predicates!=null) {
      predicates = Predicate.unfoldTemporalComparisons(predicates);
      ArrayList<String> vars = new ArrayList<>();
      vars.addAll(userEdgeCache.keySet());
      vars.addAll(userVertexCache.keySet());
      vars.addAll(autoEdgeCache.keySet());
      vars.addAll(autoVertexCache.keySet());
      predicates = Predicate.translateGlobalPredicates(predicates, vars, true);
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
    if (variable != null && userVertexCache.containsKey(variable)) {
      v = userVertexCache.get(variable);
    } else {
      v = initNewVertex(vertexContext);

      if (variable != null) {
        userVertexCache.put(variable, v);
      } else {
        variable = String.format(ANONYMOUS_VERTEX_VARIABLE, v.getId());
        autoVertexCache.put(variable, v);
      }
      v.setVariable(variable);
      vertices.add(v);
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
   * in the graph and that default asOf(now) predicates are added iff there are no constraints
   * on any tx_to values
   *
   * @param ctx where context
   */
  @Override
  public void exitWhere(GDLParser.WhereContext ctx) {
    Predicate predicate = currentPredicates.pop();
    addPredicates(Collections.singletonList(predicate));
  }

  /**
   * Builds a {@code Comparison} expression from comparison context
   *
   * @param ctx comparison context
   */
  @Override
  public void enterComparisonExpression(GDLParser.ComparisonExpressionContext ctx) {
    currentPredicates.add(buildComparison(ctx));
  }

  @Override
  public void enterTemporalComparison(GDLParser.TemporalComparisonContext ctx){
    currentPredicates.add(buildTemporalComparison(ctx));
  }

  /**
   * Builds a {@code Predicate} from the given Intervall-Function (caller is a interval)
   * interval functions are e.g. succeeds(x), between(x,y)....
   *
   * @param ctx interval function context
   */
  @Override
  public void enterIntvF(GDLParser.IntvFContext ctx){
    currentPredicates.add(buildIntervalFunction(ctx));
  }

  /**
   * Builds a Comparison filter operator from comparison context
   *
   * @param ctx the comparison context that will be parsed
   * @return parsed operator
   */
  private Comparison buildTemporalComparison(GDLParser.TemporalComparisonContext ctx) {
    ComparableExpression lhs = buildTimePoint(ctx.timePoint(0));
    ComparableExpression rhs = buildTimePoint(ctx.timePoint(1));
    Comparator comp = Comparator.fromString(ctx .ComparisonOP().getText());

    return new Comparison(lhs, comp, rhs);
  }

  /**
   * Converts an interval function into a (complex) predicate
   * For example, i.between(x,y) would be translated to a predicate ((i.from<= y) AND (i.to>x))
   * @param ctx interval function context
   * @return complex predicate that encodes the interval function. Atoms are time stamp comparisons
   */
  private Predicate buildIntervalFunction(GDLParser.IntvFContext ctx) {
    int predicateSizeBefore = currentPredicates.size();
    TimePoint[] intv = buildIntervall(ctx.interval());
    TimePoint from = intv[0];
    TimePoint to = intv[1];
    Predicate predicate = createIntervalPredicates(from, to, ctx.intervalFunc());
    // additional constraints?
    int countConstraints = currentPredicates.size() - predicateSizeBefore;
    for(int i = 0; i<countConstraints; i++){
      predicate = new And(predicate, currentPredicates.removeFirst());
    }
    return predicate;
  }

  /**
   * Creates a new predicate about an intervall. There are different types of intervall predicates/
   * functions: precedes, fromTo,...
   * @param from represents the start time (from value) of the intervall
   * @param to  represents the end time (to value) of the intervall
   * @param intervalFunc contains the context information needed to create the correct predicate
   * @return new predicate (according to {@code intervalFunc}) about the intervall represented by
   *    {@code from} and {@code to}.
   */
  private Predicate createIntervalPredicates(TimePoint from, TimePoint to, GDLParser.IntervalFuncContext intervalFunc) {
    if(intervalFunc.overlapsIntervallOperator()!=null){
      return createOverlapsPredicates(from, to, intervalFunc.overlapsIntervallOperator());
    }
    else if(intervalFunc.fromToOperator()!=null){
      return createFromToPredicates(from, to, intervalFunc.fromToOperator());
    }
    else if(intervalFunc.betweenOperator()!=null){
      return createBetweenPredicates(from, to, intervalFunc.betweenOperator());
    }
    else if(intervalFunc.precedesOperator()!=null){
      return createPrecedesPredicates(to, intervalFunc.precedesOperator());
    }
    else if(intervalFunc.succeedsOperator()!=null){
      return createSucceedsPredicates(from, intervalFunc.succeedsOperator());
    }
    else if(intervalFunc.containsOperator()!=null){
      return createContainsPredicates(from, to, intervalFunc.containsOperator());
    }
    else if(intervalFunc.immediatelyPrecedesOperator()!=null){
      return createImmediatelyPrecedesPredicates(to, intervalFunc.immediatelyPrecedesOperator());
    }
    else if(intervalFunc.immediatelySucceedsOperator()!=null){
      return createImmediatelySucceedsPredicates(from, intervalFunc.immediatelySucceedsOperator());
    }
    else if(intervalFunc.equalsOperator()!=null){
      return createEqualsPredicates(from, to, intervalFunc.equalsOperator());
    }
    else if(intervalFunc.longerThanOperator()!=null){
      return createLongerThanPredicates(from, to, intervalFunc.longerThanOperator());
    }
    else if(intervalFunc.shorterThanOperator()!=null){
      return createShorterThanPredicates(from, to, intervalFunc.shorterThanOperator());
    }
    return null;
  }

  /**
   * Creates a predicate a.overlaps(b)=max(a.from,b.from)<min(a.to,b.to)
   * @param from the from value of the calling interval
   * @param to the to value of the calling interval
   * @param ctx the context containing the called interval
   * @return overlaps predicate
   */
  private Predicate createOverlapsPredicates(TimePoint from, TimePoint to, GDLParser.OverlapsIntervallOperatorContext ctx) {
    TimePoint[] arg = buildIntervall(ctx.interval());
    TimePoint arg_from = arg[0];
    TimePoint arg_to = arg[1];
    TimePoint mx = new MaxTimePoint(from, arg_from);
    TimePoint mn = new MinTimePoint(to, arg_to);
    return new Comparison(mx, Comparator.LT, mn);
  }


  /**
   * Creates a predicate a.fromTo(x,y)= a.from<y AND a.to>x
   * @param from from value of the calling interval
   * @param to to value of the calling interval
   * @param ctx context of the call, containing x and y
   * @return fromTo predicate
   */
  private Predicate createFromToPredicates(TimePoint from, TimePoint to, GDLParser.FromToOperatorContext ctx){
    TimePoint x = buildTimePoint(ctx.timePoint(0));
    TimePoint y = buildTimePoint(ctx.timePoint(1));
    return new And(
            new Comparison(from, Comparator.LT, y),
            new Comparison(to, Comparator.GT, x)
    );
  }

  /**
   * Creates a predicate a.between(x,y) = a.from<=y AND a.to>x
   * @param from from value of the calling interval
   * @param to to value of the calling interval
   * @param ctx context of the call, containing x and y
   * @return between predicate
   */
  private Predicate createBetweenPredicates(TimePoint from, TimePoint to, GDLParser.BetweenOperatorContext ctx){
    TimePoint x = buildTimePoint(ctx.timePoint(0));
    TimePoint y = buildTimePoint(ctx.timePoint(1));
    return new And(
            new Comparison(from, LTE, y),
            new Comparison(to, Comparator.GT, x)
    );
  }

  /**
   * Creates a predicate a.precedes(b) = a.to <= b.from.
   * Function is used for interval and timestamp function {@code precedes}, as they both
   * only compare two time stamps
   * @param to the time stamp of the caller to compare
   * @param ctx the context containing the value to be compared
   * @return precedes predicate
   */
  private Predicate createPrecedesPredicates(TimePoint to, GDLParser.PrecedesOperatorContext ctx){
    TimePoint[] arg = buildIntervall(ctx.interval());
    TimePoint arg_from = arg[0];
    return new Comparison(to, LTE, arg_from);
  }

  /**
   * Creates a predicate a.immediatelyPrecedes(b) = (a.to == b.from).
   * @param to the time stamp of the caller to compare
   * @param ctx the context containing the from value to be compared
   * @return immediatelyPrecedes predicate
   */
  private Predicate createImmediatelyPrecedesPredicates(TimePoint to, GDLParser.ImmediatelyPrecedesOperatorContext ctx){
    TimePoint[] arg = buildIntervall(ctx.interval());
    TimePoint arg_from = arg[0];
    return new Comparison(to, EQ, arg_from);
  }

  /**
   * Creates a predicate a.succeeds(b) = a >= b.
   * Function is used for interval and timestamp function {@code precedes}, as they both
   * only compare two time stamps
   * @param point the time stamp of the caller to compare
   * @param ctx the context containing the value to be compared
   * @return succeeds predicate
   */
  private Predicate createSucceedsPredicates(TimePoint point, GDLParser.SucceedsOperatorContext ctx){
    TimePoint[] arg = buildIntervall(ctx.interval());
    TimePoint arg_to = arg[1];
    return new Comparison(point, GTE, arg_to);
  }

  /**
   * Creates a predicate a.immediatelySucceeds(b) = (a.from == b.to).
   * Function is used for interval and timestamp function {@code precedes}, as they both
   * only compare two time stamps
   * @param from the from value of the caller interval
   * @param ctx the context containing the to value of the interval to be compared
   * @return immediatelySucceeds predicate
   */
  private Predicate createImmediatelySucceedsPredicates(TimePoint from,
                                                        GDLParser.ImmediatelySucceedsOperatorContext ctx){
    TimePoint[] arg = buildIntervall(ctx.interval());
    TimePoint arg_to = arg[1];
    return new Comparison(from, EQ, arg_to);
  }

  /**
   * Creates a predicate a.contains(b) = a.from<=b.from AND a.to>=b.to
   * @param from from value of the calling interval
   * @param to to value of the calling interval
   * @param ctx context of the call, containing b
   * @return contains predicate
   */
  private Predicate createContainsPredicates(TimePoint from, TimePoint to, GDLParser.ContainsOperatorContext ctx){
    if(ctx.interval()!=null){
      TimePoint[] arg = buildIntervall(ctx.interval());
      TimePoint arg_from = arg[0];
      TimePoint arg_to = arg[1];
      return new And(
              new Comparison(from, LTE, arg_from),
              new Comparison(to, GTE, arg_to)
      );
    }
    // argument is only a timestamp
    else{
      TimePoint arg = buildTimePoint(ctx.timePoint());
      return new And(
              new Comparison(from, LTE, arg), new Comparison(to, GTE, arg)
      );
    }
  }

  /**
   * Creates a predicate a.equals(b) = (a.from = b.from AND a.to = b.to).
   * @param from from value of the calling interval
   * @param to to value of the calling interval
   * @param ctx context containing the callee interval
   * @return equals predicate
   */
  private Predicate createEqualsPredicates(TimePoint from, TimePoint to, GDLParser.EqualsOperatorContext ctx){
    TimePoint[] arg = buildIntervall(ctx.interval());
    TimePoint arg_from = arg[0];
    TimePoint arg_to = arg[1];
    return new And(
            new Comparison(from, EQ, arg_from),
            new Comparison(to, EQ, arg_to)
    );
  }

  /**
   * Creates a predicate a.longerThan(b) = (length(a) > length(b))
   * @param from from value of the calling interval
   * @param to to value of the calling interval
   * @param ctx context containing the callee interval
   * @return longerThan predicate
   */
  private Predicate createLongerThanPredicates(TimePoint from, TimePoint to, GDLParser.LongerThanOperatorContext ctx){
    Duration rhs = new Duration(from, to);
    if(ctx.timeConstant()!=null) {
      TimeConstant constant = buildTimeConstant(ctx.timeConstant());
      return new Comparison(rhs, GT, constant);
    }
    else if(ctx.interval()!=null){
      TimePoint[] interval = buildIntervall(ctx.interval());
      Duration lhs = new Duration(interval[0], interval[1]);
      return new Comparison(rhs, GT, lhs);
    }
    return null;
  }

  /**
   * Creates a predicate a.shorterThan(b) = (length(a) < length(b))
   * @param from from value of the calling interval
   * @param to to value of the calling interval
   * @param ctx context containing the callee interval
   * @return shorterThan predicate
   */
  private Predicate createShorterThanPredicates(TimePoint from, TimePoint to, GDLParser.ShorterThanOperatorContext ctx){
    Duration rhs = new Duration(from, to);
    if(ctx.timeConstant()!=null) {
      TimeConstant constant = buildTimeConstant(ctx.timeConstant());
      return new Comparison(rhs, LT, constant);
    }
    else if(ctx.interval()!=null){
      TimePoint[] interval = buildIntervall(ctx.interval());
      Duration lhs = new Duration(interval[0], interval[1]);
      return new Comparison(rhs, LT, lhs);
    }
    return null;
  }

  /**
   * Creates a TimeConstant given a suitable context. Constants can be a constant number
   * of days ({@code Days(n)}), hours ({@code Hours(n)}), minutes ({@code Minutes(n)}),
   * seconds ({@code Seconds(n)}) or milliseconds ({@code Millis(n)}).
   * @param ctx the context containing the constant.
   * @return time constant
   */
  private TimeConstant buildTimeConstant(GDLParser.TimeConstantContext ctx){
    int value = Integer.parseInt(ctx.IntegerLiteral().getText());
    if(ctx.getText().startsWith("Days(")){
      return new TimeConstant(value,0,0,0,0);
    }
    else if(ctx.getText().startsWith("Hours(")){
      return new TimeConstant(0, value, 0, 0, 0);
    }
    else if(ctx.getText().startsWith("Minutes(")){
      return new TimeConstant(0, 0, value, 0, 0);
    }
    else if(ctx.getText().startsWith("Seconds(")){
      return new TimeConstant(0, 0, 0, value, 0);
    }
    else if(ctx.getText().startsWith("Millis(")){
      return new TimeConstant(0, 0, 0, 0, value);
    }
    return null;
  }

  /**
   * Creates an array {@code {from, to}} representing an intervall.
   * @param ctx context from which to derive {@code from} and {@code to}
   * @return {@code {from, to}} representing an intervall
   */
  private TimePoint[] buildIntervall(GDLParser.IntervalContext ctx) {
    if (ctx.intervalSelector()!=null){
      GDLParser.IntervalSelectorContext selector = ctx.intervalSelector();
      // throws exception, if variable invalid
      return buildIntervalFromSelector(selector);
    }
    else if(ctx.intervalFromStamps()!=null){
      GDLParser.IntervalFromStampsContext fs = ctx.intervalFromStamps();
      return buildIntervalFromStamps(fs);
    }
    else if(ctx.complexInterval()!=null){
      GDLParser.ComplexIntervalArgumentContext arg1 = ctx.complexInterval()
              .complexIntervalArgument(0);
      GDLParser.ComplexIntervalArgumentContext arg2 = ctx.complexInterval()
              .complexIntervalArgument(1);
      boolean join = ctx.getText().contains(".join(");
      return buildIntervalFromComplex(arg1, arg2, join);
    }
    return null;
  }
  
  /**
   * Creates an intervall as an array {@code {from, to}} from a selector context.
   * I.e., a intervall like {@code a.val} would result in {@code {a.val_from, a.val_to}}.
   * What is more, {@code val} results in {@code GLOBAL_SELECTOR.val_from, GLOBAL_SELECTOR.val_to}
   * @param ctx context from which to derive the interval
   * @return {@code {from, to}} representing the interval
   */
  private TimePoint[] buildIntervalFromSelector(GDLParser.IntervalSelectorContext ctx){
    String var = ctx.Identifier()!=null ?
            resolveIdentifier(ctx.Identifier().getText()) : TimeSelector.GLOBAL_SELECTOR;
    String intId = ctx.IntervalConst().getText();
    TimePoint from = new TimeSelector(var, intId+"_from");
    TimePoint to = new TimeSelector(var, intId+"_to");
    return new TimePoint[]{from, to};
  }

  /**
   * Creates an intervall as an array {@code {from, to}} from a interval constant context.
   * I.e., a intervall like {@code Interval(1970-01-01, 2020-01-01)} would result in
   * {@code {1970-01-01, 2020-01-01}}.
   * @param ctx context from which to derive the interval
   * @return {@code {from, to}} representing the interval
   */
  private TimePoint[] buildIntervalFromStamps(GDLParser.IntervalFromStampsContext ctx){
    TimePoint from = buildTimePoint(ctx.timePoint(0));
    TimePoint to = buildTimePoint(ctx.timePoint(1));
    return new TimePoint[]{from,to};
  }

  /**
   * Creates an intervall as an array {@code {from, to}} from a complex interval context, i.e.
   * {@code merge} and {@code join} expressions.
   * An intervall like {@code a.merge(b)} would result in {@code {max(a.from, b.from), min(a.to, b.to}}
   * while {@code a.join(b)} results in {@code {min(a.from, b.from), max(a.to, b.to)}}.
   * Furthermore, a constraint {max(a.from, b.from)<= min(a.to, b.to)} is added (intervals must overlap,
   * in at least one ms)
   * @param arg1 context from which to derive the calling interval
   * @param arg2 context from which to derive the callee interval
   * @param join true iff join should be performed, false iff merge is desired
   * @return {@code {from, to}} representing the interval
   */
  private TimePoint[] buildIntervalFromComplex(GDLParser.ComplexIntervalArgumentContext arg1,
                                               GDLParser.ComplexIntervalArgumentContext arg2,
                                               boolean join){
    TimePoint[] i1 = null;
    TimePoint[] i2 = null;
    if(arg1.intervalFromStamps()!=null){
      i1 = buildIntervalFromStamps(arg1.intervalFromStamps());
    }
    else{
      i1 = buildIntervalFromSelector(arg1.intervalSelector());
    }
    if(arg2.intervalFromStamps()!=null){
      i2 = buildIntervalFromStamps(arg2.intervalFromStamps());
    }
    else{
      i2 = buildIntervalFromSelector(arg2.intervalSelector());
    }
    // constraint: merge and join only when overlapping or meeting
    Comparison constraint = new Comparison(
            new MaxTimePoint(i1[0], i2[0]), LTE, new MinTimePoint(i1[1], i2[1])
    );
    currentPredicates.addFirst(constraint);
    // now build complex intervall from i1, i2
    if(join){
      TimePoint start = new MinTimePoint(i1[0],i2[0]);
      TimePoint end = new MaxTimePoint(i1[1],i2[1]);
      return new TimePoint[]{start, end};
    }
    // merge
    else{
      TimePoint start = new MaxTimePoint(i1[0],i2[0]);
      TimePoint end = new MinTimePoint(i1[1],i2[1]);
      return new TimePoint[]{start, end};
    }
  }

  /**
   * Builds a {@code Predicate} from the given TimeStamp-Function (caller is a timestamp)
   * time stamp functions are e.g. succeeds(x), before(x),...
   *
   * @param ctx stamp function context
   */
  @Override
  public void enterStmpF(GDLParser.StmpFContext ctx){
    currentPredicates.add(buildStampFunction(ctx));
  }

  /**
   * Converts a time stamp function into a (potentially complex) {@code Predicate}
   * For example, i.before(x) would be translated to a {@code Predicate} i<x
   *
   * @param ctx time stamp function context
   * @return (potentially complex) {@code Predicate} that encodes the time stamp function. Atoms are time stamp comparisons
   */
  private Predicate buildStampFunction(GDLParser.StmpFContext ctx) {
    TimePoint tp = buildTimePoint(ctx.timePoint());
    return createStampPredicates(tp, ctx.stampFunc());
  }

  /**
   * Returns time stamp {@code Predicate} given the caller (a time stamp) and its context
   *
   * @param tp the caller
   * @param stampFunc context including the operator (e.g. before,...) and its argument(s)
   * @return (potentially complex) {@code Predicate} that encodes the time stamp function. Atoms are time stamp comparisons
   */
  private Predicate createStampPredicates(TimePoint tp, GDLParser.StampFuncContext stampFunc) {
    if(stampFunc.beforePointOperator()!=null) {
      return createBeforePredicates(tp, stampFunc.beforePointOperator());
    }
    else if(stampFunc.afterPointOperator()!=null){
      return createAfterPredicates(tp, stampFunc.afterPointOperator());
    }
    else if(stampFunc.precedesOperator()!=null){
      return createPrecedesPredicates(tp, stampFunc.precedesOperator());
    }
    else if(stampFunc.succeedsOperator()!=null){
      return createSucceedsPredicates(tp, stampFunc.succeedsOperator());
    }
    return null;
  }


  /**
   * Creates a before {@code Predicate} given the caller (a timestamp) and its context
   *
   * @param from the caller
   * @param ctx its context including the argument
   * @return a {@code Predicate} encoding the before function: from<x
   */
  private Predicate createBeforePredicates(TimePoint from, GDLParser.BeforePointOperatorContext ctx){
    TimePoint x = buildTimePoint(ctx.timePoint());
    return new Comparison(from, Comparator.LT, x);
  }

  /**
   * Creates a after {@code Predicate} given the caller (a timestamp) and its context
   *
   * @param from the caller
   * @param ctx context including the argument
   * @return a {@code Predicate} encoding the after function: from>x
   */
  private Predicate createAfterPredicates(TimePoint from, GDLParser.AfterPointOperatorContext ctx){
    TimePoint x = buildTimePoint(ctx.timePoint());
    return new Comparison(from, Comparator.GT, x);
  }

  /**
   * Builds a {@code TimePoint} (can be {@code TimeLiteral}, {@code TimeSelector}, {@code MIN},...) given its context
   *
   * @param ctx timepoint context
   * @return the {@code TimePoint} described by the context
   */
  private TimePoint buildTimePoint(GDLParser.TimePointContext ctx) {
    if (ctx.timeLiteral() != null){
      return buildTimeLiteral(ctx.timeLiteral());
    }
    else if (ctx.timeSelector()!=null){
      return buildTimeSelector(ctx.timeSelector());
    }
    else if (ctx.complexTimePoint()!=null){
      return buildComplexTimePoint(ctx.complexTimePoint());
    }
    return null;
  }

  /**
   * Builds a TimeLiteral given a context.
   * @param ctx context containing the literal
   * @return TimeLiteral
   */
  private TimeLiteral buildTimeLiteral(GDLParser.TimeLiteralContext ctx){
    return new TimeLiteral(ctx.getText().trim());
  }

  /**
   * Builds a TimeSelector (variable.field, where field in {TX_FROM, TX_TO, VAL_FROM, VAL_TO})
   * @param ctx context containing the selector
   * @return TimeSelector
   */
  private TimeSelector buildTimeSelector(GDLParser.TimeSelectorContext ctx){
    // checks whether ID is even there (is a vertex or edge) and returns its variable
    String var = ctx.Identifier()!=null ?
            resolveIdentifier(ctx.Identifier().getText()) : TimeSelector.GLOBAL_SELECTOR;
    String field = ctx.TimeProp().getText();
    return new TimeSelector(var, field);
  }

  /**
   * Builds a "complex" time point, i.e. a time point described by a {@code MAX(...)} or
   * {@code MIN(...)} expression.
   * @param ctx context containing the time point
   * @return complex time point
   */
  private TimePoint buildComplexTimePoint(GDLParser.ComplexTimePointContext ctx){

    List<GDLParser.ComplexTimePointArgumentContext> argumentContexts =
            ctx.complexTimePointArgument();
    TimePoint[] args = new TimePoint[argumentContexts.size()];

    for(int i=0; i<argumentContexts.size(); i++){
      GDLParser.ComplexTimePointArgumentContext argumentContext = argumentContexts.get(i);
      if(argumentContext.timeLiteral()!=null){
        args[i]= buildTimeLiteral(argumentContext.timeLiteral());
      }
      else if(argumentContext.timeSelector()!=null){
        args[i]= buildTimeSelector(argumentContext.timeSelector());
      }
    }
    // available complex timepoints: min and max
    if(ctx.getText().startsWith("MAX(")){
      return new MaxTimePoint(args);
    }
    else if(ctx.getText().startsWith("MIN(")){
      return new MinTimePoint(args);
    }
    return null;
  }

  @Override
  public void enterAsOf(GDLParser.AsOfContext ctx){
      TimePoint tp = buildTimePoint(ctx.timePoint());
      String identifier = resolveIdentifier(ctx.Identifier().getText());
      currentPredicates.add(new And(
              new Comparison(
                      new TimeSelector(identifier, TimeSelector.TimeField.TX_FROM),
                      LTE,
                      tp),
              new Comparison(
                      new TimeSelector(identifier, TimeSelector.TimeField.TX_TO),
                      GTE,
                      tp)
              )
      );
  }

  /**
   * Creates the default asOf conditions, that ensure that every element's transaction time is
   * as of now. This is only done if no other constraints on transaction times' ends
   * are specified in the query.
   * @return default asOf predicates
   */
  private Predicate createDefaultAsOf(){
      Set<String> vars = new HashSet<>();
      vars.addAll(userEdgeCache.keySet());
      vars.addAll(userVertexCache.keySet());
      vars.addAll(autoEdgeCache.keySet());
      vars.addAll(autoVertexCache.keySet());
      if(vars.isEmpty()){
        return null;
      }
      else{
        TimeLiteral now = new TimeLiteral("now");
        ArrayList<String> variables = new ArrayList<>();
        variables.addAll(vars);
        Predicate asOfNow = new And(
                new Comparison(
                        new TimeSelector(variables.get(0), TimeSelector.TimeField.TX_FROM),
                        Comparator.LTE,
                        now
                ),
                new Comparison(
                        new TimeSelector(variables.get(0), TimeSelector.TimeField.TX_TO),
                        GTE,
                        now
                )
        );
        for(int i=1; i<variables.size(); i++){
            asOfNow = new And(
                    asOfNow,
                    new And(
                            new Comparison(
                                    new TimeSelector(variables.get(i), TimeSelector.TimeField.TX_FROM),
                                    Comparator.LTE,
                                    now
                            ),
                            new Comparison(
                                    new TimeSelector(variables.get(i), TimeSelector.TimeField.TX_TO),
                                    GTE,
                                    now
                            )
                    )
            );
        }
        return asOfNow;
      }
  }


  /**
   * Called when we leave an NotExpression.
   *
   * Checks if the expression is preceded by a Not and adds the filter in that case.
   * @param ctx expression context
   */
  @Override
  public void exitNotExpression(GDLParser.NotExpressionContext ctx) {
    if (!ctx.NOT().isEmpty()) {
      Predicate not = new Not(currentPredicates.removeLast());
      currentPredicates.add(not);
    }
  }

  /**
   * Called when parser leaves AndExpression
   *
   * Processes expressions connected by AND
   * @param ctx expression context
   */
  @Override
  public void exitAndExpression(GDLParser.AndExpressionContext ctx) {
    processConjunctionExpression(ctx.AND());
  }

  /**
   * Called when parser leaves OrExpression
   *
   * Processes expressions connected by OR
   * @param ctx expression context
   */
  @Override
  public void exitOrExpression(GDLParser.OrExpressionContext ctx) {
    processConjunctionExpression(ctx.OR());
  }

  /**
   * Called when parser leaves AXorExpression
   *
   * Processes expressions connected by XOR
   * @param ctx expression context
   */
  @Override
  public void exitXorExpression(GDLParser.XorExpressionContext ctx) {
    processConjunctionExpression(ctx.XOR());
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
    if (variable != null && userEdgeCache.containsKey(variable)) {
      e = userEdgeCache.get(variable);
    } else {
      e = initNewEdge(edgeBodyContext, isIncoming);

      if (variable != null) {
        userEdgeCache.put(variable, e);
      } else {
        variable = String.format(ANONYMOUS_EDGE_VARIABLE, e.getId());
        autoEdgeCache.put(variable, e);
      }
      e.setVariable(variable);
      edges.add(e);
    }
    updateGraphElement(e);
    setLastSeenEdge(e);
  }

  /**
   * Processes a conjuctive expression (AND, OR, XOR) and connects the filter with the corresponding operator
   *
   * @param conjunctions list of conjunction operators
   */
  private void processConjunctionExpression(List<TerminalNode> conjunctions) {
    Predicate conjunctionReuse;
    for (int i = conjunctions.size() - 1; i >= 0; i--) {
      Predicate rhs = currentPredicates.removeLast();
      Predicate lhs = currentPredicates.removeLast();

      switch (conjunctions.get(i).getText().toLowerCase()) {
        case "and":
          conjunctionReuse = new And(lhs, rhs);
          break;
        case "or":
          conjunctionReuse = new Or(lhs, rhs);
          break;
        default:
          conjunctionReuse = new Xor(lhs, rhs);
          break;
      }
      currentPredicates.add(conjunctionReuse);
    }
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
    List<String> labels = getLabels(graphContext.header());
    g.setLabels(labels.isEmpty() ?
      useDefaultGraphLabel ? Collections.singletonList(defaultGraphLabel) : Collections.emptyList()
      : labels);
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
    List<String> labels = getLabels(vertexContext.header());
    v.setLabels(labels.isEmpty() ?
      useDefaultVertexLabel ? Collections.singletonList(defaultVertexLabel) : Collections.emptyList()
      : labels);
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

    if (hasBody) {
      List<String> labels = getLabels(edgeBodyContext.header());
      e.setLabels(labels.isEmpty() ?
        useDefaultEdgeLabel ? Collections.singletonList(defaultEdgeLabel) : Collections.emptyList()
        : labels);
      e.setProperties(getProperties(edgeBodyContext.properties()));
      int[] range = parseEdgeLengthContext(edgeBodyContext.edgeLength());
      e.setLowerBound(range[0]);
      e.setUpperBound(range[1]);
    } else {
      if (useDefaultEdgeLabel) {
        e.setLabel(defaultEdgeLabel);
      } else {
        e.setLabel(null);
      }
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
      if(! graphElements.containsKey(getNextGraphId())){
        graphElements.put(getNextGraphId(), new ArrayList<>());
      }
      graphElements.get(getNextGraphId()).add(graphElement);
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
   * Returns the element labels from a given header context.
   *
   * @param header header context
   * @return element labels or {@code null} if context was null
   */
  private List<String> getLabels(GDLParser.HeaderContext header) {
    if (header != null && header.label() != null) {
      return header
        .label()
        .stream()
        .map(RuleContext::getText)
        .map(x -> x.substring(1))
        .collect(Collectors.toList());
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
      Map<String, Object> properties = new HashMap<>();
      for (GDLParser.PropertyContext property : propertiesContext.property()) {
        properties.put(property.Identifier().getText(), getPropertyValue(property.literal()));
      }
      return properties;
    }
    return Collections.emptyMap();
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
      return parseString(literalContext.StringLiteral().getText());
    } else if (literalContext.BooleanLiteral() != null) {
      return Boolean.parseBoolean(literalContext.BooleanLiteral().getText());
    } else if (literalContext.IntegerLiteral() != null) {
      text = literalContext.IntegerLiteral().getText().toLowerCase();
      if (text.endsWith("l")) {
        return Long.parseLong(text.substring(0, text.length() - 1));
      }
      return Integer.parseInt(text);
    } else if (literalContext.FloatingPointLiteral() != null) {
      text = literalContext.FloatingPointLiteral().getText().toLowerCase();
      if (text.endsWith("f")) {
        return Float.parseFloat(text.substring(0, text.length() - 1));
      } else if (text.endsWith("d")) {
        return Double.parseDouble(text.substring(0, text.length() - 1));
      }
      return Float.parseFloat(text);
    } else if (literalContext.NaN() != null) {
      return Double.NaN;
    }
    return null;
  }

  /**
   * Parses an {@code EdgeLengthContext} and returns the indicated Range
   *
   * @param lengthCtx the edges length context
   * @return int array representing lower and upper bound
   */
  private int[] parseEdgeLengthContext(GDLParser.EdgeLengthContext lengthCtx) {
    int lowerBound = 0;
    int upperBound = 0;

    if(lengthCtx != null) {
      int children = lengthCtx.getChildCount();

      if (children == 4) { // [*1..2]
        lowerBound = terminalNodeToInt(lengthCtx.IntegerLiteral(0));
        upperBound = terminalNodeToInt(lengthCtx.IntegerLiteral(1));
      } else if (children == 3) { // [*..2]
        upperBound = terminalNodeToInt(lengthCtx.IntegerLiteral(0));

      } else if (children == 2) { // [*1]
        lowerBound = terminalNodeToInt(lengthCtx.IntegerLiteral(0));
      } else { // [*]
        lowerBound = 0;
        upperBound = 0;
      }
    } else {
      // regular edge
      lowerBound = 1;
      upperBound = 1;
    }
    return new int[] { lowerBound, upperBound };
  }

  /**
   * Builds a Comparison filter operator from comparison context
   *
   * @param ctx the comparison context that will be parsed
   * @return parsed operator
   */
  private Comparison buildComparison(GDLParser.ComparisonExpressionContext ctx) {
    ComparableExpression lhs = extractComparableExpression(ctx.comparisonElement(0));
    ComparableExpression rhs = extractComparableExpression(ctx.comparisonElement(1));
    Comparator comp = Comparator.fromString(ctx .ComparisonOP().getText());

    return new Comparison(lhs, comp, rhs);
  }

  /**
   * Extracts a ComparableExpression from comparissonElement
   *
   * @param element comparissonElement
   * @return extracted comparable expression
   */
  private ComparableExpression extractComparableExpression(GDLParser.ComparisonElementContext element) {
    if(element.literal() != null) {
      return new Literal(getPropertyValue(element.literal()));
    } else if(element.propertyLookup() != null) {
      return buildPropertySelector(element.propertyLookup());
    }
      else {
      return new ElementSelector(element.Identifier().getText());
    }
  }

  /**
   * Builds an property selector expression like alice.age
   *
   * @param ctx the property lookup context that will be parsed
   * @return parsed property selector expression
   */
  private PropertySelector buildPropertySelector(GDLParser.PropertyLookupContext ctx) {
    String identifier = resolveIdentifier(ctx.Identifier(0).getText());
    String property = ctx.Identifier(1).getText();
    return new PropertySelector(identifier,property);
  }

  private String resolveIdentifier(String identifier){
    GraphElement element;
    if(userVertexCache.containsKey(identifier)) {
      element = userVertexCache.get(identifier);
    }
    else if(userEdgeCache.containsKey(identifier)) {
      element = userEdgeCache.get(identifier);
    }
    else { throw new InvalidReferenceException(identifier);}
    return element.getVariable();
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
   * Creates a cache containing a mapping from variables to query elements. The cache is filled
   * with elements from the user cache and/or the auto cache depending on the specified flags.
   *
   * @param userCache element user cache
   * @param autoCache element auto cache
   * @param includeUserDefined true, iff user cache elements shall be included
   * @param includeAutoGenerated true, iff auto cache elements shall be included
   * @param <T> query element type
   * @return immutable cache
   */
  private <T> Map<String, T> getCache(Map<String, T> userCache, Map<String, T> autoCache,
    boolean includeUserDefined, boolean includeAutoGenerated) {
    Map<String, T> cache = new HashMap<>();
    if (includeUserDefined) {
      cache.putAll(userCache);
    }
    if (includeAutoGenerated) {
      cache.putAll(autoCache);
    }
    return Collections.unmodifiableMap(cache);
  }

  /**
   * Adds a list of predicates to the current predicates using AND conjunctions
   *
   * @param newPredicates predicates to be added
   */
  private void addPredicates(List<Predicate> newPredicates) {
    for(Predicate newPredicate : newPredicates) {
      if(this.predicates == null) {
        this.predicates = newPredicate;
      } else {
        this.predicates = new And(this.predicates, newPredicate);
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

  /**
   * Parses a String literal from the input string. Unescapes " and '
   *
   * @param in the raw input string
   * @return the parsed string
   */
  private String parseString(String in) {
    return in.replaceAll("^.|.$", "")
             .replaceAll("\\\\\"","\"")
             .replaceAll("\\\\\'","'");
  }

}
