package com.adaptris.core.jdbc;

import com.adaptris.jdbc.ParameterValueType;


public interface JdbcParameter {
  
  public ParameterValueType getType();
  
  public int getOrder();
  
  public String getName();

}
