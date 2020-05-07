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
import org.s1ck.gdl.model.predicates.Predicate;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface ComparableExpression extends Serializable{
  /**
   * Returns the variables of the expression
   * @return variable
   */
  public Set<String> getVariables();

  /**
   * Returns the variable of the expression
   * Same functionality is given by {@code getVariables}, kept for downward compatibility
   * @return variable
   */
  public String getVariable();

  /**
   * Checks whether a certain type of selector (val_from, val_to, tx_from, tx_to) is contained
   * @param type the type of selector (val_from, val_to, tx_from, tx_to)
   * @return true iff the specified type of selector is contained
   */
  boolean containsSelectorType(TimeSelector.TimeField type);

  /**
   * Checks whether a global time selector is contained.
   * @return true iff a global time selector is contained
   */
  boolean isGlobal();

  /**
   * Replaces global time selectors like __global.val_from by their equivalent expressions
   * over all local variables.
   * E.g., {@code __global.val_from} would be replaced by {@code MAX(v1.val_from,...,
   * vn.val_from)} for variables {@code v1,...,vn}.
   * @param variables all query variables
   * @return comparable with global selector replaced by their local variable equivalent
   * expressions. If comparable is not a global time selector, identity function.
   */
  ComparableExpression replaceGlobalByLocal(List<String> variables);

}
