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

package org.s1ck.gdl.model.predicates.expressions;

import org.s1ck.gdl.model.comparables.time.TimeAtom;
import org.s1ck.gdl.model.comparables.time.TimePoint;
import org.s1ck.gdl.model.comparables.time.TimeSelector;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.comparables.ComparableExpression;
import org.s1ck.gdl.utils.Comparator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a comparison of two values
 */
public class Comparison implements Predicate {
  /**
   * Left hand side value
   */
  private final ComparableExpression lhs;
  /**
   * Right hand side value
   */
  private final ComparableExpression rhs;
  /**
   * The comparator used to compare a the values
   */
  private final Comparator comparator;

  /**
   * Creates a new comparison operator
   * @param lhs left hand side value
   * @param comparator comparator
   * @param rhs right hand side value
   */
  public Comparison(ComparableExpression lhs, Comparator comparator, ComparableExpression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
    this.comparator = comparator;
  }

  @Override
  public Predicate[] getArguments() {
      return new Predicate[]{};
  }

  /**
   * Returns the comparator
   * @return the comparator
   */
  public Comparator getComparator() {
    return comparator;
  }

  /**
   * Returns the left and right hand side values
   * @return lhs and rhs values
   */
  public ComparableExpression[] getComparableExpressions() {
      return new ComparableExpression[]{lhs, rhs};
  }

  @Override
  public boolean isTemporal(){
    //actually, time data can only be compared to time data (yet), thus it would suffices to check whether
    // lhs is a TimePoint at the moment
    return lhs instanceof TimePoint || rhs instanceof TimePoint;
  }

  @Override
  public Predicate unfoldGlobalLeft(List<String> variables) {
    // check whether there is something to do
    if(!isTemporal() || !(lhs instanceof TimeAtom)){
      return this;
    }
    return (((TimeAtom)lhs).unfoldGlobal(comparator, rhs, variables));

  }

  @Override
  public Predicate replaceGlobalByLocal(List<String> variables) {
    return new Comparison(lhs.replaceGlobalByLocal(variables),
            comparator,
            rhs.replaceGlobalByLocal(variables));
  }

  @Override
  public Comparison switchSides(){
    if(!isTemporal()){
      return this;
    }
    Comparator newComp;
    if(comparator == Comparator.EQ){
      newComp = Comparator.EQ;
    }
    else if(comparator == Comparator.NEQ){
      newComp = Comparator.NEQ;
    }
    else if(comparator == Comparator.GT){
      newComp = Comparator.LT;
    }
    else if(comparator == Comparator.GTE){
      newComp = Comparator.LTE;
    }
    else if(comparator == Comparator.LT){
      newComp = Comparator.GT;
    }
    //LTE
    else{
      newComp = Comparator.GTE;
    }
    return new Comparison(rhs, newComp, lhs);
  }

  /**
   * Returns a set of variables referenced by the predicates
   * @return set of variables
   */
  @Override
  public Set<String> getVariables() {
    Set<String> variables = new HashSet<>();
    if(lhs.getVariables() != null) variables.addAll(lhs.getVariables());
    if(rhs.getVariables() != null) variables.addAll(rhs.getVariables());

    return variables;
  }

  @Override
  public boolean containsSelectorType(TimeSelector.TimeField type){
    return lhs.containsSelectorType(type) || rhs.containsSelectorType(type);
  }

  @Override
  public boolean isGlobal(){
    return lhs.isGlobal() || rhs.isGlobal();
  }


  @Override
  public String toString() {
    return lhs + " " + comparator + " " + rhs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Comparison that = (Comparison) o;

    if (lhs != null ? !lhs.equals(that.lhs) : that.lhs != null) return false;
    if (rhs != null ? !rhs.equals(that.rhs) : that.rhs != null) return false;
    return comparator == that.comparator;

  }

  @Override
  public int hashCode() {
    int result = lhs != null ? lhs.hashCode() : 0;
    result = 31 * result + (rhs != null ? rhs.hashCode() : 0);
    result = 31 * result + (comparator != null ? comparator.hashCode() : 0);
    return result;
  }
}
