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
 * Represents a vector of doubles, e.g. {@code vector([1.0d, 3.0d, 3.0d, 7.0d])}.
 */
public class DoubleVectorLiteral extends VectorLiteral {

  /**
   * vector value
   */
  private final List<Double> value;

  /**
   * Creates a new double vector
   *
   * @param value vector value, must not contain null elements
   */
  public DoubleVectorLiteral(List<Double> value) {
    this.value = checkedCopy(value, Double.class);
  }

  @Override
  public List<Double> getValue() {
    return value;
  }

  @Override
  public Class<Double> getElementType() {
    return Double.class;
  }

  @Override
  protected String getTypeName() {
    return "double_vector";
  }
}