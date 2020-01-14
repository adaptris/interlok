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

  @Override
  public Context getInitialContext(Hashtable environment) throws NamingException {
    if(initialContext == null) {
      initialContext = new NamingContext(addAdapterSchemePackage(merge(myEnvironment, environment)));
    }
    return initialContext;
  }

  protected static Hashtable merge(Hashtable<?, ?> defaultEnv, Hashtable environment) {
    Hashtable result = (Hashtable) environment.clone();
    for (Map.Entry e : defaultEnv.entrySet()) {
      if (!environment.containsKey(e.getKey())) {
        result.put(e.getKey(), e.getValue());
      }
    }
    return result;
  }
  
  protected static Hashtable<String, Object> addAdapterSchemePackage(Hashtable<String, Object> hashtable) {
    String property = (String) hashtable.get(Context.URL_PKG_PREFIXES);
    if(property == null)
      hashtable.put(Context.URL_PKG_PREFIXES, SystemPropertiesUtil.NAMING_PACKAGE);
    else if (!property.contains(SystemPropertiesUtil.NAMING_PACKAGE))
      hashtable.put(Context.URL_PKG_PREFIXES, property + ":" + SystemPropertiesUtil.NAMING_PACKAGE);
    
    return hashtable;
  }
  
}
