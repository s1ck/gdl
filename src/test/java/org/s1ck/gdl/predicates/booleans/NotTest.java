package org.s1ck.gdl.predicates.booleans;

import org.junit.Test;
import org.s1ck.gdl.model.comparables.Literal;
import org.s1ck.gdl.model.comparables.PropertySelector;
import org.s1ck.gdl.model.predicates.booleans.Not;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class NotTest {
  @Test
  public void extractVariablesTest() {
    Comparison a = new Comparison(
            new PropertySelector("a","label"),
            Comparator.EQ,
            new Literal("Person")
    );

    Not not = new Not(a);

    Set<String> reference = new HashSet<>();
    reference.add("a");

    assertEquals(reference,not.getVariables());
  }
}
