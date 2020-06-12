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

package org.s1ck.gdl.model.comparables;

import org.s1ck.gdl.model.comparables.time.TimeSelector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Used to compare elements (by id)
 */
public class ElementSelector implements ComparableExpression {

  /**
   * Variable which identifies the element
   */
  private final String variable;

  /**
   * Creates a new element selector
   *
   * @param variable the variables that represents the element
   */
  public ElementSelector(String variable) {
    this.variable = variable;
  }

  @Override
  public Set<String> getVariables() {
    HashSet<String> var = new HashSet<>();
    var.add(variable);
    return var;
  }

  @Override
  public String getVariable() {
    return this.variable;
  }

  @Override
  public ComparableExpression replaceGlobalByLocal(List<String> variables) {
    return this;
  }

  @Override
  public boolean containsSelectorType(TimeSelector.TimeField type){
    return false;
  }

  @Override
  public boolean isGlobal(){
    return false;
  }

  @Override
  public String toString() {
    return variable;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ElementSelector that = (ElementSelector) o;

    return variable != null ? variable.equals(that.variable) : that.variable == null;
  }

  @Override
  public int hashCode() {
    return variable != null ? variable.hashCode() : 0;
  }
}
