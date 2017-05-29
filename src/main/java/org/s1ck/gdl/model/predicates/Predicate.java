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

package org.s1ck.gdl.model.predicates;

import org.s1ck.gdl.model.GraphElement;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.model.comparables.Literal;
import org.s1ck.gdl.model.comparables.PropertySelector;
import org.s1ck.gdl.utils.Comparator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a predicate defined on a query vertex or edge.
 */
public interface Predicate extends Serializable {

  /**
   * Builds predicates from label and property definitions embedded at an
   * {@link GraphElement}.
   *
   * @param element the element to extract from
   * @return extracted predicates
   */
  static List<Predicate> fromGraphElement(GraphElement element, String defaultLabel) {
    ArrayList<Predicate> predicates = new ArrayList<>();

    Predicate predicate;

    if(element.getLabel() != null && !element.getLabel().equals(defaultLabel)) {
      predicate = new Comparison(
        new PropertySelector(element.getVariable(),"__label__"),
        Comparator.EQ,
        new Literal(element.getLabel()));

      predicates.add(predicate);
    }

    if(element.getProperties() != null) {
      for (Map.Entry<String, Object> entry : element.getProperties().entrySet()) {
        predicate = new Comparison(
                new PropertySelector(element.getVariable(), entry.getKey()),
                Comparator.EQ,
                new Literal(entry.getValue())
        );

        predicates.add(predicate);
      }
    }
    return predicates;
  }

  /**
   * Returns the predicates arguments
   *
   * @return The predicates arguments
   */
  Predicate[] getArguments();

  /**
   * Returns the variables which are referenced by the predicate
   *
   * @return referenced variables
   */
  Set<String> getVariables();
}
