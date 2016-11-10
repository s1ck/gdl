package org.s1ck.gdl.cnf;

import org.junit.Test;
import org.s1ck.gdl.model.comparables.ElementSelector;
import org.s1ck.gdl.model.comparables.PropertySelector;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.cnf.CNF;
import org.s1ck.gdl.model.cnf.CNFElement;
import org.s1ck.gdl.model.comparables.Literal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CNFTest {
  @Test
  public void andConjunctionTest() {
    CNFElement or1 = new CNFElement();
    CNFElement or2 = new CNFElement();

    CNF and1 = new CNF();
    and1.addPredicate(or1);

    CNF and2 = new CNF();
    and2.addPredicate(or2);

    ArrayList<CNFElement> reference = new ArrayList<>();
    reference.add(or1);
    reference.add(or2);

    assertEquals(reference, and1.and(and2).getPredicates());
  }

  @Test
  public void orConjunctionTest() {
    Predicate a = new Comparison(new Literal(1), Comparison.Comparator.GT, new Literal(5));
    Predicate b = new Comparison(new Literal(2), Comparison.Comparator.GT, new Literal(6));
    Predicate c = new Comparison(new Literal(3), Comparison.Comparator.GT, new Literal(7));
    Predicate d = new Comparison(new Literal(4), Comparison.Comparator.GT, new Literal(8));

    CNFElement or1 = new CNFElement();
    or1.addPredicate(a);
    CNFElement or2 = new CNFElement();
    or2.addPredicate(b);
    CNFElement or3 = new CNFElement();
    or3.addPredicate(c);
    CNFElement or4 = new CNFElement();
    or4.addPredicate(d);

    // Define a CNF of the Form [ [a], [b] ]
    CNF and1 = new CNF();
    and1.addPredicate(or1);
    and1.addPredicate(or2);

    // Define a CNF of the Form [ [c], [d] ]
    CNF and2 = new CNF();
    and2.addPredicate(or3);
    and2.addPredicate(or4);


    CNFElement refOr1 = new CNFElement();
    refOr1.addPredicate(a);
    refOr1.addPredicate(c);
    CNFElement refOr2 = new CNFElement();
    refOr2.addPredicate(a);
    refOr2.addPredicate(d);
    CNFElement refOr3 = new CNFElement();
    refOr3.addPredicate(b);
    refOr3.addPredicate(c);
    CNFElement refOr4 = new CNFElement();
    refOr4.addPredicate(b);
    refOr4.addPredicate(d);

    // Expected output is [ [a,c], [a,d], [b,c]. [b,d] ]
    CNF reference = new CNF();
    reference.addPredicate(refOr1);
    reference.addPredicate(refOr2);
    reference.addPredicate(refOr3);
    reference.addPredicate(refOr4);

    assertEquals(reference.toString(), and1.or(and2).toString());
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

    List<CNFElement> cnfElements = new ArrayList<>();
    CNFElement e1 = new CNFElement();
    e1.addPredicate(a);
    cnfElements.add(e1);

    CNFElement e2 = new CNFElement();
    e1.addPredicate(b);
    cnfElements.add(e2);

    CNF cnf = new CNF();
    cnf.addPredicates(cnfElements);

    Set<String> reference = new HashSet<>();
    reference.add("a");
    reference.add("b");

    assertEquals(reference,cnf.getVariables());
  }

  @Test
  public void createExistingSubCnfTest() {
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

    Comparison c = new Comparison(
            new PropertySelector("c","label"),
            Comparison.Comparator.EQ,
            new Literal("Person")
    );

    List<CNFElement> cnfElements = new ArrayList<>();
    List<CNFElement> refCnfElements = new ArrayList<>();

    CNFElement e1 = new CNFElement();
    e1.addPredicate(a);
    cnfElements.add(e1);
    refCnfElements.add(e1);

    CNFElement e2 = new CNFElement();
    e2.addPredicate(b);
    cnfElements.add(e2);

    CNFElement e3 = new CNFElement();
    e3.addPredicate(c);
    cnfElements.add(e3);

    CNF cnf = new CNF();
    cnf.addPredicates(cnfElements);

    CNF reference = new CNF();
    reference.addPredicates(refCnfElements);

    Set<String> variables = new HashSet<>();
    variables.add("a");
    variables.add("b");

    assertEquals(reference,cnf.getSubCNF(variables));

    variables.add("c");
    assertTrue(cnf.getSubCNF(variables).getPredicates().isEmpty());
  }
}
