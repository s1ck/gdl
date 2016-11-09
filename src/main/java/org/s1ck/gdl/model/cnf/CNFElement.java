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

import org.s1ck.gdl.model.predicates.Predicate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a collection disjunct predicates
 * This can be used to represent a CNF
 */
public class CNFElement extends PredicateCollection<Predicate>{

  public CNFElement() {
    this.predicates = new ArrayList<>();
  }

  public CNFElement(List<Predicate> predicates) {
    this.predicates = predicates;
  }

  @Override
  public Set<String> variables() {
    Set<String> variables = new HashSet<>();
    for(Predicate predicate : predicates) {
      variables.addAll(predicate.variables());
    }
    return variables;
  }

  @Override
  public String operatorName() { return "OR"; }

}
