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

import org.xml.sax.SAXParseException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Object model representation of all an individual schema violation for rendering purposes.
 * 
 */
@XStreamAlias("schema-violation")
public class SchemaViolation {

  private String message;
  private int lineNumber;
  private int columnNumber;

  public SchemaViolation() {

  }

  public SchemaViolation(SAXParseException exception) {
    message = exception.getMessage();
    lineNumber = exception.getLineNumber();
    columnNumber = exception.getColumnNumber();
  }
}
