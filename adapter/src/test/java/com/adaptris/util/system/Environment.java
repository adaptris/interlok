/*
 * $Author: lchan $
 * $RCSfile: Environment.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/07/01 10:30:41 $
 */
package com.adaptris.util.system;

import java.util.Map;

/** Get environment variables from the system.
 * 
 * @author $Author: lchan $
 * @author lchan
 */
@Deprecated
public final class Environment {

  private static Environment procEnv = null;
  private Map<String, String> env = null;

  private Environment() {
    env = System.getenv();
  }

  /** Get the environment instance.
   *  @return the environment.
   */
  public static Environment getInstance() {
    if (procEnv == null) {
      procEnv = new Environment();
    }
    return procEnv;
  }

  /**
   * Return the environment variable value specified by <code>envVar</code>.
   * @param envVar the environment variable
   * @return The environment variable value, or null if the variable does not
   * exist
   */
  public String getVariable(String envVar) {
    return env.containsKey(envVar) ? (String) env.get(envVar) : null;
  }

  /**
   * Query the existance of an environment variable.
   * @param envVar the environment variable
   * @return true if the environment variable exists
   */
  public boolean exists(String envVar) {
    return env.containsKey(envVar);
  }

  /**
   * Return a clone of the environment.
   * @return the environment stored as a Map
   */
  public Map getEnvironment() {
    return env;
  }
}
