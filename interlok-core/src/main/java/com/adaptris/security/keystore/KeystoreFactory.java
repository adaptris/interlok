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

package com.adaptris.security.keystore;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.exc.KeystoreException;
import com.adaptris.security.util.Constants;

/**
 * Factory for creating Keystore Proxy objects.
 * <p>
 * There should be no real reason to replace the default factory implementation,
 * however should use wish to, you should set the the keystore factory class
 * system property with your own KeystoreFactory implementation class. This
 * class should have a no param public constructor.
 * </p>
 *
 * @see #KEYSTORE_FACTORY_CLASS
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class KeystoreFactory {

  private static KeystoreFactory defaultFactory = null;
  private static transient Logger logR = LoggerFactory.getLogger(KeystoreFactory.class);

  /**
   * The System Property key that controls the default keystore factory
   * implementation
   */
  public static final String KEYSTORE_FACTORY_CLASS = "adp.default.keystore."
      + "factory.impl";

  private static final String[] STANDARD_TYPES =
  {
      "JKS", "JCEKS", "BKS"
  };

  private static final List<String> STANDARD_TYPES_LIST = Arrays.asList(STANDARD_TYPES);

  private static final String[] CUSTOM_TYPES =
  {
      Constants.KEYSTORE_X509,
      Constants.KEYSTORE_XMLKEYINFO
  };

  private static final Class<?>[] CUSTOM_TYPES_IMPS =
  {
      X509KeystoreProxy.class,
      XmlKeyInfoKeystoreProxy.class
  };

  private static final Map<String, Class<?>> CUSTOM_TYPES_MAP;

  static {
    Map<String, Class<?>> ht = new Hashtable<>();
    for (int i = 0; i < CUSTOM_TYPES.length; i++) {
      ht.put(CUSTOM_TYPES[i], CUSTOM_TYPES_IMPS[i]);
    }
    CUSTOM_TYPES_MAP = Collections.unmodifiableMap(ht);
  }

  /**
   * @see Object#Object()
   *
   *
   */
  public KeystoreFactory() {
  }

  /**
   * Get the default instance of the factory.
   *
   * @return the factory
   */
  public static synchronized KeystoreFactory getDefault() {
    if (defaultFactory == null) {
      if (System.getProperties().containsKey(KEYSTORE_FACTORY_CLASS)) {
        String cls = System.getProperty(KEYSTORE_FACTORY_CLASS);
        try {
          defaultFactory =
              (KeystoreFactory) Class.forName(cls).getDeclaredConstructor().newInstance();
        }
        catch (Exception e) {
          logR.warn("Failed to create custom keystore factory " + cls
              + ", reverting to default");
          defaultFactory = new Factory();
        }
      }
      else {
        defaultFactory = new Factory();
      }
    }
    return defaultFactory;
  }

  /**
   * Create a KeyStoreLocation instance.
   *
   * @param url the url
   * @param password the password to access the keystore.
   * @return the keystore info object.
   * @throws AdaptrisSecurityException wrapping any underlying exception.
   * @see #create(String)
   */
  public abstract KeystoreLocation create(String url, char[] password)
      throws AdaptrisSecurityException;

  /**
   * Create a KeyStoreLocation instance.
   * <p>
   * Valid URLS are in the form <br />
   * <code>[protocol]://[server]:[port]/[path]?keystoreType=[type]&amp;
   * keystorePassword=[password]</code>
   * <ul>
   * <li>protocol - the protocol to use, e.g. http</li>
   * <li>server - the server hosting the keystore</li>
   * <li>port - the port through which the protocol will operate</li>
   * <li>path - Path to the keystore</li>
   * <li>type - The type of keystore to use (e.g. JKS) </li>
   * <li>password - password to the keystore</li>
   * </ul>
   * </p>
   * <p>
   * An example would be <code>http://www.adaptris.com/my.ks?keystoreType=JKS&amp;
   * keystorePassword=ABCDE</code>
   * or <code>file://localhost/c:/my.ks?keystoreType=JKS&amp;
   * keystorePassword=ABCDE</code>
   * </p>
   * @param url the url that contains the keystore. They keystore is required to
   *          contain all the information (including the password).
   * @return the keystore info object.
   * @throws AdaptrisSecurityException wrapping any underlying exception.
   * @see #create(String, char[])
   * @see KeystoreProxy
   */
  public abstract KeystoreLocation create(String url)
      throws AdaptrisSecurityException;

  /**
   * Create a KeystoreProxy instance.
   *
   * @param loc the location of the keystore
   * @return a KeystoreProxy.
   * @throws AdaptrisSecurityException wrapping the underlying exception
   * @see KeystoreLocation
   */
  public abstract KeystoreProxy create(KeystoreLocation loc)
      throws AdaptrisSecurityException;

  private static class Factory extends KeystoreFactory {

    Factory() {
      super();
    }

    /**
     *
     * @see KeystoreFactory#create(java.lang.String, char[])
     */
    @Override
    public KeystoreLocation create(String url, char[] password)
        throws AdaptrisSecurityException {
      KeystoreLocation ksi = null;
      String formattedUrl = "";
      try {
        formattedUrl = url.replaceAll("\\\\", "/");
        URI uri = new URI(formattedUrl);
        Properties props = convertToProperties(uri.getQuery());
        if (STANDARD_TYPES_LIST.contains(uri.getScheme().toUpperCase())) {
          logR.warn(formattedUrl + " is deprecated, file:///" + uri.getPath()
              + "?" + Constants.KEYSTORE_TYPE + "="
              + uri.getScheme().toUpperCase() + " is preferred.");
          ksi = new LocalKeystore(uri.getScheme(), uri.getPath(), password,
              props);
        }
        else {
          String type = props.getProperty(Constants.KEYSTORE_TYPE);
          if (type == null) {
            throw new KeystoreException(url + " missing a type");
          }
          if (uri.getScheme() == null
              || "file".equalsIgnoreCase(uri.getScheme())) {
            ksi = new LocalKeystore(type, uri.getPath(), password, props);
          }
          else {
            ksi = new RemoteUrlKeystore(type, url, password, props);
          }
        }
      }
      catch (AdaptrisSecurityException e) {
        throw e;
      }
      catch (Exception e) {
        throw new KeystoreException("Could not parse url [" + formattedUrl
            + "]", e);
      }
      logR.debug("Created keystore location " + ksi);
      return ksi;
    }

    /**
     *
     * @see KeystoreFactory#create(java.lang.String)
     */
    @Override
    public KeystoreLocation create(String urlString)
        throws AdaptrisSecurityException {
      String pw = "", formattedUrl;
      try {
        formattedUrl = urlString.replaceAll("\\\\", "/");
        URI uri = new URI(formattedUrl);
        Properties props = convertToProperties(uri.getQuery());
        pw = props.getProperty(Constants.KEYSTORE_PASSWORD, "");
      }
      catch (Exception e) {
        throw new KeystoreException("Could not parse url [" + urlString + "]",
            e);
      }
      // logR.debug("[" + formattedUrl + "][" + pw + "]");
      return create(formattedUrl, pw.toCharArray());
    }

    /**
     *
     * @see KeystoreFactory#create(KeystoreLocation)
     */
    @Override
    public KeystoreProxy create(KeystoreLocation ksl)
        throws AdaptrisSecurityException {
      Class<?> clazz = CUSTOM_TYPES_MAP.get(ksl.getKeyStoreType()
          .toUpperCase());
      KeystoreProxy kp = null;
      if (clazz == null) {
        kp = new KeystoreProxyImp(ksl);
      }
      else {
        try {
          kp = (KeystoreProxy) clazz.getDeclaredConstructor().newInstance();
          kp.setKeystoreLocation(ksl);
        }
        catch (Exception e) {
          throw new KeystoreException("Failed to keystore proxy that handles "
              + ksl.getKeyStoreType(), e);
        }
      }
      return kp;
    }

    private Properties convertToProperties(String query) {
      Properties result = new Properties();
      if (query == null) {
        return result;
      }
      String[] tokens = query.split("&");
      for (String token : tokens) {
        String[] values = token.split("=");
        result.setProperty(values[0], values[1]);
      }
      return result;
    }
  }
}
