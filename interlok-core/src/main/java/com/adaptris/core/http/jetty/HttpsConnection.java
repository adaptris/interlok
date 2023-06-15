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
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.SimpleBeanUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

/**
 * Concrete implementation of JettyConnection that allows HTTPs traffic.
 *
 * <p>
 * By default all fields are left as null. This will cause Jetty to use its internal defaults. Consult the Jetty Documentation for
 * information on the behaviour and configuration required.
 * </p>
 * <p>
 * The key from the <code>ssl-properties</code> element should match the name of the underlying {@link SslContextFactory.Server} setter.
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
 *
 * will invoke {@link SslContextFactory.Server#setWantClientAuth(boolean)}, setting the WantClientAuth property to true. Note that no
 * validation of the various properties is performed and will be passed as-is to the {@link SslContextFactory.Server} with an attempt to
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
@ComponentProfile(summary = "Connection that creates its own jetty engine instance and listens on the specified port", tag = "connections,https,jetty")
@DisplayOrder(order = { "port", "httpConfiguration", "serverConnectorProperties", "sslProperties" })
public class HttpsConnection extends HttpConnection {
  /**
   * Properties for {@link SslContextFactory}.
   *
   *
   */
  public enum SslProperty {

    /**
     *
     * @see SslContextFactory.Server#setKeyStorePath(String)
     */
    KeyStorePath {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setKeyStorePath(value);
      }
    },
    /**
     * Set the keystore password (may be encoded using {@link com.adaptris.security.password.Password}.
     * <p>
     * Note that the password may also be obfuscated using the internal jetty utility {@link org.eclipse.jetty.util.security.Password} using
     * one of the reversible schemes.
     * </p>
     *
     * @see SslContextFactory.Server#setKeyStorePassword(String)
     */
    KeyStorePassword {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) throws PasswordException {
        server.setKeyStorePassword(Password.decode(value));
      }
    },
    /**
     *
     * @see SslContextFactory.Server#setKeyStoreType(String)
     */
    KeyStoreType {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setKeyStoreType(value);
      }
    },
    /**
     * @see SslContextFactory.Server#setKeyStoreProvider(String)
     */
    KeyStoreProvider {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setKeyStoreProvider(value);
      }
    },
    /**
     *
     * @see SslContextFactory.Server#setTrustStorePath(String)
     */
    TrustStorePath {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setTrustStorePath(value);
      }
    },
    /**
     * Set the truststore password (may be encoded using {@link com.adaptris.security.password.Password} .
     * <p>
     * Note that the password may also be obfuscated using the internal jetty utility {@link org.eclipse.jetty.util.security.Password} using
     * one of the reversible schemes.
     * </p>
     *
     * @see SslContextFactory.Server#setTrustStorePassword(String)
     */
    TrustStorePassword {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) throws PasswordException {
        server.setTrustStorePassword(Password.decode(value));
      }
    },
    /**
     *
     * @see SslContextFactory.Server#setTrustStoreType(String)
     */
    TrustStoreType {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setTrustStoreType(value);
      }
    },
    /**
     * @see SslContextFactory.Server#setTrustStoreProvider(String)
     */
    TrustStoreProvider {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setTrustStoreProvider(value);
      }
    },
    /**
     * Set the key manager password (may be encoded using {@link com.adaptris.security.password.Password} .
     * <p>
     * Note that the password may also be obfuscated using the internal jetty utility {@link org.eclipse.jetty.util.security.Password} using
     * one of the reversible schemes.
     * </p>
     *
     * @see SslContextFactory.Server#setKeyManagerPassword(String)
     */
    KeyManagerPassword {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) throws PasswordException {
        server.setKeyManagerPassword(Password.decode(value));
      }
    },

    /**
     * A comma separated list of cipher suites to exclude.
     *
     * @see SslContextFactory.Server#setExcludeCipherSuites(String...)
     */
    ExcludeCipherSuites {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setExcludeCipherSuites(asArray(value));
      }
    },
    /**
     * A comma separated list of cipher suites to include.
     *
     * @see SslContextFactory.Server#setIncludeCipherSuites(String...)
     */
    IncludeCipherSuites {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setIncludeCipherSuites(asArray(value));
      }
    },
    /**
     * A comma separated list of protocols to exclude
     *
     * @see SslContextFactory.Server#setExcludeProtocols(String...)
     */
    ExcludeProtocols {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setExcludeProtocols(asArray(value));
      }
    },
    /**
     * A comma separated list of protocols to include
     *
     * @see SslContextFactory.Server#setIncludeProtocols(String...)
     */
    IncludeProtocols {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setIncludeProtocols(asArray(value));
      }
    },
    /**
     *
     * @see SslContextFactory.Server#setSecureRandomAlgorithm(String)
     */
    SecureRandomAlgorithm {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setSecureRandomAlgorithm(value);
      }
    },
    /**
     * @see SslContextFactory.Server#setSessionCachingEnabled(boolean)
     */
    SessionCachingEnabled {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setSessionCachingEnabled(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see SslContextFactory.Server#setMaxCertPathLength(int)
     */
    MaxCertPathLength {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setMaxCertPathLength(Integer.parseInt(value));
      }
    },
    /**
     * @see SslContextFactory.Server#setNeedClientAuth(boolean)
     */
    NeedClientAuth {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setNeedClientAuth(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see SslContextFactory.Server#setOcspResponderURL(String)
     */
    OcspResponderURL {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setOcspResponderURL(value);
      }
    },
    /**
     * @see SslContextFactory.Server#setSslSessionTimeout(int)
     */
    SslSessionTimeout {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setSslSessionTimeout(Integer.parseInt(value));
      }
    },
    /**
     * @see SslContextFactory.Server#setTrustAll(boolean)
     */
    TrustAll {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setTrustAll(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see SslContextFactory.Server#setTrustManagerFactoryAlgorithm(String)
     */
    TrustManagerFactoryAlgorithm {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setTrustManagerFactoryAlgorithm(value);
      }
    },
    /**
     * @see SslContextFactory.Server#setValidateCerts(boolean)
     */
    ValidateCerts {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setValidateCerts(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see SslContextFactory.Server#setValidatePeerCerts(boolean)
     */
    ValidatePeerCerts {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setValidatePeerCerts(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     *
     * @see SslContextFactory.Server#setWantClientAuth(boolean)
     */
    WantClientAuth {
      @Override
      void applyProperty(SslContextFactory.Server server, String value) {
        server.setWantClientAuth(Boolean.valueOf(value).booleanValue());
      }
    };

    abstract void applyProperty(SslContextFactory.Server server, String value) throws Exception;

  }

  /**
   * Properties for {@link SecureRequestCustomizer}.
   *
   *
   */
  public enum SecureRequestCustomizerProperty {

    /**
     *
     * @see SecureRequestCustomizer#setSniRequired(boolean)
     */
    SniRequired {
      @Override
      void applyProperty(SecureRequestCustomizer secureRequestCustomizer, String value) {
        secureRequestCustomizer.setSniRequired(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see SecureRequestCustomizer#setSniHostCheck(boolean)
     */
    SniHostCheck {
      @Override
      void applyProperty(SecureRequestCustomizer secureRequestCustomizer, String value) throws PasswordException {
        secureRequestCustomizer.setSniHostCheck(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     *
     * stsMaxAgeSeconds The max age in seconds for a Strict-Transport-Security response header. If set less than zero then no header is
     * sent.
     *
     * @see SecureRequestCustomizer#setStsMaxAge(long)
     */
    StsMaxAge {
      @Override
      void applyProperty(SecureRequestCustomizer secureRequestCustomizer, String value) {
        secureRequestCustomizer.setStsMaxAge(Long.parseLong(value));
      }
    },
    /**
     * @see SecureRequestCustomizer#setStsIncludeSubDomains(boolean)
     */
    StsIncludeSubDomains {
      @Override
      void applyProperty(SecureRequestCustomizer secureRequestCustomizer, String value) {
        secureRequestCustomizer.setStsIncludeSubDomains(Boolean.valueOf(value).booleanValue());
      }
    };

    abstract void applyProperty(SecureRequestCustomizer secureRequestCustomizer, String value) throws Exception;

  }

  /**
   * Set the SSL properties.
   *
   * @param sslProperties
   *          the SSL properties
   * @see SslProperty
   */
  @Getter
  @Setter
  @NotNull
  @Valid
  @AutoPopulated
  @AdvancedConfig
  @InputFieldHint(style = "com.adaptris.core.http.jetty.HttpConnections.SslProperty")
  private KeyValuePairSet sslProperties;

  /**
   * Set the Secure Request Customizer properties.
   *
   * @param secureRequestCustomizerProperties
   *          the Secure Request Customizer properties
   * @see SecureRequestCustomizer
   */
  @Getter
  @Setter
  @NotNull
  @Valid
  @AutoPopulated
  @AdvancedConfig
  @InputFieldHint(style = "com.adaptris.core.http.jetty.HttpConnections.SecureRequestCustomizerProperty")
  private KeyValuePairSet secureRequestCustomizerProperties;

  public HttpsConnection() {
    super();
    setPort(8443);
    setSslProperties(new KeyValuePairSet());
    setSecureRequestCustomizerProperties(new KeyValuePairSet());
  }

  SecureRequestCustomizer createSecureRequestCustomizer() throws Exception {
    SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();
    for (KeyValuePair kvp : getSecureRequestCustomizerProperties().getKeyValuePairs()) {
      boolean matched = false;
      for (SecureRequestCustomizerProperty secp : SecureRequestCustomizerProperty.values()) {
        if (kvp.getKey().equalsIgnoreCase(secp.toString())) {
          secp.applyProperty(secureRequestCustomizer, kvp.getValue());
          matched = true;
          break;
        }
      }
      if (!matched) {
        if (!SimpleBeanUtil.callSetter(server, "set" + kvp.getKey(), kvp.getValue())) {
          log.trace("Ignoring unsupported Property {}", kvp.getKey());
        }
      }
    }
    return secureRequestCustomizer;
  }

  @Override
  protected ConnectionFactory[] createConnectionFactory() throws Exception {
    HttpConfiguration httpsConfig = new HttpConfiguration(createConfig());
    httpsConfig.addCustomizer(createSecureRequestCustomizer());
    SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(createSslContext(), HttpVersion.HTTP_1_1.asString());
    return new ConnectionFactory[] { sslConnectionFactory, new HttpConnectionFactory(httpsConfig) };
  }

  SslContextFactory.Server createSslContext() throws Exception {
    SslContextFactory.Server sslServer = new SslContextFactory.Server();
    for (KeyValuePair kvp : getSslProperties().getKeyValuePairs()) {
      boolean matched = false;
      for (SslProperty sp : SslProperty.values()) {
        if (kvp.getKey().equalsIgnoreCase(sp.toString())) {
          sp.applyProperty(sslServer, kvp.getValue());
          matched = true;
          break;
        }
      }
      if (!matched) {
        if (!SimpleBeanUtil.callSetter(sslServer, "set" + kvp.getKey(), kvp.getValue())) {
          log.trace("Ignoring unsupported Property {}", kvp.getKey());
        }
      }
    }
    return sslServer;
  }

}
