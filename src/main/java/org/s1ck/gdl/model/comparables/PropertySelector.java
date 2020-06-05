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
 * Selects a property of a variable
 */
public class PropertySelector implements ComparableExpression {

  /**
   * Elements variable
   */
  private final String variable;

  /**
   * Elements property name
   */
  private final String propertyName;

  public PropertySelector(String variable, String propertyName) {
    this.variable = variable;
    this.propertyName = propertyName;
  }

  /**
   * Returns the property name
   *
   * @return the property name
   */
  public String getPropertyName() {
    return propertyName;
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
  public boolean containsSelectorType(TimeSelector.TimeField type){
    return false;
  }

  @Override
  public boolean isGlobal(){
    return false;
  }

  @Override
  public ComparableExpression replaceGlobalByLocal(List<String> variables) {
    return this;
  }

  @Override
  public String toString() {
    return variable + "." + propertyName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PropertySelector that = (PropertySelector) o;

    if (variable != null ? !variable.equals(that.variable) : that.variable != null) return false;
    return propertyName != null ? propertyName.equals(that.propertyName) : that.propertyName == null;

  }

  @Override
  public int hashCode() {
    int result = variable != null ? variable.hashCode() : 0;
    result = 31 * result + (propertyName != null ? propertyName.hashCode() : 0);
    return result;
  }
}
