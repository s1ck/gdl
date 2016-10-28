package org.s1ck.gdl.predicates;

import org.junit.Test;
import org.s1ck.gdl.model.predicates.*;
import org.s1ck.gdl.model.predicates.cnf.AndPredicate;
import org.s1ck.gdl.model.predicates.cnf.OrPredicate;

import static org.junit.Assert.assertEquals;

public class NotTest extends PredicateTest {

  @Test
  public void convertNestedNotToCnfTest() {
    Predicate a = getComparison();
    Predicate nestedNot = new Not(a);

    Not not = new Not(nestedNot);

    AndPredicate reference = a.toCNF();

    assertEquals(reference,not.toCNF());
  }

  @Test
  public void convertNestedComparisonToCnfTest() {
    Predicate a = getComparison();
    Not not = new Not(a);

    OrPredicate orPredicate = new OrPredicate();
    orPredicate.addPredicate(not);
    AndPredicate reference = new AndPredicate();
    reference.addPredicate(orPredicate);

    assertEquals(reference,not.toCNF());
  }

  @Test
  public void convertNestedAndToCnfTest() {
    Predicate a = getComparison();
    Predicate b = getComparison();
    And and = new And(a,b);
    Not not = new Not(and);

    OrPredicate orPredicate = new OrPredicate();
    orPredicate.addPredicate(new Not(a));
    orPredicate.addPredicate(new Not(b));
    AndPredicate reference = new AndPredicate();
    reference.addPredicate(orPredicate);

    assertEquals(reference.toString(),not.toCNF().toString());
  }

  @Test
  public void convertNestedOrToCnfTest() {
    Predicate a = getComparison();
    Predicate b = getComparison();
    Or or = new Or(a,b);
    Not not = new Not(or);

    AndPredicate reference = new Not(a).toCNF().and(new Not(a).toCNF());
    assertEquals(reference.toString(),not.toCNF().toString());
  }

  @Test
  public void convertNestedXorToCnfTest() {
    Predicate a = getComparison();
    Predicate b = getComparison();
    Xor xor = new Xor(a,b);
    Not not = new Not(xor);

    AndPredicate reference = new Or(new And(a,b),new And(new Not(a),new Not(b))).toCNF();
    assertEquals(reference.toString(),not.toCNF().toString());
  }
}
