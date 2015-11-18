package org.s1ck.gdl.model;

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
