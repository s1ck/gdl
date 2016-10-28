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

package org.s1ck.gdl.model.predicates;

import org.s1ck.gdl.model.GraphElement;
import org.s1ck.gdl.model.predicates.cnf.AndPredicate;
import org.s1ck.gdl.model.predicates.comparables.Literal;
import org.s1ck.gdl.model.predicates.comparables.PropertySelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a predicate defined on a query vertex or edge.
 */
public interface Predicate {

  /**
   * Builds predicates from label and property definitions embedded at an
   * {@link GraphElement}.
   *
   * @param element the element to extract from
   * @return extracted predicates
   */
  static List<Predicate> fromGraphElement(GraphElement element) {
    ArrayList<Predicate> predicates = new ArrayList<>();

    Predicate predicate;

    if(element.getLabel() != null) {
      predicate = new Comparison(
        new PropertySelector(element,"label"),
        Comparison.Comparator.EQ,
        new Literal(element.getLabel()));

      predicates.add(predicate);
    }

    if(element.getProperties() != null) {
      for (Map.Entry<String, Object> entry : element.getProperties().entrySet()) {
        predicate = new Comparison(
                new PropertySelector(element, entry.getKey()),
                Comparison.Comparator.EQ,
                new Literal(entry.getValue())
        );

        predicates.add(predicate);
      }
    }
    return predicates;
  }

  public AndPredicate toCNF();

  public Predicate[] getArguments();
}
