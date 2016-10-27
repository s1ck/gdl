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

import com.google.common.collect.Range;

public class Edge extends GraphElement {
  private Long sourceVertexId;

  private Long targetVertexId;

  /**
   * Stores the length of the edge.
   * This is used for expressions with variable path lengths.
   */
  private Range<Integer> range = Range.closed(1,1);

  public Edge() {
    super();
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

  public Range<Integer> getRange() { return this.range; }

  public void setRange(Range<Integer> range) {
    this.range = range;
  }

  public boolean hasVariableLength() {
    return !(range.equals(Range.closed(1,1)));
  }

  public int getLowerBound() { return range.lowerEndpoint(); }

  public int getUpperBound() { return range.upperEndpoint(); }

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
