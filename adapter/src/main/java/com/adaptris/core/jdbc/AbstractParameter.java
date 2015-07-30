package com.adaptris.core.jdbc;

import com.adaptris.jdbc.ParameterValueType;

public abstract class AbstractParameter implements InOutParameter {

  private int order;
  private ParameterValueType type;
  private String name;

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public ParameterValueType getType() {
    return type;
  }

  public void setType(ParameterValueType type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  
}
