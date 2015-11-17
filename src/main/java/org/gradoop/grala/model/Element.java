package org.gradoop.grala.model;

import com.google.common.collect.Maps;

import java.util.Map;

public class Element {
  private long id;

  private String label;

  private Map<String, Object> properties;

  public Element() {
    properties = Maps.newHashMap();
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  public void addProperty(String key, Object value) {
    properties.put(key, value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Element element = (Element) o;

    return id == element.id;

  }

  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }
}
