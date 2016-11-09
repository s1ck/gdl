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
import org.s1ck.gdl.model.cnf.CNF;

import java.util.Set;

public class Xor implements Predicate {

  // left hand side
  private Predicate lhs;

  // right hand side
  private Predicate rhs;

  public Xor(Predicate lhs, Predicate rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public Predicate[] getArguments() {
    Predicate[] arguments = {lhs,rhs};
    return arguments;
  }

  @Override
  public CNF toCNF() {
    return new Or(new And(lhs,new Not(rhs)),new And(new Not(lhs),rhs)).toCNF();
  }

  @Override
  public Set<String> variables() {
    Set<String> variables = lhs.variables();
    variables.addAll(rhs.variables());

    return variables;
  }

  @Override
  public String toString() {
    return "(" + lhs + " XOR " + rhs + ")";
  }
}
