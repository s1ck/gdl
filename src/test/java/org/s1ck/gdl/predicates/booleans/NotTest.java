package org.s1ck.gdl.predicates.booleans;

import org.junit.Test;
import org.s1ck.gdl.model.comparables.Literal;
import org.s1ck.gdl.model.comparables.PropertySelector;
import org.s1ck.gdl.model.predicates.*;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.booleans.Not;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.booleans.Xor;
import org.s1ck.gdl.model.cnf.CNF;
import org.s1ck.gdl.model.cnf.CNFElement;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.predicates.PredicateTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class NotTest extends PredicateTest {

  @Test
  public void convertNestedNotToCnfTest() {
    Predicate a = getComparison();
    Predicate nestedNot = new Not(a);

    Not not = new Not(nestedNot);

    CNF reference = a.toCNF();

    assertEquals(reference,not.toCNF());
  }

  @Test
  public void convertNestedComparisonToCnfTest() {
    Predicate a = getComparison();
    Not not = new Not(a);

    CNFElement CNFElement = new CNFElement();
    CNFElement.addPredicate(not);
    CNF reference = new CNF();
    reference.addPredicate(CNFElement);

    assertEquals(reference,not.toCNF());
  }

  @Test
  public void convertNestedAndToCnfTest() {
    Predicate a = getComparison();
    Predicate b = getComparison();
    And and = new And(a,b);
    Not not = new Not(and);

    CNFElement CNFElement = new CNFElement();
    CNFElement.addPredicate(new Not(a));
    CNFElement.addPredicate(new Not(b));
    CNF reference = new CNF();
    reference.addPredicate(CNFElement);

    assertEquals(reference.toString(),not.toCNF().toString());
  }

  @Test
  public void convertNestedOrToCnfTest() {
    Predicate a = getComparison();
    Predicate b = getComparison();
    Or or = new Or(a,b);
    Not not = new Not(or);

    CNF reference = new Not(a).toCNF().and(new Not(a).toCNF());
    assertEquals(reference.toString(),not.toCNF().toString());
  }

  @Test
  public void convertNestedXorToCnfTest() {
    Predicate a = getComparison();
    Predicate b = getComparison();
    Xor xor = new Xor(a,b);
    Not not = new Not(xor);

    CNF reference = new Or(new And(a,b),new And(new Not(a),new Not(b))).toCNF();
    assertEquals(reference.toString(),not.toCNF().toString());
  }

  @Test
  public void extractVariablesTest() {
    Comparison a = new Comparison(
            new PropertySelector("a","label"),
            Comparison.Comparator.EQ,
            new Literal("Person")
    );

    Not not = new Not(a);

    Set<String> reference = new HashSet<>();
    reference.add("a");

    assertEquals(reference,not.getVariables());
  }
}
