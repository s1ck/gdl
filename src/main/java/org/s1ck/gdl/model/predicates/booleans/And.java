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

package org.s1ck.gdl.model.predicates.booleans;

import org.s1ck.gdl.model.comparables.time.TimeSelector;
import org.s1ck.gdl.model.predicates.Predicate;

import java.util.List;
import java.util.Set;

public class And implements Predicate {

  // left hand side
  private final Predicate lhs;

  // right hand side
  private final Predicate rhs;

  public And(Predicate lhs, Predicate rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public Predicate[] getArguments() {
    return new Predicate[] { lhs, rhs };
  }

  /**
   * Returns a set of variables referenced by the predicates
   * @return set of variables
   */
  @Override
  public Set<String> getVariables() {
    Set<String> variables = lhs.getVariables();
    variables.addAll(rhs.getVariables());

    return variables;
  }

  @Override
  public Predicate switchSides(){
    return new And(lhs.switchSides(), rhs.switchSides());
  }

  @Override
  public boolean containsSelectorType(TimeSelector.TimeField type){
    return lhs.containsSelectorType(type) || rhs.containsSelectorType(type);
  }

  @Override
  public boolean isTemporal(){
    return lhs.isTemporal() || rhs.isTemporal();
  }

  @Override
  public Predicate unfoldGlobalLeft(List<String> variables) {
    return new And(lhs.unfoldGlobalLeft(variables), rhs.unfoldGlobalLeft(variables));
  }

  @Override
  public boolean isGlobal(){
    return lhs.isGlobal() || rhs.isGlobal();
  }

  @Override
  public Predicate replaceGlobalByLocal(List<String> variables) {
    return new And(lhs.replaceGlobalByLocal(variables),
            rhs.replaceGlobalByLocal(variables));
  }

  @Override
  public String toString() {
    return String.format("(%s AND %s)", lhs, rhs);
  }

  @Override
  public boolean equals(Object o){
    if(o==null){
      return false;
    }
    if(!this.getClass().equals(o.getClass())){
      return false;
    }
    And that = (And)o;
    return (that.lhs.equals(this.lhs) && that.rhs.equals(this.rhs));
  }

}
