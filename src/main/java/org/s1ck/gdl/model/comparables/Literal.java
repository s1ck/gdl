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

package org.s1ck.gdl.model.comparables;

/**
 * Represents a literal like String, Integer, ...
 */
public class Literal implements ComparableExpression {

  /**
   * literal value
   */
  private Object value;

  /**
   * Creates a new Literal
   *
   * @param value literal value
   */
  public Literal(Object value) {
    this.value = value;
  }

  public Object getValue() {
    return value;
  }

  /**
   * Returns null since this does not reference a variable
   * @return null
   */
  @Override
  public String getVariable() {
    return null;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Literal literal = (Literal) o;

    return value != null ? value.equals(literal.value) : literal.value == null;

  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }
}
