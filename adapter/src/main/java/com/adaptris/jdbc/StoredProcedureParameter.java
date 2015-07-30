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

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("Name = " + this.getName() + "; ");
    buffer.append("Order = " + this.getOrder() + "; ");
    buffer.append("InValue = " + this.getInValue() + "; ");
    buffer.append("OutValue = " + this.getOutValue() + "; ");
    buffer.append("ParameterType = " + this.getParameterType() + "; ");
    buffer.append("ParameterValueType = " + this.getParameterValueType());
    return buffer.toString();
  }
  
}
