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

package org.s1ck.gdl.model.operators;

import org.s1ck.gdl.model.operators.comparables.ComparableExpression;

public class Comparison extends Filter {

  public enum Comparator {
    EQ, NEQ, GT, LT, GTE, LTE;

    public static Comparator fromString(String str) {
      switch (str) {
        case "=":   return EQ;
        case "!=":  return NEQ;
        case ">":   return GT;
        case "<":   return LT;
        case ">=":  return GTE;
        case "<=":  return LTE;
        default:    return null;
      }
    }

    public String toString() {
      switch (this) {
        case EQ:   return "=";
        case NEQ:  return "!=";
        case GT:   return ">";
        case LT:   return "<";
        case GTE:  return ">=";
        case LTE:  return "<=";
        default:   return null;
      }
    }
  }

  private ComparableExpression lhs;
  private ComparableExpression rhs;
  private Comparator comparator;

  public Comparison(ComparableExpression lhs, Comparator comparator, ComparableExpression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
    this.comparator = comparator;
  }

  public String toString() {
    return lhs + " " + comparator + " " + rhs;
  }
}
