package org.s1ck.gdl.predicates.booleans;

import org.junit.Test;
import org.s1ck.gdl.model.comparables.ElementSelector;
import org.s1ck.gdl.model.comparables.Literal;
import org.s1ck.gdl.model.comparables.PropertySelector;
import org.s1ck.gdl.model.predicates.*;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.booleans.Not;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.booleans.Xor;
import org.s1ck.gdl.model.cnf.CNF;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.predicates.PredicateTest;

import java.util.HashSet;
import java.util.Set;

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

    Xor xor = new Xor(a,b);

    Set<String> reference = new HashSet<>();
    reference.add("a");
    reference.add("b");

    assertEquals(reference,xor.getVariables());
  }
}
