/*
 * Copyright 2017 The GDL Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.s1ck.gdl.model.values;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a vector of numbers, e.g. {@code vector([1.0f, 3.0f, 3.0f, 7.0f])}.
 *
 * <p>A vector never mixes element types. Each concrete subclass fixes the element type, so use
 * {@link FloatVectorLiteral} or {@link DoubleVectorLiteral} to create one and {@code instanceof}
 * to tell them apart.
 */
public abstract class VectorLiteral {

  /**
   * Returns the vector value.
   *
   * @return unmodifiable list of vector elements, all of {@link #getElementType()}
   */
  public abstract List<? extends Number> getValue();

  /**
   * Returns the type of all elements in this vector.
   *
   * @return element type
   */
  public abstract Class<? extends Number> getElementType();

  /**
   * Returns the name of this vector type, used as prefix of the string representation.
   *
   * @return vector type name
   */
  protected abstract String getTypeName();

  /**
   * Checks that a vector value contains no null and no foreign elements and returns an
   * unmodifiable copy of it.
   *
   * @param value vector value
   * @param elementType expected type of every element
   * @param <T> element type
   * @return unmodifiable copy of the value
   */
  protected static <T extends Number> List<T> checkedCopy(List<T> value, Class<T> elementType) {
    if (value == null) {
      throw new IllegalArgumentException("Value must not be null");
    }
    for (T element : value) {
      if (element == null) {
        throw new IllegalArgumentException("Elements must not be null");
      }
      if (element.getClass() != elementType) {
        throw new IllegalArgumentException(String.format(
                "Elements must be of type '%s' but found '%s'",
                elementType.getSimpleName(), element.getClass().getSimpleName()));
      }
    }
    return Collections.unmodifiableList(new ArrayList<>(value));
  }

  @Override
  public String toString() {
    return getTypeName() + "(" + getValue() + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VectorLiteral vector = (VectorLiteral) o;

    return getValue().equals(vector.getValue());
  }

  @Override
  public int hashCode() {
    return getValue().hashCode();
  }
}