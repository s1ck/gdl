/*
 * This file is part of GDL.
 *
 * GDL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GDL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GDL.  If not, see <http://www.gnu.org/licenses/>.
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
