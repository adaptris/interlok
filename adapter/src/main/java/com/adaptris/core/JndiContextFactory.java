package com.adaptris.core;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.management.SystemPropertiesUtil;
import com.adaptris.naming.adapter.NamingContext;

public class JndiContextFactory implements InitialContextFactory {

  private static transient Logger log = LoggerFactory.getLogger(JndiContextFactory.class.getName());
  
  private Hashtable<?, ?> myEnvironment;
  
  protected static Context initialContext = null;

  public JndiContextFactory() {   
    this(new Hashtable<Object, Object>());
  }

  public JndiContextFactory(Hashtable environment) {
    myEnvironment = environment;
  }

  public Context getInitialContext(Hashtable environment) throws NamingException {
    if(initialContext == null) {
      initialContext = new NamingContext(addAdapterSchemePackage(merge(environment)));
    }
    return initialContext;
  }

  private Hashtable merge(Hashtable environment) {
    Hashtable result = (Hashtable) environment.clone();
    for (Map.Entry e : myEnvironment.entrySet()) {
      if (!environment.containsKey(e.getKey())) {
        result.put(e.getKey(), e.getValue());
      }
    }
    return result;
  }
  
  private static Hashtable<String, Object> addAdapterSchemePackage(Hashtable<String, Object> hashtable) {
    String property = (String) hashtable.get(Context.URL_PKG_PREFIXES);
    if(property == null)
      hashtable.put(Context.URL_PKG_PREFIXES, SystemPropertiesUtil.NAMING_PACKAGE);
    else if (!property.contains(SystemPropertiesUtil.NAMING_PACKAGE))
      hashtable.put(Context.URL_PKG_PREFIXES, property + ":" + SystemPropertiesUtil.NAMING_PACKAGE);
    
    return hashtable;
  }
  
}
