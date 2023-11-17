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

import static com.adaptris.core.security.JunitSecurityHelper.KEYSTORE_PATH;
import static com.adaptris.core.security.JunitSecurityHelper.KEYSTORE_TYPE;
import static com.adaptris.core.security.JunitSecurityHelper.SECURITY_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.InetAddress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.http.jetty.HttpConnection.HttpConfigurationProperty;
import com.adaptris.core.http.jetty.HttpConnection.ServerConnectorProperty;
import com.adaptris.core.http.jetty.HttpsConnection.SecureRequestCustomizerProperty;
import com.adaptris.core.http.jetty.HttpsConnection.SslProperty;
import com.adaptris.core.management.webserver.SecurityHandlerWrapper;
import com.adaptris.core.security.ConfiguredPrivateKeyPasswordProvider;
import com.adaptris.core.security.JunitSecurityHelper;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.http.legacy.HttpsProduceConnection;
import com.adaptris.http.legacy.SimpleHttpProducer;
import com.adaptris.http.legacy.VersionedHttpsProduceConnection;
import com.adaptris.interlok.junit.scaffolding.util.PortManager;
import com.adaptris.util.KeyValuePair;

@SuppressWarnings("deprecation")
public class HttpsConsumerTest extends HttpConsumerTest {

  private static final String JETTY_HTTPS_PORT = "jetty.https.port";

  @BeforeEach
  public void doKeyStore() throws Exception {
    createKeystore();
  }

