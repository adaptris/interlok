package com.adaptris.core;

import java.util.Arrays;
import java.util.Properties;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import com.adaptris.core.util.ExceptionHelper;

public class SharedComponent {

  protected AdaptrisComponent triggerJndiLookup(String jndiName) throws CoreException {
    AdaptrisComponent result = null;
    try {
      InitialContext ctx = createContext();
      String compEnvName = checkAndComposeName(jndiName);
      String[] namesToTry =
      {
          compEnvName, jndiName
      };
      for (String name : namesToTry) {
        result = lookupQuietly(ctx, name);
        if (result != null) {
          break;
        }
      }
      if (result == null) {
        throw new CoreException("Failed to find adaptris component: [" + compEnvName + "] or [" + jndiName + "]");
      }
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return result;
  }

  private InitialContext createContext() throws NamingException {
    Properties env = new Properties();
    env.put(Context.INITIAL_CONTEXT_FACTORY, JndiContextFactory.class.getName());
    return new InitialContext(env);
  }

  @SuppressWarnings({"lgtm [java/jndi-injection]"})
  private AdaptrisComponent lookupQuietly(InitialContext ctx, String name) {
    AdaptrisComponent result = null;
    try {
      result = (AdaptrisComponent) ctx.lookup(name);
    }
    catch (NamingException ex) {
    }
    return result;
  }

  /**
   * If the lookup name doesn't contain any subcontext (e.g. "comp/env/") then lets prepend these sub contexts to the name.
   * If however the lookup name does include at least one sub-context, no matter what that sub-context is, then leave the name alone.
   * @param jndiName
   * @return String jndiName
   * @throws InvalidNameException
   */
  private String checkAndComposeName(String jndiName) throws InvalidNameException {
    CompositeName compositeName = new CompositeName(jndiName);
    if(compositeName.size() == 1) // no sub contexts
      jndiName = new CompositeName("comp/env/" + jndiName).toString();
    return jndiName;
  }

}
