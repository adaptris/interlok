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
