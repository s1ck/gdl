package org.s1ck.gdl.model.operators;

/**
 * Created by max on 25.10.16.
 */
public class Not extends Filter{

  private Filter expression;

  public Not(Filter expression) {
    this.expression = expression;
  }

  public String toString() {
    return "( NOT " + expression + " )";
  }
}
