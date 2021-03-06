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

import org.s1ck.gdl.model.predicates.Predicate;

import java.util.Set;

public class Not implements Predicate {

  private Predicate expression;

  public Not(Predicate expression) {
    this.expression = expression;
  }

  @Override
  public Predicate[] getArguments() {
    Predicate[] arguments = {expression};
    return arguments;
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
  public String toString() {
    return String.format("(NOT %s)", expression);
  }
}
