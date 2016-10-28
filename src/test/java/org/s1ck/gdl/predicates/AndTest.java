package org.s1ck.gdl.predicates;

import org.junit.Test;
import org.s1ck.gdl.model.predicates.And;
import org.s1ck.gdl.model.predicates.Comparison;
import org.s1ck.gdl.model.predicates.cnf.AndPredicate;
import static org.junit.Assert.assertEquals;

public class AndTest extends PredicateTest {

  @Test
  public void convertToCnfTest() {
    Comparison a = getComparison();
    Comparison b = getComparison();

    And and = new And(a,b);
    AndPredicate reference = a.toCNF().and(b.toCNF());

    assertEquals(reference,and.toCNF());
  }
}
