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

package org.s1ck.gdl.model.operators;

import org.s1ck.gdl.model.GraphElement;
import org.s1ck.gdl.model.operators.comparables.Literal;
import org.s1ck.gdl.model.operators.comparables.PropertySelector;

import java.util.ArrayList;
import java.util.Map;

public abstract class Filter {

  /**
   * This builds a filter from a GraphElement, extracting parameters and labels into filters
   * @param element the element to extract from
   * @return list of extracted filters
   */
  public static ArrayList<Filter> fromGraphElement(GraphElement element) {
    ArrayList<Filter> filters = new ArrayList<>();

    Filter filter;

    if(element.getLabel() != null) {
      filter = new Comparison(new PropertySelector(element,"label"), Comparison.Comparator.EQ, new Literal(element.getLabel()));
      filters.add(filter);
    }

    if(element.getProperties() != null) {
      for (Map.Entry<String, Object> entry : element.getProperties().entrySet()) {
        filter = new Comparison(
                new PropertySelector(element, entry.getKey()),
                Comparison.Comparator.EQ,
                new Literal(entry.getValue())
        );

        filters.add(filter);
      }
    }
    return filters;
  }
}
