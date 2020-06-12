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

public class Not implements Predicate {

  private final Predicate expression;

  public Not(Predicate expression) {
    this.expression = expression;
  }

  @Override
  public Predicate[] getArguments() {
      return new Predicate[]{expression};
  }

  /**
   * Returns a set of variables referenced by the predicates
   * @return set of variables
   */
  @Override
  public Set<String> getVariables() {
    return expression.getVariables();
  }

  @Override
  public Predicate switchSides(){
    return new Not(expression.switchSides());
  }

  @Override
  public boolean containsSelectorType(TimeSelector.TimeField type){
    return expression.containsSelectorType(type);
  }

  @Override
  public boolean isTemporal(){
    return expression.isTemporal();
  }

  @Override
  public Predicate unfoldGlobalLeft(List<String> variables) {
    return new Not(expression.unfoldGlobalLeft(variables));
  }

  @Override
  public String toString() {
    return String.format("(NOT %s)", expression);
  }

  @Override
  public boolean isGlobal(){
    return expression.isGlobal();
  }

  @Override
  public Predicate replaceGlobalByLocal(List<String> variables) {
    return new Not(expression.replaceGlobalByLocal(variables));
  }

  @Override
  public boolean equals(Object o){
    if(o==null){
      return false;
    }
    if(!this.getClass().equals(o.getClass())){
      return false;
    }
    Not that = (Not)o;
    return that.expression.equals(this.expression);
  }
}
