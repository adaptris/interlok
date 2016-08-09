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

import java.net.InetAddress;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.PortManager;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.http.HttpProducer;
import com.adaptris.core.http.jetty.HttpConnection.HttpProperty;
import com.adaptris.core.http.jetty.HttpsConnection.SslProperty;
import com.adaptris.core.security.ConfiguredPrivateKeyPasswordProvider;
import com.adaptris.core.security.JunitSecurityHelper;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.http.legacy.HttpsProduceConnection;
import com.adaptris.http.legacy.SimpleHttpProducer;
import com.adaptris.http.legacy.VersionedHttpsProduceConnection;
import com.adaptris.util.KeyValuePair;

@SuppressWarnings("deprecation")
public class HttpsConsumerTest extends HttpConsumerTest {

  private static final String JETTY_HTTPS_PORT = "jetty.https.port";
  private static final String URL_TO_POST_TO = "/url/to/post/to";
  private static final String XML_PAYLOAD = "<root><document>value</document></root>";

  public HttpsConsumerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    createKeystore();
    super.setUp();
  }

  public void testTLS_ConsumeWorkflow() throws Exception {
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    HttpConnection connection = createConnection(null);
    ((HttpsConnection) connection).getSslProperties().add(new KeyValuePair(SslProperty.ExcludeProtocols.name(), "SSLv3,TLSv1.1,"));
    MockMessageProducer mockProducer = new MockMessageProducer();
    HttpProducer myHttpProducer = createProducer(new VersionedHttpsProduceConnection("TLSv1.2"));
    Channel channel = JettyHelper.createChannel(connection, JettyHelper.createConsumer(URL_TO_POST_TO), mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(myHttpProducer);
      AdaptrisMessage reply = myHttpProducer.request(msg, createProduceDestination(connection.getPort()));
      assertEquals("Reply Payloads", XML_PAYLOAD, reply.getContent());
      doAssertions(mockProducer);
    }
    finally {
      stop(myHttpProducer);
      channel.requestClose();
      PortManager.release(connection.getPort());
      Thread.currentThread().setName(oldName);
    }
  }

  public void testTLS_ConsumeWorkflow_ClientUsesSSL() throws Exception {
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(getName());
    HttpConnection connection = createConnection(null);
    ((HttpsConnection) connection).getSslProperties().add(new KeyValuePair(SslProperty.ExcludeProtocols.name(), "SSLv3"));
    MockMessageProducer mockProducer = new MockMessageProducer();
    HttpProducer myHttpProducer = createProducer(new VersionedHttpsProduceConnection("SSLv3"));
    Channel channel = JettyHelper.createChannel(connection, JettyHelper.createConsumer(URL_TO_POST_TO), mockProducer);
    try {
      channel.requestStart();
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(XML_PAYLOAD);
      msg.addMetadata(CONTENT_TYPE_METADATA_KEY, "text/xml");
      start(myHttpProducer);
      AdaptrisMessage reply = myHttpProducer.request(msg, createProduceDestination(connection.getPort()));
      // SSLv3 context shouldn't be allowed to connect to TLSv1.2 only
      fail();
    }
    catch (CoreException expected) {

    }
    finally {
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
    connection.getSslProperties().add(new KeyValuePair(HttpsConnection.SslProperty.TrustStore.name(), "/path/to/trust/keystore"));
    StandaloneConsumer result = new StandaloneConsumer(connection, JettyHelper.createConsumer(URL_TO_POST_TO));
    return result;
  }

  @Override
  protected HttpProducer createProducer() {
    return createProducer(new HttpsProduceConnection());
  }

  private HttpProducer createProducer(HttpsProduceConnection https) {
    SimpleHttpProducer p = new SimpleHttpProducer();
    https.setKeystore(PROPERTIES.getProperty(JunitSecurityHelper.KEYSTORE_URL));
    https.setAlwaysTrust(true);
    https.setPrivateKeyPasswordProvider(new ConfiguredPrivateKeyPasswordProvider(PROPERTIES
        .getProperty(JunitSecurityHelper.SECURITY_PASSWORD)));
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
    https.getHttpProperties().clear();
    https.getHttpProperties().add(new KeyValuePair(HttpProperty.SoLingerTime.name(), "-1"));
    https.getHttpProperties().add(new KeyValuePair(HttpProperty.ReuseAaddress.name(), "true"));
    https.getHttpProperties().add(new KeyValuePair(HttpProperty.ResolveNames.name(), "true"));

    https.getSslProperties().clear();
    https.getSslProperties().add(new KeyValuePair(SslProperty.KeyStorePassword.name(), PROPERTIES.getProperty(SECURITY_PASSWORD)));
    https.getSslProperties().add(new KeyValuePair(SslProperty.KeyStorePath.name(), PROPERTIES.getProperty(KEYSTORE_PATH)));
    https.getSslProperties().add(new KeyValuePair(SslProperty.KeyStoreType.name(), PROPERTIES.getProperty(KEYSTORE_TYPE)));
    https.getSslProperties()
        .add(new KeyValuePair(SslProperty.KeyManagerPassword.name(), PROPERTIES.getProperty(SECURITY_PASSWORD)));

    https.getSslProperties()
        .add(new KeyValuePair(SslProperty.TrustStorePassword.name(), PROPERTIES.getProperty(SECURITY_PASSWORD)));
    https.getSslProperties().add(new KeyValuePair(SslProperty.TrustStoreType.name(), PROPERTIES.getProperty(KEYSTORE_TYPE)));
    https.getSslProperties().add(new KeyValuePair(SslProperty.TrustStore.name(), PROPERTIES.getProperty(KEYSTORE_PATH)));
    if (sh != null) {
      https.setSecurityHandler(sh);
    }
    return https;
  }

  @Override
  protected ConfiguredProduceDestination createProduceDestination(int port) {
    ConfiguredProduceDestination d = new ConfiguredProduceDestination("https://localhost:" + port + URL_TO_POST_TO);
    return d;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "S";
  }
}
