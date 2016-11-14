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

package org.s1ck.gdl.utils;

/**
 * Holds the comparators for comparable expressions
 */
public enum Comparator {
  /**
   * Equality
   */
  EQ,
  /**
   * Inequality
   */
  NEQ,
  /**
   * Greater than
   */
  GT,
  /**
   * Lower than
   */
  LT,
  /**
   * Greater than or equal
   */
  GTE,
  /**
   * Lower than or equal
   */
  LTE;

  /**
   * Create a comparator from a string
   * @param str the string representation
   * @return the equivalent comparator
   */
  public static Comparator fromString(String str) {
    switch (str) {
      case "=":   return EQ;
      case "!=":
      case "<>":  return NEQ;
      case ">":   return GT;
      case "<":   return LT;
      case ">=":  return GTE;
      case "<=":  return LTE;
      default:    return null;
    }
  }

  /**
   * Inverts the operator
   * @return the inverted operator
   */
  public Comparator getInverse() {
    switch (this) {
      case NEQ:   return EQ;
      case EQ:  return NEQ;
      case LT:   return GT;
      case GT:   return LT;
      case LTE:  return GTE;
      case GTE:  return LTE;
      default:    return null;
    }
  }

  /**
   * Returns the string representation
   * @return the string representation
   */
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