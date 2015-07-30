package com.adaptris.core.jdbc;

import java.util.List;

public interface ParameterList<T> {

  List<T> getParameters();
  
  T getByName(String name);
  
  T getByOrder(int order);
  
}
