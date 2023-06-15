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

import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.CoreException;
import com.adaptris.core.management.webserver.SecurityHandlerWrapper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.SimpleBeanUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

/**
 * Concrete implementation of JettyConnection that allows HTTP traffic.
 * <p>
 * By default all fields are left as null. This will cause Jetty to use its internal defaults. Consult the Jetty Documentation for
 * information on the behaviour and configuration required.
 * </p>
 * <p>
 * The key from the {@code server-connector-properties} element should match the name of the underlying {@link ServerConnector} setter.
 *
 * <pre>
 * {@code
 *   <server-connector-properties>
 *     <key-value-pair>
 *        <key>ReuseAddress</key>
 *        <value>true</value>
 *     </key-value-pair>
 *   </server-connector-properties>
 * }
 * </pre>
 *
 * will invoke {@code ServerConnector#setReuseAddress(boolean)}, setting the ReuseAddress property to true. Note that no validation of the
 * various properties is performed and will be passed as-is to the {@link AbstractConnector} with an attempt to transform into the correct
 * type. Invalid combinations may result in undefined behaviour. Similarly additional {@code HttpConfiguration} properties can be configured
 * via the {@code http-connection} element.
 * </p>
 *
 * @config jetty-http-connection
 *
 * @author lchan
 */
@XStreamAlias("jetty-http-connection")
@AdapterComponent
@ComponentProfile(summary = "Connection that creates its own jetty engine instance and listens on the specified port", tag = "connections,http,jetty")
@DisplayOrder(order = { "port", "httpConfiguration", "serverConnectorProperties" })
public class HttpConnection extends JettyConnection {

  /**
   * Standard {@link ServerConnector} properties for use with {@link HttpConnection#setServerConnectorProperties(KeyValuePairSet)}.
   *
   */
  public enum ServerConnectorProperty {

