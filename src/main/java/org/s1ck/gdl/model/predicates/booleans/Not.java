/*
 * This file is part of GDL.
 *
 * GDL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GDL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GDL.  If not, see <http://www.gnu.org/licenses/>.
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
    return "( NOT " + expression + " )";
  }
}
