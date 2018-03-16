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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolver implementation that resolves from {@link System#getProperties()}. *
 * <p>
 * This resolver resolves values based on the associated system property; e.g. {@code %env{my.sysprop}} will return the value of the
 * system property{@code my.sysprop}. In the event that {@code my.sysprop} is not defined, then the property name will be returned
 * (in this case the literal value 'my.sysprop').
 * </p>
 */
public class FromSystemProperties extends ResolverImp {

  // Should match %sysprop{MY.SYSTEM.PROPERTY}%sysprop{MY_OTHER_SYSTEM_PROPERTY} etc.
  private static final String RESOLVE_REGEXP = "^.*%sysprop\\{([\\w!\\$\"#&'\\*\\+,\\-\\.:=]+)\\}.*$";
  private transient Pattern resolverPattern;


  public FromSystemProperties() {
    resolverPattern = Pattern.compile(RESOLVE_REGEXP);
  }

  @Override
  public String resolve(String s) {
    if (s == null) {
      return null;
    }
    String result = s;
    log.trace("Resolving {} from system properties", s);
    Matcher m = resolverPattern.matcher(s);
    Map<String, String> properties = asMap(System.getProperties());
    while (m.matches()) {
      String key = m.group(1);
      log.trace("Resolve on {} ", key);
      String syspropValue = resolve(key, properties);
      String toReplace = "%sysprop{" + key + "}";
      result = result.replace(toReplace, syspropValue);
      m = resolverPattern.matcher(result);
    }
    return result;
  }

  @Override
  public boolean canHandle(String value) {
    return resolverPattern.matcher(value).matches();
  }
}
