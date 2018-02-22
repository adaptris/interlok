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


public abstract class CommandLineArgs {

  protected Map<String, String> dashArgs;
  protected List<String> normalArgs;

  protected CommandLineArgs(String[] args) throws Exception {
    parseArguments(args);
  }

  protected CommandLineArgs(Map<String, String> dashedArgs, List normalArgs) {
    this.dashArgs = dashedArgs;
    this.normalArgs = normalArgs;
  }

  protected abstract void parseArguments(String[] args) throws Exception;

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
    for (String p : paramList) {
      if (dashArgs.containsKey(p)) {
        rs = dashArgs.get(p);
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
    for (Map.Entry<String, String> entry : dashArgs.entrySet()) {
      result.add(entry.getKey());
      result.add(entry.getValue());
    }
    for (String s : normalArgs) {
      result.add(s);
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
    Map<String, String> copy = new HashMap<>(dashArgs);
    for (String arg : args) {
      copy.remove(arg);
    }
    return new CommandLineArgs(copy, normalArgs) {
      @Override
      protected void parseArguments(String[] args) {
        throw new IllegalStateException();
      }
      
    };
  }

  /**
   * Converts argument from <code>--key value</code> to <code>value</code>.
   *
   * @param args
   * @return a new instance.
   */
  public CommandLineArgs convertToNormal(String... args) {
    Map<String, String> copy = new HashMap<>(dashArgs);
    String value = getArgument(args);
    for (String arg : args) {
      copy.remove(arg);
    }
    if (value != null && !value.equals(Boolean.TRUE.toString())){
      normalArgs.add(0, value);
    }
    return new CommandLineArgs(copy, normalArgs) {
      @Override
      protected void parseArguments(String[] args) {
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
    CommandLineArgs a = new DefaultImpl(argv);
    return a;
  }

  private static final class DefaultImpl extends CommandLineArgs {

    public DefaultImpl(String[] args) throws Exception {
      super(args);
    }

    protected void parseArguments(String[] s) throws Exception {

      dashArgs = new HashMap<>();
      normalArgs = new ArrayList<>();
      if (s == null) {
        return;
      }
      for (int i = 0; i < s.length; i++) {
        if (s[i].startsWith("-")) {
          dashArgs.put(s[i], Boolean.TRUE.toString());
          int j = i + 1;
          if (j < s.length) {
            if (!s[j].startsWith("-")) {
              dashArgs.put(s[i], s[j]);
              i = j;
            }
          }
        }
        else {
          normalArgs.add(s[i]);
        }

      }
      return;
    }
  }
}
