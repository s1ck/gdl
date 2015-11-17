package org.gradoop.grala.model;

public class Graph extends Element {
  @Override
  public String toString() {
    return "Graph{" +
      "id=" + getId() +
      ", label='" + getLabel() + '\'' +
      ", properties=" + getProperties() +
      '}';
  }
}
