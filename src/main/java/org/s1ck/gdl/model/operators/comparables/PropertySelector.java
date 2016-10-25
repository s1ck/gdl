package org.s1ck.gdl.model.operators.comparables;

import org.s1ck.gdl.model.GraphElement;

/**
 * Created by max on 25.10.16.
 */
public class PropertySelector extends Comparable {
  private GraphElement element;
  private String propertyName;

  public PropertySelector(GraphElement element, String propertyName) {
    this.element = element;
    this.propertyName = propertyName;
  }

  public String toString() {
    return element.getVariable() + "." + propertyName;
  }
}
