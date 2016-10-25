package org.s1ck.gdl.model.operators;

/**
 * Created by max on 25.10.16.
 */
public class Or extends Filter{

  private Filter lhs;
  private Filter rhs;

  public Or(Filter lhs, Filter rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  public String toString() {
    return "(" + lhs + " OR " + rhs + ")";
  }
}
