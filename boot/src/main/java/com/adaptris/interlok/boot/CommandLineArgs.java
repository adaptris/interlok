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

package com.adaptris.interlok.boot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class CommandLineArgs {

  protected Map<String, String> argumentHash;

  protected CommandLineArgs(String[] args) throws Exception {
    argumentHash = parseArguments(args);
  }

  protected CommandLineArgs(Map<String, String> args) {
    argumentHash = args;
  }

  protected abstract Map<String, String> parseArguments(String[] args) throws Exception;

  /**
   * Determine if the listed parameters are contained in the commandline
   * arguments.
   * @return true if the argument list contains these parameters
   * @param paramList the list of parameters.
   */
  public boolean hasArgument(String... paramList) {
    return this.getArgument(paramList) != null;
  }

  /**
   * Get the argument specified.
   *
   * @param paramList the argument specified.
   * @return the argument or null
   */
  public String getArgument(String... paramList) {
    String rs = null;
    for (int i = 0; i < paramList.length; i++) {
      if (argumentHash.containsKey(paramList[i])) {
        rs = argumentHash.get(paramList[i]);
        break;
      }
    }
    return rs;

  }

  /**
   * Render the arguments for passing into another {@code main} method.
   * <p>
   * Normally used in conjunction with {@link #remove(String...)} so that you strip off the commandline args that you have already
   * processed.
   * </p>
   * 
   * @return an array of Strings
   * @see #remove(String...)
   */
  public String[] render() {
    List<String> result = new ArrayList<>();
    for (Map.Entry<String, String> entry : argumentHash.entrySet()) {
      result.add(entry.getKey());
      result.add(entry.getValue());
    }
    return result.toArray(new String[0]);
  }

  /**
   * Remove arguments.
   * 
   * @param args
   * @return a new instance.
   */
  public CommandLineArgs remove(String... args) {
    Map<String, String> copy = new HashMap<>(argumentHash);
    for (String arg : args) {
      copy.remove(arg);
    }
    return new CommandLineArgs(copy) {
      @Override
      protected Map<String, String> parseArguments(String[] args) {
        throw new IllegalStateException();
      }
      
    };
  }

  /**
   * Parse the arguments
   *
   * @param argv the list of commandline arguments.
   * @return the default argument processor
   * @throws Exception on error.
   */
  public static CommandLineArgs parse(String[] argv) throws Exception {
    CommandLineArgs a = new JdkRegexpImpl(argv);
    return a;
  }

  private static final class JdkRegexpImpl extends CommandLineArgs {

    public JdkRegexpImpl(String[] args) throws Exception {
      super(args);
    }

    protected Map<String, String> parseArguments(String[] s) throws Exception {

      Map<String, String> argHash = new HashMap<>();
      if (s == null) {
        return argHash;
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
      return argHash;
    }
  }
}
