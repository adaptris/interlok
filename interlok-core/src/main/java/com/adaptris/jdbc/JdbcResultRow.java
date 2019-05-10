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

import com.adaptris.annotation.Removal;

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

  private void setFieldNames(List<String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  private List<Object> getFieldValues() {
    return fieldValues;
  }

  private void setFieldValues(List<Object> fieldValues) {
    this.fieldValues = fieldValues;
  }

  /**
   * @deprecated since 3.8.3 use {@link #setFieldValue(String, Object, Integer)} instead to add
   *             additional Type information.
   */
  @Deprecated
  @Removal(version = "3.11.0", message = "Use #setFieldValue(String, Object, Integer) instead")
  public void setFieldValue(String fieldName, Object fieldValue) {
    setFieldValue(fieldName, fieldValue, (ParameterValueType) null);
  }


  /**
   * Set the field value.
   *
   * @param fieldName the fieldname
   * @param fieldValue the field value
   * @param type the type if possible.
   */
  public void setFieldValue(String fieldName, Object fieldValue, ParameterValueType type) {
    getFieldNames().add(fieldName);
    getFieldValues().add(fieldValue);
    getFieldTypes().add(type);
  }


  /**
   * Set the field value.
   *
   * @param fieldName the fieldname
   * @param fieldValue the field value
   * @param type the type from {@code java.sql.Types}.
   */
  public void setFieldValue(String fieldName, Object fieldValue, Integer type) {
    setFieldValue(fieldName, fieldValue, TYPE_MAP.get(type));
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
      return list.get(index);
    else
      return null;
  }

  public List<ParameterValueType> getFieldTypes() {
    return fieldTypes;
  }

  private void setFieldTypes(List<ParameterValueType> l) {
    fieldTypes = l;
  }

}
