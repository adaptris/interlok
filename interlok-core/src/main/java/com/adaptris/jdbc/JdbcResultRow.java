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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcResultRow {
  private static final Map<Integer, ParameterValueType> TYPE_MAP;

  private List<String> fieldNames;
  private List<Object> fieldValues;
  private List<ParameterValueType> fieldTypes;

  static {
    Map<Integer, ParameterValueType> typeMap = new HashMap<>();
    for (ParameterValueType t : ParameterValueType.values()) {
      typeMap.put(t.getValue(), t);
    }
    TYPE_MAP = Collections.unmodifiableMap(typeMap);
  }

  public JdbcResultRow() {
    setFieldNames(new ArrayList<String>());
    setFieldValues(new ArrayList<>());
    setFieldTypes(new ArrayList<ParameterValueType>());
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
    getFieldTypes().add(TYPE_MAP.get(type));
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

  public ParameterValueType getFieldType(int order) {
    return getFieldTypes().get(order);
  }

  public ParameterValueType getFieldType(String fieldName) {
    return getValue(getFieldTypes(), fieldName);
  }

  public Object getFieldValue(String fieldName) {
    return getValue(getFieldValues(), fieldName);
  }

  private <T> T getValue(List<T> list, String fieldName) {
    int index = getFieldNames().indexOf(fieldName);
    if (index >= 0)
      return (T) getFieldValues().get(index);
    else
      return null;
  }

  public List<ParameterValueType> getFieldTypes() {
    return fieldTypes;
  }

  public void setFieldTypes(List<ParameterValueType> l) {
    fieldTypes = l;
  }

}
