package org.s1ck.gdl.predicates;

import org.junit.Test;
import org.s1ck.gdl.model.predicates.*;
import org.s1ck.gdl.model.predicates.cnf.AndPredicate;

import static org.junit.Assert.assertEquals;

public class XorTest extends PredicateTest {
  @Test
  public void convertToCnfTest() {
    Predicate a = getComparison();
    Predicate b = getComparison();

    Xor xor = new Xor(a,b);

    AndPredicate reference = new Or(new And(a,new Not(b)),new And(new Not(a),b)).toCNF();

    assertEquals(reference.toString(),xor.toCNF().toString());
  }
}
