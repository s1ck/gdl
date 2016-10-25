package org.s1ck.gdl.model.operators;

import org.s1ck.gdl.model.operators.comparables.Comparable;
/**
 * Created by max on 25.10.16.
 */
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

      default:    return null;

      }
    }
  }

  private Comparator comparator;
  private Comparable lhs;
  private Comparable rhs;

  public Comparison(Comparable lhs, Comparator comparator, Comparable rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
    this.comparator = comparator;
  }

  public String toString() {
    return lhs + " " + comparator + " " + rhs;
  }
}
