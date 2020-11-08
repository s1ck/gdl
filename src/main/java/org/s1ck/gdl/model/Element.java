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

package org.s1ck.gdl.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Element {

  private Long id;

  private List<String> labels;

  private Map<String, Object> properties;

  private String variable;

  public Element() {
    properties = new HashMap<>();
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getLabel() {
    return (labels.size() > 0) ? labels.get(0) : null;
  }

  public void setLabel(String label) {
    this.labels = Collections.singletonList(label);
  }

  public List<String> getLabels() {
    return this.labels;
  }

  public void setLabels(List<String> labels) {
    this.labels = labels;
  }

  public String getVariable() {
    return variable;
  }

  public void setVariable(String variable) {
    this.variable = variable;
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

  public String referenceString() {
    return getVariable();
  };

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Element element = (Element) o;

    return !(id != null ? !id.equals(element.id) : element.id != null);

  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