    /**
     * @see ServerConnector#setAcceptQueueSize(int)
     *
     */
    AcceptQueueSize {
      @Override
      void applyProperty(ServerConnector connector, String value) throws Exception {
        connector.setAcceptQueueSize(Integer.parseInt(value));
      }
    },
    /**
     * @see ServerConnector#setAcceptorPriorityDelta(int)
     *
     */
    AcceptorPriorityDelta {
      @Override
      void applyProperty(ServerConnector connector, String value) throws Exception {
        connector.setAcceptorPriorityDelta(Integer.parseInt(value));
      }
    },
    /**
     * @see ServerConnector#setIdleTimeout(long)
     *
     */
    IdleTimeout {
      @Override
      void applyProperty(ServerConnector connector, String value) throws Exception {
        connector.setIdleTimeout(Long.parseLong(value));
      }
    },
    /**
     * @see ServerConnector#setInheritChannel(boolean)
     *
     */
    InheritChannel {
      @Override
      void applyProperty(ServerConnector connector, String value) throws Exception {
        connector.setInheritChannel(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see ServerConnector#setReuseAddress(boolean)
     */
    ReuseAaddress {
      @Override
      void applyProperty(ServerConnector connector, String value) throws Exception {
        connector.setReuseAddress(Boolean.valueOf(value).booleanValue());
      }
    };

    abstract void applyProperty(ServerConnector connector, String value) throws Exception;
  }

  /**
   * Standard {@link HttpConfiguration} properties for use with {@link HttpConnection#setHttpConfiguration(KeyValuePairSet)}.
   *
   */
  public enum HttpConfigurationProperty {
    /**
     * @see HttpConfiguration#setSecureScheme(String).
     *
     */
    SecureScheme {

      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setSecureScheme(value);
      }

    },
    /**
     * @see HttpConfiguration#setSecurePort(int).
     *
     */
    SecurePort {

      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setSecurePort(Integer.parseInt(value));
      }

    },
    /**
     * @see HttpConfiguration#setOutputBufferSize(int)
     */
    OutputBufferSize {

      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setOutputBufferSize(Integer.parseInt(value));
      }

    },
    /**
     * @see HttpConfiguration#setOutputAggregationSize(int)
     */
    OutputAggregationSize {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setOutputAggregationSize(Integer.parseInt(value));
      }
    },
    /**
     * @see HttpConfiguration#setRequestHeaderSize(int)
     */
    RequestHeaderSize {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setRequestHeaderSize(Integer.parseInt(value));
      }
    },
    /**
     * @see HttpConfiguration#setResponseHeaderSize(int)
     */
    ResponseHeaderSize {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setResponseHeaderSize(Integer.parseInt(value));
      }
    },
    /**
     * @see HttpConfiguration#setSendDateHeader(boolean)
     */
    SendDateHeader {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setSendDateHeader(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see HttpConfiguration#setSendServerVersion(boolean)
     */
    SendServerVersion {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setSendServerVersion(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see HttpConfiguration#setHeaderCacheSize(int)
     */
    HeaderCacheSize {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setHeaderCacheSize(Integer.parseInt(value));
      }
    },
    /**
     * @see HttpConfiguration#setDelayDispatchUntilContent(boolean)
     */
    DelayDispatchUntilContent {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setDelayDispatchUntilContent(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see HttpConfiguration#setSendXPoweredBy(boolean)
     */
    SendXPoweredBy {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setSendXPoweredBy(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see HttpConfiguration#setFormEncodedMethods(String...)
     */
    FormEncodedMethods {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setFormEncodedMethods(asArray(value));
      }
    },
    /**
     * @see HttpConfiguration#setMaxErrorDispatches(int)
     */
    MaxErrorDispatches {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setMaxErrorDispatches(Integer.parseInt(value));
      }
    },
    /**
     * @see HttpConfiguration#setIdleTimeout(long)
     */
    IdleTimeout {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setIdleTimeout(Long.parseLong(value));
      }
    },
    /**
     * @see HttpConfiguration#setMinRequestDataRate(long)
     */
    MinRequestDataRate {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setMinRequestDataRate(Long.parseLong(value));
      }
    },
    MinResponseDataRate {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setMinResponseDataRate(Long.parseLong(value));
      }
    },
    /**
     * @see HttpConfiguration#setPersistentConnectionsEnabled(boolean)
     */
    PersistentConnectionsEnabled {
      @Override
      void applyProperty(HttpConfiguration config, String value) throws Exception {
        config.setPersistentConnectionsEnabled(Boolean.valueOf(value).booleanValue());
      }
    };

    abstract void applyProperty(HttpConfiguration config, String value) throws Exception;

  }

  /**
   * Set the port to listen on for HTTP traffic.
   *
   * @param port
   *          the port, by default it is 8080
   */
  @Getter
  @Setter
  private int port;

  /**
   * Specify the SecurityHandler implementation.
   *
   * @param securityHandler
   *          the securityHandler wrapper implementation.
   */
  @Getter
  @Setter
  @Valid
  @AdvancedConfig
  private SecurityHandlerWrapper securityHandler;

  /**
   * The Server Connector properties to set
   *
   * @param httpConfiguration
   *          KeyValuePairSet
   */
  @Getter
  @Setter
  @NotNull
  @Valid
  @AutoPopulated
  @AdvancedConfig
  @InputFieldHint(style = "com.adaptris.core.http.jetty.HttpConnection.ServerConnectorProperty")
  private KeyValuePairSet serverConnectorProperties;

  /**
   * The HTTP Configuration properties to set
   *
   * @param httpConfiguration
   *          KeyValuePairSet
   */
  @Getter
  @Setter
  @NotNull
  @Valid
  @AutoPopulated
  @AdvancedConfig
  @InputFieldHint(style = "com.adaptris.core.http.jetty.HttpConnection.HttpConfigurationProperty")
  private KeyValuePairSet httpConfiguration;

  public HttpConnection() {
    super();
    setPort(8080);
    setServerConnectorProperties(new KeyValuePairSet());
    setHttpConfiguration(new KeyValuePairSet());
  }

  protected ConnectionFactory[] createConnectionFactory() throws Exception {
    return new ConnectionFactory[] { new HttpConnectionFactory(createConfig()) };
  }

  protected HttpConfiguration createConfig() throws Exception {
    HttpConfiguration cfg = new HttpConfiguration();
    for (KeyValuePair kvp : getHttpConfiguration().getKeyValuePairs()) {
      boolean matched = false;
      for (HttpConfigurationProperty sp : HttpConfigurationProperty.values()) {
        if (kvp.getKey().equalsIgnoreCase(sp.toString())) {
          sp.applyProperty(cfg, kvp.getValue());
          matched = true;
          break;
        }
      }
      if (!matched) {
        if (!SimpleBeanUtil.callSetter(cfg, "set" + kvp.getKey(), kvp.getValue())) {
          log.trace("Ignoring unsupported Property {}", kvp.getKey());
        }
      }
    }
    cfg.setHttpCompliance(HttpCompliance.RFC2616);
    return cfg;
  }

  @Override
  Server configure(Server server) throws Exception {
    server.addConnector(configure(new ServerConnector(server, -1, -1, createConnectionFactory())));
    return server;
  }

  @Override
  Handler createHandler(ServletContextHandler context) throws Exception {
    ContextHandlerCollection handlerCollection = new ContextHandlerCollection();
    Handler result = handlerCollection;
    handlerCollection.addHandler(context);
    if (getSecurityHandler() != null) {
      SecurityHandler sh = getSecurityHandler().createSecurityHandler();
      sh.setHandler(handlerCollection);
      result = sh;
    }
    return result;
  }

  protected ServerConnector configure(ServerConnector connector) throws Exception {
    connector.setPort(getPort());
    for (KeyValuePair kvp : getServerConnectorProperties().getKeyValuePairs()) {
      boolean matched = false;
      for (ServerConnectorProperty sp : ServerConnectorProperty.values()) {
        if (kvp.getKey().equalsIgnoreCase(sp.toString())) {
          sp.applyProperty(connector, kvp.getValue());
          matched = true;
          break;
        }
      }
      if (!matched) {
        if (!SimpleBeanUtil.callSetter(connector, "set" + kvp.getKey(), kvp.getValue())) {
          log.trace("Ignoring unsupported Property {}", kvp.getKey());
        }
      }
    }
    return connector;
  }

  @Override
  protected void prepareConnection() throws CoreException {
  }

}
