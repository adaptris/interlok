/*
 * $Author: lchan $
 * $RCSfile: Environment.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/07/01 10:30:41 $
 */
package com.adaptris.util.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** Get environment variables from the system.
 * 
 * @author $Author: lchan $
 * @author lchan
 */
public final class Environment {

  private static Environment procEnv = null;
  private Properties env = null;

  private Environment() {
    initProperties(getEnv());
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
    return (Properties) env.clone();
  }

  private List getEnv() {
    List envList = new ArrayList();
    try {
      Process process = Runtime.getRuntime().exec(getCmd());
      BufferedReader in =
        new BufferedReader(new InputStreamReader(process.getInputStream()));

      String var = null;
      String line;
      String lineSep = System.getProperty("line.separator");
      while ((line = in.readLine()) != null) {
        if (line.indexOf('=') == -1) {
          if (var == null) {
            var = lineSep + line;
          }
          else {
            var += lineSep + line;
          }
        }
        else {
          if (var != null) {
            envList.add(var);
          }
          var = line;
        }
      }
      if (var != null) {
        envList.add(var);
      }
    }
    catch (IOException e) {
      ;
    }
    return envList;
  }

  private void initProperties(List l) {
    env = new Properties();
    Iterator i = l.iterator();
    while (i.hasNext()) {
      String e = i.next().toString();
      int pos = e.indexOf('=');
      if (pos != -1) {
        env.put(e.substring(0, pos).toUpperCase(), e.substring(pos + 1));
      }
    }
  }

  private String[] getCmd() {

    if (Os.isOs(Os.OS2) || Os.isFamily(Os.WINDOWS_NT_FAMILY)) {
      String[] cmd = { "cmd", "/c", "set" };
      return cmd;
    }
    else if (Os.isFamily(Os.WINDOWS_9X_FAMILY)) {

      String[] cmd = { "command.com", "/c", "set" };
      return cmd;
    }
    else if (Os.isFamily(Os.UNIX_FAMILY)) {
      String[] cmd = { "/bin/sh", "-c", "env" };
      // String[] cmd = { "/usr/bin/env" };
      return cmd;
    }
    return new String[0];
  }
}
