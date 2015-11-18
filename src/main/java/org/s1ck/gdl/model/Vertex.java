package org.s1ck.gdl.model;

public class Vertex extends GraphElement {
  @Override
  public String toString() {
    return "Vertex{" +
      "id=" + getId() +
      ", label='" + getLabel() + '\'' +
      ", properties=" + getProperties() +
      ", graphs=" + getGraphs() +
      '}';
  }
}
