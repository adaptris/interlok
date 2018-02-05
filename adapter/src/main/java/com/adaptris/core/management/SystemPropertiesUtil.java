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
import static com.adaptris.core.util.PropertyHelper.getPropertySubset;

import java.util.Properties;

import javax.naming.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.management.properties.PropertyResolver;

/**
 * Set additional system properties from properties stored in the bootstrap.properties file.
 * <p>
 * Any property prefixed by {@link Constants#SYSTEM_PROPERTY_PREFIX} will be added as a system property (the prefix will be removed
 * first). If the property value starts with a '{scheme}' then the {@link com.adaptris.core.management.properties.Decoder}
 * implementation that matches the scheme will be used to decode the property before adding it
 * <p/>
 * <code>
 * <pre>
 sysprop.plaintext=plaintext
 sysprop.encrypted={password}PW:AAAAEDNPp8M3xBUiU+goN1cmjBYAAAAQorWHploKWvTb5bmjjgiCWQAAABCa6cnOef76qd67FXsgN4nV
 * </pre>
 * </code>
 * <p>
 * In the above example the 'encrypted' property will be decrypted using a password decoder before System.setProperty('encrypted')
 * is invoked
 * </p>
 *
 * @author gcsiki
 *
 */
public class SystemPropertiesUtil {

  public static final String NAMING_PACKAGE = "com.adaptris.naming";
  
  private static Logger log = LoggerFactory.getLogger(BootstrapProperties.class);

  /**
   * Add a subset of the properties parameter as system properties.
   *
   * @param p properties that might contain system properties.
   */
  public static void addSystemProperties(Properties p) {
    try {
      Properties sysProps = getPropertySubset(p, Constants.SYSTEM_PROPERTY_PREFIX, true);
      if (sysProps.size() > 0) {
        PropertyResolver resolver = PropertyResolver.getDefaultInstance();
        for (String keyWithPrefix : sysProps.stringPropertyNames()) {
          String syspropKey = keyWithPrefix.substring(Constants.SYSTEM_PROPERTY_PREFIX.length());
          String toBeResolved = sysProps.getProperty(keyWithPrefix);
          String syspropValue = resolver.resolve(toBeResolved);
          if (log.isTraceEnabled()) {
            if (syspropValue.equals(toBeResolved)) {
              log.trace("Adding " + syspropKey + "=" + toBeResolved + " to system properties");

            }
            else {
              log.trace("Adding " + syspropKey + "=PropertyResolver#resolve(" + toBeResolved + ") to system properties");
            }
          }
          System.setProperty(syspropKey, syspropValue);
        }
      }
    }
    catch (Exception ex) {
      log.error("Error during setting system properties", ex);
    }
  }
  
  public static void addJndiProperties(Properties bootstrapProperties) {
    boolean enableJndi = BootstrapProperties.isEnabled(bootstrapProperties, Constants.CFG_KEY_JNDI_SERVER);
    if (enableJndi) {
      Properties sysProps = System.getProperties();
      String property = sysProps.getProperty(Context.URL_PKG_PREFIXES);
      if (property == null) System.setProperty(Context.URL_PKG_PREFIXES, NAMING_PACKAGE);
      else
        System.setProperty(Context.URL_PKG_PREFIXES, property + ":" + NAMING_PACKAGE);
    }
  }

}
