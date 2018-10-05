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

package com.adaptris.core.management.properties;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves any properties that are stored using a scheme and decodes using the specified {@link Decoder} implementation.
 *
 * <p>
 * At the moment only one scheme {password} is registered, which will be resolved using {@link PasswordDecoder}. If the system
 * property is represented like this:
 * </p>
 * <code>
 * <pre>
 sysprop.encrypted.property={password}PW:AAAAEDNPp8M3xBUiU+goN1cmjBYAAAAQorWHploKWvTb5bmjjgiCWQAAABCa6cnOef76qd67FXsgN4nV
 * </pre>
 * </code> then the property 'encrypted.property' will actually have the plain text value of the encoded value associated with it
 * when <code>System.getProperty("encrypted.property")</code> is called after full initialisation occurs.
 *
 * @author gcsiki
 * @see PasswordDecoder
 */
public abstract class PropertyResolver {
  private static PropertyResolver defaultInstance;

  private static transient Logger log = LoggerFactory.getLogger(PropertyResolver.class);
  private static final String MAPPING_FILE = "META-INF/com/adaptris/core/management/properties/resolver";

  // Matching {password}PW:BLAHBLAH, but reluctantly.
  private static final String PROPERTY_RESOLVE_PATTERN = "^\\{(.*?)\\}(.*)$";

  public PropertyResolver() {
  }

  /**
   * Convenience method to get a default instance of the PropertyResolver.
   * 
   * @return an already initialised default instance.
   * @throws Exception on exception
   */
  public static synchronized PropertyResolver getDefaultInstance() throws Exception {
    if (defaultInstance == null) {
      defaultInstance = new DefaultPropertyResolver();
      defaultInstance.init();
    }
    return defaultInstance;
  }

  /**
   * Initialises the PropertyResolver
   *
   */
  public abstract void init() throws Exception;

  /**
   * If the property needs decoding, then this method returns the decoded property. If the property is not encoded, then it simply
   * returns the same value.
   * 
   * @param s - The current value for the property.
   * @return The decoded property.
   * @throws Exception
   */
  public abstract String resolve(String s) throws Exception;


  private static class DefaultPropertyResolver extends PropertyResolver {
    private Map<String, Decoder> schemes;
    private Pattern resolverPattern;

    DefaultPropertyResolver() {
      schemes = new HashMap<String, Decoder>();
      resolverPattern = Pattern.compile(PROPERTY_RESOLVE_PATTERN);
    }

    @Override
    public void init() throws Exception {
      Enumeration<URL> mappings = this.getClass().getClassLoader().getResources(MAPPING_FILE);
      while (mappings.hasMoreElements()) {
        init(mappings.nextElement());
      }
      if (log.isTraceEnabled()) {
        Properties p = new Properties();
        for (Iterator i = schemes.keySet().iterator(); i.hasNext();) {
          String key = (String) i.next();
          Decoder d = schemes.get(key);
          p.put(key, d.getClass().getCanonicalName());
        }
        log.trace("Registered Decoders : " + p);
      }
    }

    private void init(URL url) throws Exception {
      log.trace("Parsing PropertyResolver URL [{}]", url);
      try (InputStream in = url.openStream()) {
        Properties p = new Properties();
        p.load(in);
        initSchemes(p);
      }
    }

    private void initSchemes(Properties p) throws Exception {
      for (Iterator i = p.keySet().iterator(); i.hasNext();) {
        String key = (String) i.next();
        String clazz = p.getProperty(key);
        schemes.put(key, (Decoder) Class.forName(clazz).newInstance());
      }
    }

    @Override
    public String resolve(String propertyValue) throws Exception {
      String result = propertyValue;
      Matcher m = resolverPattern.matcher(defaultIfEmpty(propertyValue, ""));
      if (m.matches()) {
        String scheme = m.group(1);
        String value = m.group(2);
        if (schemes.containsKey(scheme)) {
          Decoder transformer = schemes.get(scheme);
          result = transformer.decode(value);
        }
      }
      return result;
    }
  }
}
