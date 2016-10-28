package org.s1ck.gdl.predicates.cnf;

import org.junit.Test;
import org.s1ck.gdl.model.predicates.Comparison;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.cnf.AndPredicate;
import org.s1ck.gdl.model.predicates.cnf.OrPredicate;
import org.s1ck.gdl.model.predicates.comparables.Literal;
import static org.junit.Assert.assertEquals;



import java.util.ArrayList;

public class AndPredicateTest {
  @Test
  public void andConjunctionTest() {
    OrPredicate or1 = new OrPredicate();
    OrPredicate or2 = new OrPredicate();

    AndPredicate and1 = new AndPredicate();
    and1.addPredicate(or1);

    AndPredicate and2 = new AndPredicate();
    and2.addPredicate(or2);

    ArrayList<OrPredicate> reference = new ArrayList<>();
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

    OrPredicate or1 = new OrPredicate();
    or1.addPredicate(a);
    OrPredicate or2 = new OrPredicate();
    or2.addPredicate(b);
    OrPredicate or3 = new OrPredicate();
    or3.addPredicate(c);
    OrPredicate or4 = new OrPredicate();
    or4.addPredicate(d);

    // Define a CNF of the Form [ [a], [b] ]
    AndPredicate and1 = new AndPredicate();
    and1.addPredicate(or1);
    and1.addPredicate(or2);

    // Define a CNF of the Form [ [c], [d] ]
    AndPredicate and2 = new AndPredicate();
    and2.addPredicate(or3);
    and2.addPredicate(or4);


    OrPredicate refOr1 = new OrPredicate();
    refOr1.addPredicate(a);
    refOr1.addPredicate(c);
    OrPredicate refOr2 = new OrPredicate();
    refOr2.addPredicate(a);
    refOr2.addPredicate(d);
    OrPredicate refOr3 = new OrPredicate();
    refOr3.addPredicate(b);
    refOr3.addPredicate(c);
    OrPredicate refOr4 = new OrPredicate();
    refOr4.addPredicate(b);
    refOr4.addPredicate(d);

    // Expected output is [ [a,c], [a,d], [b,c]. [b,d] ]
    AndPredicate reference = new AndPredicate();
    reference.addPredicate(refOr1);
    reference.addPredicate(refOr2);
    reference.addPredicate(refOr3);
    reference.addPredicate(refOr4);

    assertEquals(reference.toString(), and1.or(and2).toString());
  }
}
