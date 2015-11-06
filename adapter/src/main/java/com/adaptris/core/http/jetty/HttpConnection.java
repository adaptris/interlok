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

import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.CoreException;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Concrete implementation of JettyConnection that allows HTTP traffic.
 * <p>
 * By default all fields are left as null. This will cause Jetty to use its internal defaults. Consult the Jetty Documentation for
 * information on the behaviour and configuration required.
 * </p>
 * <p>
 * The key from the <code>http-properties</code> element should match the name of the underlying {@link AbstractConnector} setter.
 * 
 * <pre>
 * {@code 
 *   <http-properties>
 *     <key-value-pair>
 *        <key>ReuseAddress</key>
 *        <value>true</value>
 *     </key-value-pair>
 *   </http-properties>
 * }
 * </pre>
 * will invoke {@link AbstractConnector#setReuseAddress(boolean)}, setting the ReuseAddress property to true. Note that no
 * validation of the various properties is performed and will be passed as-is to the {@link AbstractConnector} with an attempt to
 * transform into the correct type. Invalid combinations may result in undefined behaviour.
 * </p>
 * 
 * @config jetty-http-connection
 * @license STANDARD
 * @author lchan
 */
@XStreamAlias("jetty-http-connection")
public class HttpConnection extends JettyConnection {

  /**
   * A standard {@link AbstractConnector} property.
   * 
   */
  public enum HttpProperty {

    /**
     * @see AbstractConnector#setMaxIdleTime(int)
     */
    MaxIdleTime {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setMaxIdleTime(Integer.parseInt(value));
      }
    },
    /**
     * @see AbstractConnector#setMaxBuffers(int)
     */
    MaxBuffers {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setMaxBuffers(Integer.parseInt(value));
      }
    },
    /**
     * @see AbstractConnector#setAcceptorPriorityOffset(int)
     */
    AcceptorPriorityOffset {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setAcceptorPriorityOffset(Integer.parseInt(value));
      }
    },
    /**
     * @see AbstractConnector#setAcceptors(int)
     */
    Acceptors {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setAcceptors(Integer.parseInt(value));
      }
    },
    /**
     * @see AbstractConnector#setAcceptQueueSize(int)
     */
    AcceptQueueSize {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setAcceptQueueSize(Integer.parseInt(value));
      }
    },
    /**
     * @see AbstractConnector#setSoLingerTime(int)
     */
    SoLingerTime {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setSoLingerTime(Integer.parseInt(value));
      }
    },
    /**
     * @see AbstractConnector#setLowResourcesMaxIdleTime(int)
     */
    LowResourcesMaxIdleTime {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setLowResourcesMaxIdleTime(Integer.parseInt(value));
      }
    },

    /**
     * @see AbstractConnector#setRequestHeaderSize(int)
     */
    RequestHeaderSize {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setRequestHeaderSize(Integer.parseInt(value));
      }
    },
    /**
     * @see AbstractConnector#setRequestBufferSize(int)
     */
    RequestBufferSize {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setRequestBufferSize(Integer.parseInt(value));
      }
    },
    /**
     * @see AbstractConnector#setResponseBufferSize(int)
     */
    ResponseBufferSize {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setResponseBufferSize(Integer.parseInt(value));
      }
    },
    /**
     * @see AbstractConnector#setResponseHeaderSize(int)
     */
    ResponseHeaderSize {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setResponseHeaderSize(Integer.parseInt(value));
      }
    },
    /**
     * @see AbstractConnector#setResolveNames(boolean)
     */
    ResolveNames {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setResolveNames(Boolean.valueOf(value).booleanValue());
      }
    },
    /**
     * @see AbstractConnector#setReuseAddress(boolean)
     */
    ReuseAaddress {
      @Override
      void applyProperty(AbstractConnector connector, String value) throws Exception {
        connector.setReuseAddress(Boolean.valueOf(value).booleanValue());
      }
    };
    abstract void applyProperty(AbstractConnector connector, String value) throws Exception;
  }

  private int port;
  @AdvancedConfig
  private Boolean sendDateHeader;
  @AdvancedConfig
  private Boolean sendServerVersion;
  @Valid
  @AdvancedConfig
  private SecurityHandlerWrapper securityHandler;
  @NotNull
  @Valid
  @AutoPopulated
  @AdvancedConfig
  private KeyValuePairSet httpProperties;

  /**
   * Default Constructor. Defaults are :
   * <ul>
   * <li>port = 8080</li>
   * </ul>
   * 
   */
  public HttpConnection() {
    super();
    setPort(8080);
    setHttpProperties(new KeyValuePairSet());
  }

  Connector createConnector() throws Exception {
    BlockingChannelConnector connector = new BlockingChannelConnector();
    return configure(connector);
  }

  @Override
  Server configure(Server server) throws Exception {
    server.addConnector(createConnector());
    server.setSendServerVersion(sendServerVersion());
    server.setSendDateHeader(sendDateHeader());
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

  AbstractConnector configure(AbstractConnector connector) throws Exception {
    connector.setPort(getPort());
    for (KeyValuePair kvp : getHttpProperties().getKeyValuePairs()) {
      boolean matched = false;
      for (HttpProperty sp : HttpProperty.values()) {
        if (kvp.getKey().equalsIgnoreCase(sp.toString())) {
          sp.applyProperty(connector, kvp.getValue());
          matched = true;
          break;
        }
      }
      if (!matched) {
        log.trace("Ignoring unsupported Property " + kvp.getKey());
      }
    }
    return connector;
  }

  /**
   * Set the port to listen on.
   * 
   * @param i the port, by default it is 8080
   */
  public void setPort(int i) {
    port = i;
  }

  /**
   * Get the port to listen on for HTTP traffic.
   * 
   * @return the port
   */
  public int getPort() {
    return port;
  }

  public KeyValuePairSet getHttpProperties() {
    return httpProperties;
  }

  public void setHttpProperties(KeyValuePairSet httpProperties) {
    this.httpProperties = httpProperties;
  }

  public Boolean getSendDateHeader() {
    return sendDateHeader;
  }

  /**
   * Specify whether to send the Date when sending a response.
   * 
   * @param b the sendDateHeader to set
   */
  public void setSendDateHeader(Boolean b) {
    sendDateHeader = b;
  }

  boolean sendDateHeader() {
    return getSendDateHeader() != null ? getSendDateHeader().booleanValue() : false;
  }

  public Boolean getSendServerVersion() {
    return sendServerVersion;
  }

  boolean sendServerVersion() {
    return getSendServerVersion() != null ? getSendServerVersion().booleanValue() : false;
  }
  /**
   * Specify whether to send the server version when sending a response.
   * 
   */
  public void setSendServerVersion(Boolean b) {
    sendServerVersion = b;
  }

  /**
   * @return the securityHandler wrapper implementation
   */
  public SecurityHandlerWrapper getSecurityHandler() {
    return securityHandler;
  }

  /**
   * Specify the SecurityHandler implementation.
   * 
   * @param s the securityHandler wrapper implementation.
   */
  public void setSecurityHandler(SecurityHandlerWrapper s) {
    securityHandler = s;
  }

  @Override
  protected void prepareConnection() throws CoreException {
  }

}
