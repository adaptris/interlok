/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.jdbc;

import java.util.ArrayList;
import java.util.List;

public class JdbcResultRow {

  private List<String> fieldNames;
  private List<Object> fieldValues;
  
  public JdbcResultRow() {
    this.setFieldNames(new ArrayList<String>());
    this.setFieldValues(new ArrayList<Object>());
  }

  public List<String> getFieldNames() {
    return fieldNames;
  }

  public void setFieldNames(List<String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  public List<Object> getFieldValues() {
    return fieldValues;
  }

  public void setFieldValues(List<Object> fieldValues) {
    this.fieldValues = fieldValues;
  }
  
  public void setFieldValue(String fieldName, Object fieldValue) {
    this.getFieldNames().add(fieldName);
    this.getFieldValues().add(fieldValue);
  }
  
  public int getFieldCount() {
    return this.getFieldNames().size();
  }
  
  public Object getFieldValue(int order) {
    return this.getFieldValues().get(order);
  }
  
  public String getFieldName(int order) {
    return this.getFieldNames().get(order);
  }
  
  public Object getFieldValue(String fieldName) {
    int index = this.getFieldNames().indexOf(fieldName);
    if(index >= 0)
      return this.getFieldValues().get(index);
    else
      return null;
  }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    for(int counter = 0; counter < this.getFieldCount(); counter ++) {
      buffer.append(this.getFieldName(counter) + " = " + this.getFieldValue(counter) + "; ");
    }
    
    return buffer.toString();
  }
}
