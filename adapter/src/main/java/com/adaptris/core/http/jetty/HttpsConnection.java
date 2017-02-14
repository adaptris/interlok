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

package com.adaptris.core.http.jetty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Concrete implementation of JettyConnection that allows HTTPs traffic.
 * 
 * <p>
 * By default all fields are left as null. This will cause Jetty to use its internal defaults. Consult the Jetty Documentation for
 * information on the behaviour and configuration required.
 * </p>
 * <p>
 * The key from the <code>ssl-properties</code> element should match the name of the underlying {@link SslContextFactory} setter.
 * 
 * <pre>
 * {@code 
 *   <ssl-properties>
 *     <key-value-pair>
 *        <key>WantClientAuth</key>
 *        <value>true</value>
 *     </key-value-pair>
 *   </ssl-properties>
 * }
 * </pre>
 * will invoke {@link SslContextFactory#setWantClientAuth(boolean)}, setting the WantClientAuth property to true. Note that no
 * validation of the various properties is performed and will be passed as-is to the {@link SslContextFactory} with an attempt to
 * transform into the correct type. Invalid combinations may result in undefined behaviour.
 * </p>
 * 
 * @config jetty-https-connection
 * 
 * @author lchan
 * @see JettyConnection
 */
@XStreamAlias("jetty-https-connection")
@AdapterComponent
@ComponentProfile(summary = "Connection that creates its own jetty engine instance and listens on the specified port",
    tag = "connections,https,jetty")
@DisplayOrder(order =
{
    "port", "httpConfiguration", "serverConnectorProperties", "sslProperties"
})
public class HttpsConnection extends HttpConnection {
  /**
   * Properties for {@link SslContextFactory}.
   * 
   * 
   */
  public enum SslProperty {
    
    /**
     * 
     * @see SslContextFactory#setKeyStorePath(String)
     */
    KeyStorePath {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setKeyStorePath(value);
      }
    },
    /**
     * Set the keystore password (may be encoded using {@link com.adaptris.security.password.Password}.
     * <p>
     * Note that the password may also be obfuscated using the internal jetty utility
     * {@link org.eclipse.jetty.util.security.Password} using one of the reversible schemes.
     * </p>
     * 
     * @see SslContextFactory#setKeyStorePassword(String)
     */
    KeyStorePassword {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) throws PasswordException {
        sslContextFactory.setKeyStorePassword(Password.decode(value));
      }
    },
    /**
     * 
     * @see SslContextFactory#setKeyStoreType(String)
     */
    KeyStoreType {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setKeyStoreType(value);
      }
    },
    /**
     * @see SslContextFactory#setKeyStoreProvider(String)
     */
    KeyStoreProvider {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setKeyStoreProvider(value);
      }
    },
    /**
     * 
     * @see SslContextFactory#setTrustStore(String)
     */
    TrustStorePath {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setTrustStorePath(value);
      }
    },
    /**
     * Set the truststore password (may be encoded using {@link com.adaptris.security.password.Password} .
     * <p>
     * Note that the password may also be obfuscated using the internal jetty utility
     * {@link org.eclipse.jetty.util.security.Password} using one of the reversible schemes.
     * </p>
     * 
     * @see SslContextFactory#setTrustStorePassword(String)
     */
    TrustStorePassword {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) throws PasswordException {
        sslContextFactory.setTrustStorePassword(Password.decode(value));
      }
    },
    /**
     * 
     * @see SslContextFactory#setTrustStoreType(String)
     */
    TrustStoreType {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setTrustStoreType(value);
      }
    },
    /**
     * @see SslContextFactory#setTrustStoreProvider(String)
     */
    TrustStoreProvider {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setTrustStoreProvider(value);
      }
    },
    /**
     * Set the key manager password (may be encoded using {@link com.adaptris.security.password.Password} .
     * <p>
     * Note that the password may also be obfuscated using the internal jetty utility
     * {@link org.eclipse.jetty.util.security.Password} using one of the reversible schemes.
     * </p>
     * 
     * @see SslContextFactory#setKeyManagerPassword(String)
     */
    KeyManagerPassword {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) throws PasswordException {
        sslContextFactory.setKeyManagerPassword(Password.decode(value));
      }
    },
    
    /**
     * A comma separated list of cipher suites to exclude.
     * 
     * @see SslContextFactory#setExcludeCipherSuites(String...)
     */
    ExcludeCipherSuites {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setExcludeCipherSuites(asArray(value));
      }
    },
    /**
     * A comma separated list of cipher suites to include.
     * 
     * @see SslContextFactory#setIncludeCipherSuites(String...)
     */
    IncludeCipherSuites {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setIncludeCipherSuites(asArray(value));
      }
    },
    /**
     * A comma separated list of protocols to exclude
     * 
     * @see SslContextFactory#setExcludeProtocols(String...)
     */
    ExcludeProtocols {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setExcludeProtocols(asArray(value));
      }
    },
    /**
     * A comma separated list of protocols to include
     * 
     * @see SslContextFactory#setIncludeProtocols(String...)
     */
    IncludeProtocols {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setIncludeProtocols(asArray(value));
      }
    },
    /**
     * 
     * @see SslContextFactory#setSecureRandomAlgorithm(String)
     */
    SecureRandomAlgorithm {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setSecureRandomAlgorithm(value);
      }
    },
    /**
     * @see SslContextFactory#setSessionCachingEnabled(boolean)
     */
    SessionCachingEnabled {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setSessionCachingEnabled(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see SslContextFactory#setMaxCertPathLength(int)
     */
    MaxCertPathLength {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setMaxCertPathLength(Integer.parseInt(value));
      }
    },
    /**
     * @see SslContextFactory#setNeedClientAuth(boolean)
     */
    NeedClientAuth {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setNeedClientAuth(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see SslContextFactory#setOcspResponderURL(String)
     */
    OcspResponderURL {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setOcspResponderURL(value);
      }
    },
    /**
     * @see SslContextFactory#setSslSessionTimeout(int)
     */
    SslSessionTimeout {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setSslSessionTimeout(Integer.parseInt(value));
      }
    },
    /**
     * @see SslContextFactory#setTrustAll(boolean)
     */
    TrustAll {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setTrustAll(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see SslContextFactory#setTrustManagerFactoryAlgorithm(String)
     */
    TrustManagerFactoryAlgorithm {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setTrustManagerFactoryAlgorithm(value);
      }
    },
    /**
     * @see SslContextFactory#setValidateCerts(boolean)
     */
    ValidateCerts {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setValidateCerts(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see SslContextFactory#setValidatePeerCerts(boolean)
     */
    ValidatePeerCerts {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setValidatePeerCerts(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * 
     * @see SslContextFactory#setWantClientAuth(boolean)
     */
    WantClientAuth {
      @Override
      void applyProperty(SslContextFactory sslContextFactory, String value) {
        sslContextFactory.setWantClientAuth(Boolean.valueOf(value).booleanValue());
      }
    };

    abstract void applyProperty(SslContextFactory sslContextFactory, String value) throws Exception;
    
  }

  @NotNull
  @Valid
  @AutoPopulated
  @AdvancedConfig
  private KeyValuePairSet sslProperties;

  public HttpsConnection() {
    super();
    setPort(8443);
    setSslProperties(new KeyValuePairSet());
  }


  @Override
  protected ConnectionFactory[] createConnectionFactory() throws Exception {
    HttpConfiguration httpsConfig = new HttpConfiguration(createConfig());
    httpsConfig.addCustomizer(new SecureRequestCustomizer());
    return new ConnectionFactory[] { 
        new SslConnectionFactory(createSslContext(), HttpVersion.HTTP_1_1.asString()),
        new HttpConnectionFactory(httpsConfig)
    };
  }

  SslContextFactory createSslContext() throws Exception {
    SslContextFactory sslContextFactory = new SslContextFactory();
    for (KeyValuePair kvp : getSslProperties().getKeyValuePairs()) {
      boolean matched = false;
      for (SslProperty sp : SslProperty.values()) {
        if (kvp.getKey().equalsIgnoreCase(sp.toString())) {
          sp.applyProperty(sslContextFactory, kvp.getValue());
          matched = true;
          break;
        }
      }
      if (!matched) {
        log.trace("Ignoring unsupported Property " + kvp.getKey());
      }
    }
    return sslContextFactory;
  }

  public KeyValuePairSet getSslProperties() {
    return sslProperties;
  }

  /**
   * Set the SSL properties.
   * 
   * @param kvps the SSL properties
   * @see SslProperty
   */
  public void setSslProperties(KeyValuePairSet kvps) {
    this.sslProperties = kvps;
  }
}
