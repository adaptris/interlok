package com.adaptris.naming.adapter;

public interface JndiNameCreator {

  public String createName(Object object) throws AdapterNamingException;
  
}
