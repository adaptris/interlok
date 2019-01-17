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
  private List<Integer> fieldTypes;

  public JdbcResultRow() {
    setFieldNames(new ArrayList<String>());
    setFieldValues(new ArrayList<>());
    setFieldTypes(new ArrayList<Integer>());
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

  public void setFieldValue(String fieldName, Object fieldValue, int type) {
    getFieldNames().add(fieldName);
    getFieldValues().add(fieldValue);
    getFieldTypes().add(type);
  }

  public int getFieldCount() {
    return getFieldNames().size();
  }

  public Object getFieldValue(int order) {
    return getFieldValues().get(order);
  }

  public String getFieldName(int order) {
    return getFieldNames().get(order);
  }

  public Object getFieldValue(String fieldName) {
    int index = getFieldNames().indexOf(fieldName);
    if(index >= 0)
      return getFieldValues().get(index);
    else
      return null;
  }

  public List<Integer> getFieldTypes() {
    return fieldTypes;
  }

  public void setFieldTypes(List<Integer> l) {
    fieldTypes = l;
  }

}
