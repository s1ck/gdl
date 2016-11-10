package org.s1ck.gdl.predicates.booleans;

import org.junit.Test;
import org.s1ck.gdl.model.comparables.ElementSelector;
import org.s1ck.gdl.model.comparables.Literal;
import org.s1ck.gdl.model.comparables.PropertySelector;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.model.cnf.CNF;
import org.s1ck.gdl.predicates.PredicateTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class AndTest extends PredicateTest {

  @Test
  public void convertToCnfTest() {
    Comparison a = getComparison();
    Comparison b = getComparison();

    And and = new And(a,b);
    CNF reference = a.toCNF().and(b.toCNF());

    assertEquals(reference,and.toCNF());
  }

  @Test
  public void extractVariablesTest() {
    Comparison a = new Comparison(
            new ElementSelector("a"),
            Comparison.Comparator.EQ,
            new ElementSelector("b")
    );

    Comparison b = new Comparison(
            new PropertySelector("a","label"),
            Comparison.Comparator.EQ,
            new Literal("Person")
    );

    And and = new And(a,b);

    Set<String> reference = new HashSet<>();
    reference.add("a");
    reference.add("b");

    assertEquals(reference,and.getVariables());
  }
}
