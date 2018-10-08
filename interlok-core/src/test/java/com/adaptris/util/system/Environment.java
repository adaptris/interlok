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
