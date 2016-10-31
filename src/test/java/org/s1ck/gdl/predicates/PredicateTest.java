package org.s1ck.gdl.predicates;

import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.model.comparables.Literal;


public abstract class PredicateTest {
  protected Comparison getComparison() {
    return new Comparison(
            new Literal("a.label"),
            Comparison.Comparator.EQ,
            new Literal("Person")
    );
  }
}
