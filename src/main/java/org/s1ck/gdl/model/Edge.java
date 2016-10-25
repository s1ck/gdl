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

package org.s1ck.gdl.model;

public class Edge extends GraphElement {
  private Long sourceVertexId;

  private Long targetVertexId;

  private boolean hasVariableLength;

  private int lowerBound;

  private int upperBound;

  public Edge() {
    super();
    this.lowerBound = 1;
    this.upperBound = 1000;
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

  public void setHasVariableLength(boolean val) {
    this.hasVariableLength = val;
  }

  public boolean gethasVariableLength() {
    return this.hasVariableLength;
  }

  public int getLowerBound() { return lowerBound; }

  public void setLowerBound(int lowerBound) {
    this.lowerBound = lowerBound;
  }

  public int getUpperBound() { return upperBound; }

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
      ", targetVertexId=" + targetVertexId +
      ", hasVariableLength=" + hasVariableLength;

    if(hasVariableLength) {
      out = out +
        ", lowerBound=" + lowerBound +
        ", upperBound=" + upperBound;
    }

    out = out + ", graphs=" + getGraphs() +
      '}';

      return out;
  }
}
