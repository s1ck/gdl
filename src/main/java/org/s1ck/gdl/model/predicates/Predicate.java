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
import org.s1ck.gdl.model.comparables.time.TimeSelector;
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

    //TODO respect multiple labels
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

  /**
   * Returns an equivalent predicate, but arguments in each comparison are switched
   * switches right-hand side with left-hand side of each comparison and changes the comparator accordingly
   * @return equivalent predicate with rhs and lhs of comparisons switched
   */
  Predicate switchSides();

  /**
   * Checks whether a certain type of selector (val_from, val_to, tx_from, tx_to) is contained
   * @param type the type of selector (val_from, val_to, tx_from, tx_to)
   * @return true iff the specified type of selector is contained
   */
  boolean containsSelectorType(TimeSelector.TimeField type);

  /**
   * Checks whether the query contains temporal elements
   * @return true iff the query contains temporal elements
   */
  boolean isTemporal();

  /**
   * Checks whether the query contains a global selector
   * @return true iff the query contains a global selector
   */
  boolean isGlobal();

  /**
   * "Translates" a predicate possibly containing global time predicates to a predicate
   * containing only local time comparisons. Only unfolds the left side(!!!)
   * @param variables the variables in the whole query
   * @return translated predicate (only left side "translated"!!!)
   */
  Predicate unfoldGlobalLeft(List<String> variables);

  /**
   * Replaces global selectors like __global.val_from by their equivalent expressions
   * over all local variables.
   * E.g., {@code __global.val_from} would be replaced by {@code MAX(v1.val_from,...,
   * vn.val_from)} for variables {@code v1,...,vn}.
   * @param variables all query variables
   * @return predicate with global selectors replaced by their local variable equivalent
   * expressions
   */
  Predicate replaceGlobalByLocal(List<String> variables);

}
