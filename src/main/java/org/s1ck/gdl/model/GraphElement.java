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
