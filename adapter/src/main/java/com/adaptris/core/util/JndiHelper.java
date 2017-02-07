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

package com.adaptris.core.util;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.CoreException;
import com.adaptris.core.JndiBindable;
import com.adaptris.core.JndiContextFactory;
import com.adaptris.core.jdbc.DatabaseConnection;

public class JndiHelper {

  private static transient Logger log = LoggerFactory.getLogger(JndiHelper.class);

  private static String createJndiName(AdaptrisComponent object) throws InvalidNameException {
    String jndiName = null;
    if (((JndiBindable) object).getLookupName() != null) jndiName = ((JndiBindable) object).getLookupName();
    else
      return createJndiName(object.getUniqueId());

    return jndiName;
  }
  
  private static String createJndiName(String lookupId) throws InvalidNameException {
    String jndiName = null;
    int indexOfSchemeMarker = lookupId.indexOf(":"); // remove the scheme
    if (indexOfSchemeMarker > 0) {
      CompositeName configuredCompositeName = new CompositeName(lookupId.substring(indexOfSchemeMarker + 1));
      jndiName = "comp/env/" + configuredCompositeName.get(configuredCompositeName.size() - 1);
    }
    else {
      CompositeName configuredCompositeName = new CompositeName(lookupId);
      jndiName = "comp/env/" + configuredCompositeName.get(configuredCompositeName.size() - 1);
    }

    return jndiName;
  }

  private static String createJndiJdbcName(AdaptrisComponent object) throws InvalidNameException {
    CompositeName configuredCompositeName = null;
    if (((JndiBindable) object).getLookupName() != null) configuredCompositeName = new CompositeName(((JndiBindable) object).getLookupName());
    else {
      int indexOfSchemeMarker = object.getUniqueId().indexOf(":"); // remove the scheme
      if (indexOfSchemeMarker > 0) configuredCompositeName = new CompositeName(object.getUniqueId().substring(
          indexOfSchemeMarker + 1));
      else
        configuredCompositeName = new CompositeName(object.getUniqueId());

    }
    return "comp/env/jdbc/" + configuredCompositeName.get(configuredCompositeName.size() - 1);
  }

  public static void bind(Collection<? extends AdaptrisComponent> components) throws CoreException {
    bind(createContext(), components, false);
  }
  
  public static void bind(AdaptrisComponent adaptrisComponent) throws CoreException {
    bind(createContext(), adaptrisComponent, false);
  }

  public static void bind(Collection<? extends AdaptrisComponent> components, boolean debug) throws CoreException {
    bind(createContext(), components, debug);
  }
  
  public static void bind(AdaptrisComponent adaptrisComponent, boolean debug) throws CoreException {
    bind(createContext(), adaptrisComponent, debug);
  }

  public static void bind(Context ctx, Collection<? extends AdaptrisComponent> components, boolean debug) throws CoreException {
    for (AdaptrisComponent concomponent : components) {
      bind(ctx, concomponent, debug);
    }
  }
  
  public static void bind(Context ctx, AdaptrisComponent component, boolean debug) throws CoreException {
    try {
      if (component == null) {
        if (debug) log.trace("Component is null, ignoring");
        return;
      }
      if (component instanceof DatabaseConnection) { // database connection, bind also to jdbc subcontext
        String jndiJdbcName = createJndiJdbcName(component);
        ctx.bind(jndiJdbcName, ((DatabaseConnection) component).asDataSource());
        if (debug)
          log.trace("Saved JNDI entry {} for object ({}) {}", jndiJdbcName, ((DatabaseConnection) component).asDataSource()
              .getClass().getName(), ((DatabaseConnection) component).asDataSource());
      }
      String jndiName = createJndiName(component);
      ctx.bind(jndiName, component);
      if (debug) log.trace("Saved JNDI entry {} for object ({}) {}", jndiName, component.getClass().getName(), component);
    }
    catch (NamingException | SQLException ex) {
      if (debug) log.error("Component '{}' could not be bound into the JNDI context.", component.getUniqueId());
      ExceptionHelper.rethrowCoreException(ex);
    }
  }

  public static void unbind(Collection<? extends AdaptrisComponent> components) throws CoreException {
    unbind(createContext(), components, false);
  }

  public static void unbind(Collection<? extends AdaptrisComponent> components, boolean debug) throws CoreException {
    unbind(createContext(), components, debug);
  }

  public static void unbind(AdaptrisComponent component, boolean debug) throws CoreException {
    unbind(createContext(), component, debug);
  }

  public static void unbind(Context ctx, Collection<? extends AdaptrisComponent> components, boolean debug) throws CoreException {
    for (AdaptrisComponent component : components) {
      unbind(ctx, component, debug);
    }
  }

  public static void unbind(Context ctx, AdaptrisComponent component, boolean debug) throws CoreException {
    try {
      if (component == null) {
        if (debug) log.trace("Component is null, ignoring");
        return;
      }
      if (component instanceof DatabaseConnection) { // database connection, bind also to jdbc subcontext
        String jndiJdbcName = createJndiJdbcName(component);
        ctx.unbind(jndiJdbcName);
        if (debug) log.trace("Deleted JNDI entry {}", jndiJdbcName);
      }
      String jndiName = createJndiName(component);
      ctx.unbind(jndiName);
      if (debug) log.trace("Deleted JNDI entry {}", jndiName);
    }
    catch (NamingException ex) {
      if (debug) log.error("Component '{}' could not be unbound from the JNDI context.", component.getUniqueId());
      ExceptionHelper.rethrowCoreException(ex);
    }
  }

  public static void unbindQuietly(Context ctx, Collection<? extends AdaptrisComponent> components, boolean debug) {
    for (AdaptrisComponent component : components) {
      unbindQuietly(ctx, component, debug);
    }
  }

  public static void unbindQuietly(Context ctx, AdaptrisComponent component, boolean debug) {
    try {
      unbind(ctx, component, debug);
    }
    catch (CoreException e) {

    }
  }

  private static InitialContext createContext() throws CoreException {
    InitialContext context = null;
    try {
      Properties contextEnv = new Properties();
      contextEnv.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
      context = new InitialContext(contextEnv);
    }
    catch (NamingException e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return context;
  }
}
