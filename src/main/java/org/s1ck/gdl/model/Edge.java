package org.s1ck.gdl.model;

public class Edge extends GraphElement {
  private Long sourceVertexId;

  private Long targetVertexId;

  public Long getSourceVertexId() {
    return sourceVertexId;
  }

  public void setSourceVertexId(Long sourceVertexId) {
    this.sourceVertexId = sourceVertexId;
  }

  public Long getTargetVertexId() {
    return targetVertexId;
  }

  public void setTargetVertexId(Long targetVertexId) {
    this.targetVertexId = targetVertexId;
  }

  @Override
  public String toString() {
    return "Edge{" +
      "id=" + getId() +
      ", label='" + getLabel() + '\'' +
      ", properties=" + getProperties() +
      ", sourceVertexId=" + sourceVertexId +
      ", targetVertexId=" + targetVertexId +
      ", graphs=" + getGraphs() +
      '}';
  }
}