  @Test
  public void testTLS_ConsumeWorkflow() throws Exception {
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    HttpConnection connection = createConnection(null);
    ((HttpsConnection) connection).getSslProperties().add(new KeyValuePair(SslProperty.ExcludeProtocols.name(), "SSLv3,TLSv1.1,"));
    MockMessageProducer mockProducer = new MockMessageProducer();
    SimpleHttpProducer myHttpProducer = createProducer(new VersionedHttpsProduceConnection("TLSv1.2"));
    Channel channel = JettyHelper.createChannel(connection, JettyHelper.createConsumer(URL_TO_POST_TO), mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      myHttpProducer.setUrl(createProduceDestinationUrl(connection.getPort()));
      start(myHttpProducer);
      AdaptrisMessage reply = myHttpProducer.request(msg);
      assertEquals(XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);
    } finally {
      stop(myHttpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
      Thread.currentThread().setName(oldName);
    }
  }

  @Test
  public void testTLS_ConsumeWorkflow_ClientUsesSSL() throws Exception {
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    HttpConnection connection = createConnection(null);
    ((HttpsConnection) connection).getSslProperties().add(new KeyValuePair(SslProperty.ExcludeProtocols.name(), "SSLv3"));
    MockMessageProducer mockProducer = new MockMessageProducer();
    SimpleHttpProducer myHttpProducer = createProducer(new VersionedHttpsProduceConnection("SSLv3"));
    Channel channel = JettyHelper.createChannel(connection, JettyHelper.createConsumer(URL_TO_POST_TO), mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      myHttpProducer.setUrl(createProduceDestinationUrl(connection.getPort()));
      start(myHttpProducer);
      myHttpProducer.request(msg);
      // SSLv3 context shouldn't be allowed to connect to TLSv1.2 only
      fail();
    } catch (CoreException expected) {

    } finally {
      stop(myHttpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
      Thread.currentThread().setName(oldName);
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    HttpsConnection connection = (HttpsConnection) createConnection(createSecurityHandlerExample());
    connection.getSslProperties().clear();
    connection.getSslProperties().add(new KeyValuePair(SslProperty.ExcludeProtocols.name(), "SSLv3,TLSv1"));
    connection.getSslProperties().add(new KeyValuePair(HttpsConnection.SslProperty.KeyStorePassword.name(), "password"));
    connection.getSslProperties().add(new KeyValuePair(HttpsConnection.SslProperty.KeyStorePath.name(), "/path/to/keystore"));
    connection.getSslProperties().add(new KeyValuePair(HttpsConnection.SslProperty.KeyStoreType.name(), "JCEKS"));
    connection.getSslProperties().add(new KeyValuePair(HttpsConnection.SslProperty.TrustStorePassword.name(), "password"));
    connection.getSslProperties().add(new KeyValuePair(HttpsConnection.SslProperty.TrustStoreType.name(), "JKS"));
    connection.getSslProperties().add(new KeyValuePair(HttpsConnection.SslProperty.TrustStorePath.name(), "/path/to/trust/keystore"));
    StandaloneConsumer result = new StandaloneConsumer(connection, JettyHelper.createConsumer(URL_TO_POST_TO));
    return result;
  }

  @Override
  protected SimpleHttpProducer createProducer() {
    return createProducer(new HttpsProduceConnection());
  }

  private SimpleHttpProducer createProducer(HttpsProduceConnection https) {
    SimpleHttpProducer p = new SimpleHttpProducer();
    https.setKeystore(PROPERTIES.getProperty(JunitSecurityHelper.KEYSTORE_URL));
    https.setAlwaysTrust(true);
    https.setPrivateKeyPasswordProvider(
        new ConfiguredPrivateKeyPasswordProvider(PROPERTIES.getProperty(JunitSecurityHelper.SECURITY_PASSWORD)));
    https.setKeystorePassword(PROPERTIES.getProperty(JunitSecurityHelper.SECURITY_PASSWORD));
    p.registerConnection(https);
    p.setContentTypeKey("content.type");
    p.setIgnoreServerResponseCode(true);
    return p;
  }

  private void createKeystore() throws Exception {
    InetAddress addr = InetAddress.getLocalHost();
    JunitSecurityHelper h = new JunitSecurityHelper(PROPERTIES);
    h.createKeystore(addr.getHostName());
  }

  @Override
  protected HttpConnection createConnection(SecurityHandlerWrapper sh) {
    HttpsConnection https = new HttpsConnection();
    int port = PortManager.nextUnusedPort(Integer.parseInt(PROPERTIES.getProperty(JETTY_HTTPS_PORT)));
    https.setPort(port);
    https.getServerConnectorProperties().clear();
    https.getServerConnectorProperties().add(new KeyValuePair(ServerConnectorProperty.ReuseAaddress.name(), "true"));

    https.getSslProperties().clear();
    https.getSslProperties().add(new KeyValuePair(SslProperty.KeyStorePassword.name(), PROPERTIES.getProperty(SECURITY_PASSWORD)));
    https.getSslProperties().add(new KeyValuePair(SslProperty.KeyStorePath.name(), PROPERTIES.getProperty(KEYSTORE_PATH)));
    https.getSslProperties().add(new KeyValuePair(SslProperty.KeyStoreType.name(), PROPERTIES.getProperty(KEYSTORE_TYPE)));
    https.getSslProperties().add(new KeyValuePair(SslProperty.KeyManagerPassword.name(), PROPERTIES.getProperty(SECURITY_PASSWORD)));

    https.getSslProperties().add(new KeyValuePair(SslProperty.TrustStorePassword.name(), PROPERTIES.getProperty(SECURITY_PASSWORD)));
    https.getSslProperties().add(new KeyValuePair(SslProperty.TrustStoreType.name(), PROPERTIES.getProperty(KEYSTORE_TYPE)));
    https.getSslProperties().add(new KeyValuePair(SslProperty.TrustStorePath.name(), PROPERTIES.getProperty(KEYSTORE_PATH)));

    https.getHttpConfiguration().clear();
    https.getHttpConfiguration().add(new KeyValuePair(HttpConfigurationProperty.OutputBufferSize.name(), "8192"));
    https.getHttpConfiguration().add(new KeyValuePair(HttpConfigurationProperty.SendServerVersion.name(), "false"));
    https.getHttpConfiguration().add(new KeyValuePair(HttpConfigurationProperty.SendDateHeader.name(), "false"));
    https.getSecureRequestCustomizerProperties().add(new KeyValuePair(SecureRequestCustomizerProperty.SniRequired.name(), "false"));
    https.getSecureRequestCustomizerProperties().add(new KeyValuePair(SecureRequestCustomizerProperty.SniHostCheck.name(), "false"));

    if (sh != null) {
      https.setSecurityHandler(sh);
    }
    return https;
  }

  @Override
  protected String createProduceDestinationUrl(int port) {
    return "https://localhost:" + port + URL_TO_POST_TO;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "S";
  }
}
