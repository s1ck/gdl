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

package org.s1ck.gdl.model;

public class Edge extends GraphElement {
  private Long sourceVertexId;

  private Long targetVertexId;

  private int lowerBound;

  private int upperBound;

  public Edge() {
    super();
    lowerBound = 1;
    upperBound = 1;
  }

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

  public boolean hasVariableLength() {
    return !(upperBound == lowerBound && upperBound == 1);
  }

  public int getLowerBound() {
    return lowerBound;
  }

  public void setLowerBound(int lowerBound) {
    this.lowerBound = lowerBound;
  }

  public int getUpperBound() {
    return upperBound;
  }

  public void setUpperBound(int upperBound) {
    this.upperBound = upperBound;
  }

  @Override
  public String toString() {
    String out = "Edge{" +
      "id=" + getId() +
      ", label='" + getLabel() + '\'' +
      ", properties=" + getProperties() +
      ", sourceVertexId=" + sourceVertexId +
      ", targetVertexId=" + targetVertexId;

    if(hasVariableLength()) {
      out = out +
        ", lowerBound=" + getLowerBound() +
        ", upperBound=" + getUpperBound();
    }

    out = out + ", graphs=" + getGraphs() + '}';

    return out;
  }
}
