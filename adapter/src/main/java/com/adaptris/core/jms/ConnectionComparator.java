package com.adaptris.core.jms;

public interface ConnectionComparator<T> {

  boolean connectionEquals(T comparable);
  
}
