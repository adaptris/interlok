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

public class StoredProcedureParameter {
  
  private String name;
  
  private int order;
  
  private Object inValue;
  
  private Object outValue;
  
  private ParameterValueType parameterValueType;
  
  private ParameterType parameterType;
  
  public StoredProcedureParameter() {
  }
  
  public StoredProcedureParameter(String name, int order, ParameterValueType type, ParameterType parameterType, Object value) {
    this.setName(name);
    this.setOrder(order);
    this.setParameterValueType(type);
    this.setParameterType(parameterType);
    this.setInValue(value);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public Object getInValue() {
    return inValue;
  }

  public void setInValue(Object value) {
    this.inValue = value;
  }

  public ParameterValueType getParameterValueType() {
    return parameterValueType;
  }

  public void setParameterValueType(ParameterValueType parameterValueType) {
    this.parameterValueType = parameterValueType;
  }

  public ParameterType getParameterType() {
    return parameterType;
  }

  public void setParameterType(ParameterType parameterType) {
    this.parameterType = parameterType;
  }

  public Object getOutValue() {
    return outValue;
  }

  public void setOutValue(Object outValue) {
    this.outValue = outValue;
  }
  
}
