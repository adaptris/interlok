/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.interlok.resolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolver implementation that resolves from {@link System#getenv()}.
 * <p>
 * This resolver resolves values based on the associated environment variable. e.g. {@code %env{HOSTNAME}} will return the value of
 * the environment variable {@code HOSTNAME}. In the event that {@code HOSTNAME} is not defined, then the variable will be returned
 * (in this case the literal value 'HOSTNAME').
 * </p>
 */
public class FromEnvironment extends ResolverImp {

  // Should match %env{MY_ENV_VAR}%env{MY_ENV_VAR} etc.
  private static final String RESOLVE_REGEXP = "^.*%env\\{([\\w!\\$\"#&'\\*\\+,\\-\\.:=]+)\\}.*$";
  private transient Pattern resolverPattern;


  public FromEnvironment() {
    resolverPattern = Pattern.compile(RESOLVE_REGEXP);
  }

  @Override
  public String resolve(String s) {
    if (s == null) {
      return null;
    }
    String result = s;
    log.trace("Resolving {} from environment variables", s);
    Matcher m = resolverPattern.matcher(s);
    while (m.matches()) {
      String key = m.group(1);
      log.trace("Resolve on {} ", key);
      String envValue = resolve(key, System.getenv());
      String toReplace = "%env{" + key + "}";
      result = result.replace(toReplace, envValue);
      m = resolverPattern.matcher(result);
    }
    return result;
  }

  @Override
  public boolean canHandle(String value) {
    return resolverPattern.matcher(value).matches();
  }
}
