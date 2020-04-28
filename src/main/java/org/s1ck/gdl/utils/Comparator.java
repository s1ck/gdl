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
      case LT:   return GTE;
      case GT:   return LTE;
      case LTE:  return GT;
      case GTE:  return LT;
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