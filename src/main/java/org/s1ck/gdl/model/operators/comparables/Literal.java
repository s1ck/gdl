package org.s1ck.gdl.model.operators.comparables;

/**
 * Created by max on 25.10.16.
 */
public class Literal extends Comparable {
  private Object value;

  public Literal(Object value) {
    this.value = value;
  }

  public String toString() {
    return value.toString();
  }
}
