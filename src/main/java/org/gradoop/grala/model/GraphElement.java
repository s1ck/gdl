package org.gradoop.grala.model;

import com.google.common.collect.Sets;

import java.util.Set;

public class GraphElement extends Element {
  private Set<Long> graphs;

  public GraphElement() {
    graphs = Sets.newHashSet();
  }

  public void addToGraph(Long graphId) {
    graphs.add(graphId);
  }

  public Set<Long> getGraphs() {
    return graphs;
  }
}
