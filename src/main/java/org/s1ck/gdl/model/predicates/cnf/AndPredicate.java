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

package org.s1ck.gdl.model.predicates.cnf;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection conjunct OrPredicates
 * This can be used to represent a CNF
 */
public class AndPredicate extends PredicateCollection<OrPredicate>{

  public AndPredicate() {
    this.predicates = new ArrayList<>();
  }

  public AndPredicate(List<OrPredicate> predicates) {
    this.predicates = predicates;
  }

  /**
   * Connect another cnf predicate via AND
   * @param other a predicate in cnf
   * @return this
   */
  public AndPredicate and(AndPredicate other) {
    addPredicates(other.getPredicates());
    return this;
  }

  /**
   * Connect another predicate in cnf via OR
   *
   * @param other a predicate in cnf
   * @return this
   */
  public AndPredicate or(AndPredicate other) {
    ArrayList<OrPredicate> newPredicates = new ArrayList<>();
    for (OrPredicate p : predicates) {
      for (OrPredicate q : other.getPredicates()) {
        OrPredicate newOrPredicate = new OrPredicate();
        newOrPredicate.addPredicates(p.getPredicates());
        newOrPredicate.addPredicates(q.getPredicates());

        newPredicates.add(newOrPredicate);
      }
    }
    predicates = newPredicates;

    return this;
  }

  public String operatorName() { return "AND"; }
}
