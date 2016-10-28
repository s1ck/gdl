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

package org.s1ck.gdl.model.predicates;

import org.s1ck.gdl.model.predicates.cnf.AndPredicate;
import org.s1ck.gdl.model.predicates.cnf.OrPredicate;

public class Not implements Predicate {

  private Predicate expression;

  public Not(Predicate expression) {
    this.expression = expression;
  }

  public Predicate[] getArguments() {
    Predicate[] arguments = {expression};
    return arguments;
  }

  public AndPredicate toCNF() {
    if(expression.getClass() == Comparison.class) {
      AndPredicate andPredicate = new AndPredicate();
      OrPredicate orPredicate = new OrPredicate();
      orPredicate.addPredicate(this);
      andPredicate.addPredicate(orPredicate);
      return andPredicate;
    } else if (expression.getClass() == Not.class) {
      return expression.getArguments()[0].toCNF();
    } else if (expression.getClass() == And.class) {
      Predicate[] otherArguments = expression.getArguments();
      return new Or(new Not(otherArguments[0]),new Not(otherArguments[0])).toCNF();
    } else if (expression.getClass() == Or.class) {
      Predicate[] otherArguments = expression.getArguments();
      return new And(new Not(otherArguments[0]),new Not(otherArguments[0])).toCNF();
    } else {
      Predicate[] otherArguments = expression.getArguments();
      return new Or(new And(otherArguments[0],otherArguments[1]),new And(new Not(otherArguments[0]),new Not(otherArguments[0]))).toCNF();
    }
  }
  
  public String toString() {
    return "( NOT " + expression + " )";
  }
}
