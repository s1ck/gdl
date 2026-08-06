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

import java.util.List;

/**
 * Represents a vector of floats, e.g. {@code vector([1.0f, 3.0f, 3.0f, 7.0f])}.
 */
public class FloatVectorLiteral extends VectorLiteral {

  /**
   * vector value
   */
  private final List<Float> value;

  /**
   * Creates a new float vector
   *
   * @param value vector value, must not contain null elements
   */
  public FloatVectorLiteral(List<Float> value) {
    this.value = checkedCopy(value, Float.class);
  }

  @Override
  public List<Float> getValue() {
    return value;
  }

  @Override
  public Class<Float> getElementType() {
    return Float.class;
  }

  @Override
  protected String getTypeName() {
    return "float_vector";
  }
}