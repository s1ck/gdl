package org.s1ck.gdl.comparables;

import org.junit.Test;
import org.s1ck.gdl.model.values.DoubleVectorLiteral;
import org.s1ck.gdl.model.values.FloatVectorLiteral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

public class VectorLiteralTest {

  @Test
  public void equalsForSameValuesTest() {
    FloatVectorLiteral a = new FloatVectorLiteral(Arrays.asList(1f, 3f));
    FloatVectorLiteral b = new FloatVectorLiteral(Arrays.asList(1f, 3f));

    assertEquals(a, b);
    assertEquals("equal vectors must share a hash code", a.hashCode(), b.hashCode());
  }

  @Test
  public void notEqualsForDifferentValuesTest() {
    assertNotEquals(new FloatVectorLiteral(Arrays.asList(1f, 3f)),
            new FloatVectorLiteral(Arrays.asList(3f, 1f)));
    assertNotEquals(new FloatVectorLiteral(Arrays.asList(1f, 3f)),
            new FloatVectorLiteral(Collections.singletonList(1f)));
  }

  @Test
  public void notEqualsAcrossElementTypesTest() {
    assertNotEquals(new FloatVectorLiteral(Collections.singletonList(1f)),
            new DoubleVectorLiteral(Collections.singletonList(1d)));
  }

  @Test
  public void notEqualsForEmptyVectorsOfDifferentTypeTest() {
    assertNotEquals(new FloatVectorLiteral(Collections.emptyList()),
            new DoubleVectorLiteral(Collections.emptyList()));
  }

  @Test
  public void notEqualsForNullAndForeignTypeTest() {
    FloatVectorLiteral vector = new FloatVectorLiteral(Collections.singletonList(1f));

    assertNotEquals(null, vector);
    assertNotEquals(vector, Collections.singletonList(1f));
  }

  @Test
  public void equalsForNaNTest() {
    // Float.equals compares bits, so NaN is equal to itself unlike ==
    assertEquals(new FloatVectorLiteral(Arrays.asList(1f, Float.NaN)),
            new FloatVectorLiteral(Arrays.asList(1f, Float.NaN)));
    assertEquals(new DoubleVectorLiteral(Collections.singletonList(Double.NaN)),
            new DoubleVectorLiteral(Collections.singletonList(Double.NaN)));
  }

  @Test
  public void toStringTest() {
    assertEquals("float_vector([1.0, 3.0])",
            new FloatVectorLiteral(Arrays.asList(1f, 3f)).toString());
    assertEquals("double_vector([1.0, 3.0])",
            new DoubleVectorLiteral(Arrays.asList(1d, 3d)).toString());
    assertEquals("double_vector([])",
            new DoubleVectorLiteral(Collections.emptyList()).toString());
  }

  @Test
  public void getElementTypeTest() {
    assertEquals(Float.class, new FloatVectorLiteral(Collections.singletonList(1f)).getElementType());
    assertEquals(Double.class, new DoubleVectorLiteral(Collections.singletonList(1d)).getElementType());
    assertEquals("an empty vector keeps the type of its class",
            Float.class, new FloatVectorLiteral(Collections.emptyList()).getElementType());
  }

  @Test
  public void getValueIsUnmodifiableTest() {
    FloatVectorLiteral vector = new FloatVectorLiteral(Arrays.asList(1f, 3f));

    assertThrows(UnsupportedOperationException.class, () -> vector.getValue().add(7f));
  }

  @Test
  public void getValueIsDefensiveCopyTest() {
    List<Float> source = new ArrayList<>(Arrays.asList(1f, 3f));
    FloatVectorLiteral vector = new FloatVectorLiteral(source);

    source.add(7f);

    assertEquals("changing the source list must not change the vector",
            Arrays.asList(1f, 3f), vector.getValue());
  }

  @Test
  public void failOnNullValueTest() {
    IllegalArgumentException exc = assertThrows(
            IllegalArgumentException.class,
            () -> new FloatVectorLiteral(null)
    );

    assertEquals("Value must not be null", exc.getMessage());
  }

  @Test
  public void failOnNullElementTest() {
    IllegalArgumentException exc = assertThrows(
            IllegalArgumentException.class,
            () -> new DoubleVectorLiteral(Arrays.asList(1d, null))
    );

    assertEquals("Elements must not be null", exc.getMessage());
  }

  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void failOnForeignElementTypeTest() {
    // the element type is only guaranteed by the constructor check, erasure lets a raw list through
    List raw = new ArrayList(Arrays.asList(1f, 2d));

    IllegalArgumentException exc = assertThrows(
            IllegalArgumentException.class,
            () -> new FloatVectorLiteral(raw)
    );

    assertEquals("Elements must be of type 'Float' but found 'Double'", exc.getMessage());
  }
}