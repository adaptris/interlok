package com.adaptris.naming.adapter;

import com.adaptris.core.AdaptrisConnection;

public class AdapterSchemeJndiNameCreator implements JndiNameCreator {

  private static final transient String STATIC_NAME = "adapter:comp/env/";
  
  @Override
  public String createName(Object object) throws AdapterNamingException {
    if(object instanceof AdaptrisConnection)
      return STATIC_NAME + ((AdaptrisConnection) object).getUniqueId();
    throw new AdapterNamingException("Object of class '" + object.getClass().getName() + "' is not an AdaptrisConnection, cannot create a JNDI name.");
  }

}
