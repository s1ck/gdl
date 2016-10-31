package org.s1ck.gdl.predicates.booleans;

import org.junit.Test;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.model.cnf.CNF;
import org.s1ck.gdl.predicates.PredicateTest;

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
}
