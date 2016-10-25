package org.s1ck.gdl.model.operators;

/**
 * Created by max on 25.10.16.
 */
public class Xor extends Filter{

  private Filter lhs;
  private Filter rhs;

  public Xor(Filter lhs, Filter rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  public String toString() {
    return "(" + lhs + " XOR " + rhs + ")";
  }
}
