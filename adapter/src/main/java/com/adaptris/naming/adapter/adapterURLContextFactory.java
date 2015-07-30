package com.adaptris.naming.adapter;

import java.util.Hashtable;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

import com.adaptris.core.JndiContextFactory;

public class adapterURLContextFactory implements ObjectFactory {

  public adapterURLContextFactory() {

  }

  @Override
  public Object getObjectInstance(Object urlInfo, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
    if (urlInfo == null)
      return new JndiContextFactory(environment).getInitialContext(environment);

    if (urlInfo instanceof String) {
      Context urlCtx = getURLContext(environment);
      try {
        return urlCtx.lookup((String) urlInfo);
      } finally {
        urlCtx.close();
      }
    }

    if (urlInfo instanceof String[]) {
      String[] urls = (String[]) urlInfo;
      if (urls.length == 0) 
        throw (new ConfigurationException("adapterURLContextFactory: empty URL array"));
      
      Context urlCtx = getURLContext(environment);
      try {
        NamingException ne = null;
        for (int i = 0; i < urls.length; i++) {
          try {
            return urlCtx.lookup(urls[i]);
          } catch (NamingException e) {
            ne = e;
          }
        }
        throw ne;
      } finally {
        urlCtx.close();
      }
    }

    throw new IllegalArgumentException("argument must be a 'adapter' URL string or an array of them");
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected Context getURLContext( Hashtable environment) throws NamingException {
    environment.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
    return new InitialContext(environment);
  }
}
