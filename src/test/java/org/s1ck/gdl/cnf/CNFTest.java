package org.s1ck.gdl.cnf;

import org.junit.Test;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.cnf.CNF;
import org.s1ck.gdl.model.cnf.CNFElement;
import org.s1ck.gdl.model.comparables.Literal;
import static org.junit.Assert.assertEquals;



import java.util.ArrayList;

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
}
