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

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.JndiContextFactory;
import com.adaptris.core.jdbc.DatabaseConnection;

public class JndiHelper {

  private static transient Logger log = LoggerFactory.getLogger(JndiHelper.class);

  private static String createJndiName(AdaptrisConnection object) throws InvalidNameException {
    String jndiName = null;
    if (object.getLookupName() != null) jndiName = object.getLookupName();
    else {
      int indexOfSchemeMarker = object.getUniqueId().indexOf(":"); // remove the scheme
      if (indexOfSchemeMarker > 0) {
        CompositeName configuredCompositeName = new CompositeName(object.getUniqueId().substring(indexOfSchemeMarker + 1));
        jndiName = "comp/env/" + configuredCompositeName.get(configuredCompositeName.size() - 1);
      }
      else {
        CompositeName configuredCompositeName = new CompositeName(object.getUniqueId());
        jndiName = "comp/env/" + configuredCompositeName.get(configuredCompositeName.size() - 1);
      }
    }

    return jndiName;
  }

  private static String createJndiJdbcName(AdaptrisConnection object) throws InvalidNameException {
    CompositeName configuredCompositeName = null;
    if (object.getLookupName() != null) configuredCompositeName = new CompositeName(object.getLookupName());
    else {
      int indexOfSchemeMarker = object.getUniqueId().indexOf(":"); // remove the scheme
      if (indexOfSchemeMarker > 0) configuredCompositeName = new CompositeName(object.getUniqueId().substring(
          indexOfSchemeMarker + 1));
      else
        configuredCompositeName = new CompositeName(object.getUniqueId());

    }
    return "comp/env/jdbc/" + configuredCompositeName.get(configuredCompositeName.size() - 1);
  }

  public static void bind(Collection<AdaptrisConnection> connections) throws CoreException {
    bind(createContext(), connections, false);
  }

  public static void bind(Collection<AdaptrisConnection> connections, boolean debug) throws CoreException {
    bind(createContext(), connections, debug);
  }

  public static void bind(Context ctx, Collection<AdaptrisConnection> connections, boolean debug) throws CoreException {
    for (AdaptrisConnection connection : connections) {
      bind(ctx, connection, debug);
    }
  }

  public static void bind(AdaptrisConnection con, boolean debug) throws CoreException {
    bind(createContext(), con, debug);
  }

  public static void bind(Context ctx, AdaptrisConnection connection, boolean debug) throws CoreException {
    try {
      if (connection == null) {
        if (debug) log.trace("connection is null, ignoring");
        return;
      }
      if (connection instanceof DatabaseConnection) { // database connection, bind also to jdbc subcontext
        String jndiJdbcName = createJndiJdbcName(connection);
        ctx.bind(jndiJdbcName, ((DatabaseConnection) connection).asDataSource());
        if (debug)
          log.trace("Saved JNDI entry {} for object ({}) {}", jndiJdbcName, ((DatabaseConnection) connection).asDataSource()
              .getClass().getName(), ((DatabaseConnection) connection).asDataSource());
      }
      String jndiName = createJndiName(connection);
      ctx.bind(jndiName, connection);
      if (debug) log.trace("Saved JNDI entry {} for object ({}) {}", jndiName, connection.getClass().getName(), connection);
    }
    catch (NamingException | SQLException ex) {
      if (debug) log.error("Connection '{}' could not be bound into the JNDI context.", connection.getUniqueId());
      ExceptionHelper.rethrowCoreException(ex);
    }
  }

  public static void unbind(Collection<AdaptrisConnection> connections) throws CoreException {
    unbind(createContext(), connections, false);
  }

  public static void unbind(Collection<AdaptrisConnection> connections, boolean debug) throws CoreException {
    unbind(createContext(), connections, debug);
  }

  public static void unbind(AdaptrisConnection conn, boolean debug) throws CoreException {
    unbind(createContext(), conn, debug);
  }

  public static void unbind(Context ctx, Collection<AdaptrisConnection> connections, boolean debug) throws CoreException {
    for (AdaptrisConnection connection : connections) {
      unbind(ctx, connection, debug);
    }
  }

  public static void unbind(Context ctx, AdaptrisConnection connection, boolean debug) throws CoreException {
    try {
      if (connection == null) {
        if (debug) log.trace("connection is null, ignoring");
        return;
      }
      if (connection instanceof DatabaseConnection) { // database connection, bind also to jdbc subcontext
        String jndiJdbcName = createJndiJdbcName(connection);
        ctx.unbind(jndiJdbcName);
        if (debug) log.trace("Deleted JNDI entry {}", jndiJdbcName);
      }
      String jndiName = createJndiName(connection);
      ctx.unbind(jndiName);
      if (debug) log.trace("Deleted JNDI entry {}", jndiName);
    }
    catch (NamingException ex) {
      if (debug) log.error("Connection '{}' could not be unbound from the JNDI context.", connection.getUniqueId());
      ExceptionHelper.rethrowCoreException(ex);
    }
  }

  public static void unbindQuietly(Context ctx, Collection<AdaptrisConnection> connections, boolean debug) {
    for (AdaptrisConnection connection : connections) {
      unbindQuietly(ctx, connection, debug);
    }
  }

  public static void unbindQuietly(Context ctx, AdaptrisConnection connection, boolean debug) {
    try {
      unbind(ctx, connection, debug);
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
