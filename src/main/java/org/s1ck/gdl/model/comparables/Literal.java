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
 * Represents a literal like String, Integer, ...
 */
public class Literal implements ComparableExpression {

  /**
   * literal value
   */
  private final Object value;

  /**
   * Creates a new Literal
   *
   * @param value literal value
   */
  public Literal(Object value) {
    this.value = value;
  }

  public Object getValue() {
    return value;
  }


  @Override
  public Set<String> getVariables() {
    return new HashSet<>();
  }

  @Override
  public ComparableExpression replaceGlobalByLocal(List<String> variables) {
    return this;
  }

  @Override
  public String getVariable() {
    return null;
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
    return value.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Literal literal = (Literal) o;

    return value != null ? value.equals(literal.value) : literal.value == null;

  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }
}
