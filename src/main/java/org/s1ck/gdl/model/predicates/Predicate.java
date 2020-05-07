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
   * Unfolds a predicate, i.e. reduces complex temporal predicates to simple comparisons
   * This is needed for predicates containing complex temporal elements like e.g. MIN, MAX.
   * These elements can be reduced to simple comparisons, thus yielding a predicate only
   * consisting of comparisons between literals.
   * e.g. [(MAX(a,b)> t1) AND (t2<MIN(c,d))] is equal to [((a>t1) OR (b>t1)) AND ((t2<c) AND (t2<d))]
   * @param p predicate to unfold
   * @return unfolded predicate
   */
  static Predicate unfoldTemporalComparisons(Predicate p){
    Predicate unfolded_old = null;
    Predicate unfolded_new = p.unfoldTemporalComparisonsLeft();
    // unfold LHSs until there are only atoms on left hand sides anymore
    while(!unfolded_new.equals(unfolded_old)){
      unfolded_old = unfolded_new;
      unfolded_new = unfolded_new.unfoldTemporalComparisonsLeft();
    }
    // now unfold RHSs until there are only atoms left on right hand sides anymore
    unfolded_new = unfolded_new.switchSides();
    while(!unfolded_new.equals(unfolded_old)){
      unfolded_old = unfolded_new;
      unfolded_new = unfolded_new.unfoldTemporalComparisonsLeft();
    }
    return unfolded_new;
  }

  /**
   * "Translates" a predicate possibly containing global time predicates to a predicate
   * containing only local time comparisons.
   * E.g., for a pattern {@code (a)-->(b)}, the global predicate {@code tx_to >= 2020-04-28)} would be
   * reduced to {@code a.tx_to>=2020-04-28 AND b.tx_to>=2020-04-28}
   * while {@code tx_to <= 2020-04-28)} is reduced to
   * {@code @code a.tx_to<=2020-04-28 OR b.tx_to<=2020-04-28}
   * @param predicate the predicate whose global time predicates should be translated
   * @param unfoldedComparisons indicates whether complex time predicates were already reduced to
   *                            simple comparisons. If so, this flag should be set to {@code true}
   *                            to avoid unnecessary work.
   * @return equivalent predicate consisting only of primitive local time value comparisons
   */
  static Predicate translateGlobalPredicates(Predicate predicate, ArrayList<String> vars, boolean unfoldedComparisons){
    if(!unfoldedComparisons){
      predicate = Predicate.unfoldTemporalComparisons(predicate);
    }
    predicate = predicate.unfoldGlobalLeft(vars);
    predicate = predicate.switchSides();
    return predicate.unfoldGlobalLeft(vars);
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
   * Unfolds a predicate, but only the left hand side of each temporal comparison.
   * e.g. [(MAX(a,b)> t1) AND (t2<MIN(c,d))] would yield [((a>t1) OR (b>t1)) AND ((t2<MIN(c,d))]
   * @return unfolded (only LHSs) expression
   */
  Predicate unfoldTemporalComparisonsLeft();

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
