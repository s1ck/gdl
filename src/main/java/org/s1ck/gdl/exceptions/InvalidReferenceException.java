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

package org.s1ck.gdl.exceptions;

/**
 * Raised when referencing a variable in a predicate and the variable was not defined;
 */
public class InvalidReferenceException extends RuntimeException {

  /**
   * Creates a new exception
   *
   * @param variable the variable which could not be resolved
   */
  public InvalidReferenceException(String variable) {
    super("Predicate references variable '" + variable + "' which was not defined");
  }
}
