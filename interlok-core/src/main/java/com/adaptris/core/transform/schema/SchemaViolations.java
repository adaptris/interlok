/*
 * Copyright 2020 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.adaptris.core.transform.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Object model representation of all the schema violations for rendering purposes.
 * 
 */
@XStreamAlias("schema-violations")
public class SchemaViolations {

  // the violations
  @XStreamImplicit
  private List<SchemaViolation> violations;

  public SchemaViolations() {
    setViolations(new ArrayList<>());
  }

  public void addViolation(SchemaViolation... schemaViolations) {
    getViolations().addAll(new ArrayList<>(Arrays.asList(schemaViolations)));
  }

  public List<SchemaViolation> getViolations() {
    return violations;
  }

  public void setViolations(List<SchemaViolation> violations) {
    this.violations = violations;
  }
}
