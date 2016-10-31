package org.s1ck.gdl.predicates.booleans;

import org.junit.Test;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.cnf.CNF;
import org.s1ck.gdl.predicates.PredicateTest;

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

}
