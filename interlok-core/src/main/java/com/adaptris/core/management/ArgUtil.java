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

package com.adaptris.core.management;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Utility for parsing unix style arguments.
 * @author lchan
 * @author $Author: hfraser $
 */
public abstract class ArgUtil {

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ArgUtil() {
  }
  /**
   * Determine if the listed parameters are contained in the commandline
   * arguments.
   * @return true if the argument list contains these parameters
   * @param paramList the list of parameters.
   */
  public abstract boolean hasArgument(String[] paramList);

  /**
   * Get the argument specified.
   *
   * @param paramList the argument specified.
   * @return the argument or null
   */
  public abstract String getArgument(String[] paramList);

  /**
   * Get the default implementation of the argument processor.
   *
   * @param argv the list of commandline arguments.
   * @return the default argument processor
   * @throws Exception on error.
   */
  public static ArgUtil getInstance(String[] argv) throws Exception {
    ArgUtil a = new JdkRegexpImpl(argv);
    return a;
  }

  /**
   * Convenience method to get the arguments.
   *
   * @param argv the list of arguments.
   * @param paramList the parameter list to match
   * @return the matching argument or null if not found.
   */
  public static String getArgument(String[] argv, String[] paramList) {

    String rs = null;
    try {
      ArgUtil a = new JdkRegexpImpl(argv);
      rs = a.getArgument(paramList);
    }
    catch (Exception e) {
      ;
    }
    return rs;
  }

  /**
   * Convenience method to query the argument list.
   *
   * @param argv the list of arguments.
   * @param paramList the parameter list to match
   * @return true if there was a match between argv and paramList
   */
  public static boolean hasArgument(String[] argv, String[] paramList) {

    boolean rc = false;
    try {
      ArgUtil a = new JdkRegexpImpl(argv);
      rc = a.hasArgument(paramList);
    }
    catch (Exception e) {
      ;
    }
    return rc;
  }


  private static final class JdkRegexpImpl extends ArgUtil {

    private HashMap argHash;

    private JdkRegexpImpl(String[] argv) throws Exception {
      super();
      parseArguments(argv);
    }

    @Override
    public String getArgument(String[] paramList) {

      String rs = null;
      for (int i = 0; i < paramList.length; i++) {
        if (argHash.containsKey(paramList[i])) {
          rs = argHash.get(paramList[i]).toString();
          break;
        }
      }
      return rs;
    }

    @Override
    public boolean hasArgument(String[] paramList) {
      return this.getArgument(paramList) != null;
    }

    private void parseArguments(String[] s) throws Exception {
      argHash = new HashMap();
      if (s == null) {
        return;
      }
      String pat = "^\\-{1}[\\S]+";
      Pattern pattern = Pattern.compile(pat);
      for (int i = 0; i < s.length; i++) {
        Matcher first = pattern.matcher(s[i]);
        if (first.matches()) {
          argHash.put(s[i], Boolean.TRUE.toString());
          int j = i + 1;
          if (j < s.length) {
            Matcher second = pattern.matcher(s[j]);
            if (!second.matches()) {
              argHash.put(s[i], s[j]);
            }
          }
        }
      }
    }
  }
}
