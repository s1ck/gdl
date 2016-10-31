package org.s1ck.gdl.predicates.booleans;

import org.junit.Test;
import org.s1ck.gdl.model.predicates.*;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.booleans.Not;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.booleans.Xor;
import org.s1ck.gdl.model.cnf.CNF;
import org.s1ck.gdl.predicates.PredicateTest;

import static org.junit.Assert.assertEquals;

public class XorTest extends PredicateTest {
  @Test
  public void convertToCnfTest() {
    Predicate a = getComparison();
    Predicate b = getComparison();

    Xor xor = new Xor(a,b);

    CNF reference = new Or(new And(a,new Not(b)),new And(new Not(a),b)).toCNF();

    assertEquals(reference.toString(),xor.toCNF().toString());
  }
}
