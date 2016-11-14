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

package org.s1ck.gdl.model.predicates.expressions;

import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.comparables.ComparableExpression;
import org.s1ck.gdl.utils.Comparator;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a comparison of two values
 */
public class Comparison implements Predicate {
  /**
   * Left hand side value
   */
  private ComparableExpression lhs;
  /**
   * Right hand side value
   */
  private ComparableExpression rhs;
  /**
   * The comparator used to compare a the values
   */
  private Comparator comparator;

  /**
   * Creates a new comparison operator
   * @param lhs left hand side value
   * @param comparator comparator
   * @param rhs right hand side value
   */
  public Comparison(ComparableExpression lhs, Comparator comparator, ComparableExpression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
    this.comparator = comparator;
  }

  @Override
  public Predicate[] getArguments() {
    Predicate[] arguments = {};
    return arguments;
  }

  /**
   * Returns the comparator
   * @return the comparator
   */
  public Comparator getComparator() {
    return comparator;
  }

  /**
   * Returns the left and right hand side values
   * @return lhs and rhs values
   */
  public ComparableExpression[] getComparableExpressions() {
    ComparableExpression[] list = {lhs, rhs};
    return list;
  }

  /**
   * Returns a set of variables referenced by the predicates
   * @return set of variables
   */
  @Override
  public Set<String> getVariables() {
    Set<String> variables = new HashSet<>();
    if(lhs.getVariable() != null) variables.add(lhs.getVariable());
    if(rhs.getVariable() != null) variables.add(rhs.getVariable());

    return variables;
  }

  @Override
  public String toString() {
    return lhs + " " + comparator + " " + rhs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Comparison that = (Comparison) o;

    if (lhs != null ? !lhs.equals(that.lhs) : that.lhs != null) return false;
    if (rhs != null ? !rhs.equals(that.rhs) : that.rhs != null) return false;
    return comparator == that.comparator;

  }

  @Override
  public int hashCode() {
    int result = lhs != null ? lhs.hashCode() : 0;
    result = 31 * result + (rhs != null ? rhs.hashCode() : 0);
    result = 31 * result + (comparator != null ? comparator.hashCode() : 0);
    return result;
  }
}
