package org.s1ck.gdl.predicates;

import org.junit.Test;
import org.s1ck.gdl.model.predicates.Or;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.cnf.AndPredicate;
import static org.junit.Assert.assertEquals;

public class OrTest extends PredicateTest {

  @Test
  public void convertToCnfTest() {
    Predicate a = getComparison();
    Predicate b = getComparison();

    Or or = new Or(a,b);

    AndPredicate reference = a.toCNF().or(b.toCNF());

    assertEquals(reference,or.toCNF());
  }

}
