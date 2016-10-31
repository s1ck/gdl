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

package org.s1ck.gdl.model.cnf;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection conjunct OrPredicates
 * This can be used to represent a CNF
 */
public class CNF extends PredicateCollection<CNFElement>{

  public CNF() {
    this.predicates = new ArrayList<>();
  }

  public CNF(List<CNFElement> predicates) {
    this.predicates = predicates;
  }

  /**
   * Connect another cnf predicate via AND
   * @param other a predicate in cnf
   * @return this
   */
  public CNF and(CNF other) {
    addPredicates(other.getPredicates());
    return this;
  }

  /**
   * Connect another predicate in cnf via OR
   *
   * @param other a predicate in cnf
   * @return this
   */
  public CNF or(CNF other) {
    ArrayList<CNFElement> newPredicates = new ArrayList<>();
    for (CNFElement p : predicates) {
      for (CNFElement q : other.getPredicates()) {
        CNFElement newCNFElement = new CNFElement();
        newCNFElement.addPredicates(p.getPredicates());
        newCNFElement.addPredicates(q.getPredicates());

        newPredicates.add(newCNFElement);
      }
    }
    predicates = newPredicates;

    return this;
  }

  public String operatorName() { return "AND"; }
}
