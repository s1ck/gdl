package org.s1ck.gdl.predicates.booleans;

import org.junit.Test;
import org.s1ck.gdl.model.comparables.ElementSelector;
import org.s1ck.gdl.model.comparables.Literal;
import org.s1ck.gdl.model.comparables.PropertySelector;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.cnf.CNF;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.predicates.PredicateTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class OrTest extends PredicateTest {

  @Test
  public void convertToCnfTest() {
    Predicate a = getComparison();
    Predicate b = getComparison();

    Or or = new Or(a,b);

    CNF reference = a.toCNF().or(b.toCNF());

    assertEquals(reference,or.toCNF());
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

    Or or = new Or(a,b);

    Set<String> reference = new HashSet<>();
    reference.add("a");
    reference.add("b");

    assertEquals(reference,or.variables());
  }
}
