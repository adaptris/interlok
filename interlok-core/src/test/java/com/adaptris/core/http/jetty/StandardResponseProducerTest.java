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

import static com.adaptris.core.http.jetty.JettyHelper.createChannel;
import static com.adaptris.core.http.jetty.JettyHelper.createConsumer;
import static com.adaptris.core.http.jetty.JettyHelper.createWorkflow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoderImp;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.PortManager;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.http.HttpProducerExample;
import com.adaptris.core.http.MetadataContentTypeProvider;
import com.adaptris.core.http.client.net.ResponseHeadersAsMetadata;
import com.adaptris.core.http.client.net.StandardHttpProducer;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.services.metadata.PayloadFromMetadataService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.text.mime.MultiPartOutput;

@SuppressWarnings("deprecation")
public class StandardResponseProducerTest extends HttpProducerExample {

  protected static final String CUSTOM_VALUE2 = "CustomValue2";
  protected static final String CUSTOM_VALUE1 = "CustomValue1";
  protected static final String CUSTOM_HEADER2 = "CustomHeader2";
  protected static final String CUSTOM_HEADER1 = "CustomHeader1";
  protected static final String METADATA_REGEXP = "CustomHeader.*";
  protected static final String TEXT_PLAIN = "text/plain";
  protected static final String CONTENT_TYPE = "content.type";
  protected static final String JETTY_HTTP_PORT = "jetty.http.port";
  protected static final String URL_TO_POST_TO = "/url/to/post/to";
  protected static final String TEXT = "ABCDEFG";


  @Test
  public void testNoObjectMetadata() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    StandaloneProducer p = new StandaloneProducer(responder);
    try {
      // INTERLOK-3329 For coverage so the prepare() warning is executed 2x
      LifecycleHelper.prepare(p);
      start(p);
      AdaptrisMessage msg = createMessage();
      p.doService(msg);
    } finally {
      stop(p);
    }
  }

  @Test
  public void testDoService() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getContent());
      assertFalse(msg.containsKey(CUSTOM_HEADER1));
      assertFalse(msg.containsKey(CUSTOM_HEADER2));

    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testDoService_NoFlush() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    responder.setFlushBuffer(Boolean.FALSE);
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getContent());
      assertFalse(msg.containsKey(CUSTOM_HEADER1));
      assertFalse(msg.containsKey(CUSTOM_HEADER2));
    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testResponseWithError() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.INTERNAL_ERROR_500);
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      fail("StandaloneRequestor.doService() success even though we should have got a 500 error back");
    } catch (ServiceException expected) {
      ;
    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testResponse_ConfiguredResponseHeaders() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    ConfiguredResponseHeaderProvider headers = new ConfiguredResponseHeaderProvider(new KeyValuePair(CUSTOM_HEADER1, CUSTOM_VALUE1),
        new KeyValuePair(CUSTOM_HEADER2, CUSTOM_VALUE2));
    responder.setResponseHeaderProvider(headers);
    HttpConnection httpConnection = createConnection();

    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getContent());
      assertTrue(msg.containsKey(CUSTOM_HEADER1));
      assertTrue(msg.containsKey(CUSTOM_HEADER2));
      assertEquals(CUSTOM_VALUE1, msg.getMetadataValue(CUSTOM_HEADER1));
      assertEquals(CUSTOM_VALUE2, msg.getMetadataValue(CUSTOM_HEADER2));
    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testResponse_MetadataResponseHeaders() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addIncludePattern(METADATA_REGEXP);
    responder.setResponseHeaderProvider(new MetadataResponseHeaderProvider(filter));
    AddMetadataService addMetadata = new AddMetadataService(Arrays.asList(new MetadataElement[] {
        new MetadataElement(CUSTOM_HEADER1, CUSTOM_VALUE1), new MetadataElement(CUSTOM_HEADER2, CUSTOM_VALUE2)}));
    HttpConnection httpConnection = createConnection();

    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(),
        new ServiceList(new Service[] {addMetadata, new StandaloneProducer(responder)})));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getContent());
      assertTrue(msg.containsKey(CUSTOM_HEADER1));
      assertTrue(msg.containsKey(CUSTOM_HEADER2));
      assertEquals(CUSTOM_VALUE1, msg.getMetadataValue(CUSTOM_HEADER1));
      assertEquals(CUSTOM_VALUE2, msg.getMetadataValue(CUSTOM_HEADER2));
    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testResponse_NoOpResponseHeaders() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    responder.setResponseHeaderProvider(new NoOpResponseHeaderProvider());
    HttpConnection httpConnection = createConnection();

    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(),
        new ServiceList(new Service[] {new StandaloneProducer(responder)})));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getContent());
    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testResponse_MetadataContentType() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    responder.setContentTypeProvider(new MetadataContentTypeProvider("MyContentType"));
    AddMetadataService addMetadata =
        new AddMetadataService(Arrays.asList(new MetadataElement[] {new MetadataElement("MyContentType", "text/xml")}));
    HttpConnection httpConnection = createConnection();

    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(),
        new ServiceList(new Service[] {addMetadata, new StandaloneProducer(responder)})));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getContent());
      assertTrue(msg.containsKey("Content-Type"));
      assertTrue(msg.getMetadataValue("Content-Type").startsWith("text/xml"));
    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testResponse_MetadataContentType_MissingMetadata() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    responder.setContentTypeProvider(new MetadataContentTypeProvider("MyContentType"));
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(),
        new ServiceList(new Service[] {new StandaloneProducer(responder)})));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getContent());
    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testResponse_MultipleAttempts() throws Exception {
    StandardResponseProducer r1 = new StandardResponseProducer(HttpStatus.OK_200);
    // 2nd responder will not fire...
    StandardResponseProducer r2 = new StandardResponseProducer(HttpStatus.INTERNAL_ERROR_500);
    PayloadFromMetadataService pms = new PayloadFromMetadataService();
    pms.setTemplate("");
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(),
        new ServiceList(new Service[]
        {
            pms, new StandaloneProducer(r1), new StandaloneProducer(r2)
        })));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertNotSame(TEXT, msg.getContent());

    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testResponseWithZeroLengthPayload() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    PayloadFromMetadataService pms = new PayloadFromMetadataService();
    pms.setTemplate("");
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection,
        createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), new ServiceList(new Service[]
        {
            pms, new StandaloneProducer(responder)
        })));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertNotSame(TEXT, msg.getContent());

    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testResponseWithEncoder() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    responder.setSendPayload(true);
    responder.setEncoder(new MyMimeEncoder());
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      System.err.println(msg.getContent());
      assertNotSame(TEXT, msg.getContent());

    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testResponseWithNoSendPayload() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    responder.setSendPayload(false);
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertNotSame(TEXT, msg.getContent());
    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Test
  public void testResponse_CompositeResponseHeaders() throws Exception {
    StandardResponseProducer responder = new StandardResponseProducer(HttpStatus.OK_200);
    responder.setSendPayload(false);
    CompositeResponseHeaderProvider provider =
        new CompositeResponseHeaderProvider(new MetadataResponseHeaderProvider(new RegexMetadataFilter()),
            new ConfiguredResponseHeaderProvider(new KeyValuePair("Hello", "World")));
    responder.setResponseHeaderProvider(provider);
    AddMetadataService meta = new AddMetadataService();
    meta.addMetadataElement(getName(), getName());
    ServiceList sl = new ServiceList(new Service[] {meta, new StandaloneProducer(responder)});
    HttpConnection httpConnection = createConnection();

    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), sl));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertNotSame(TEXT, msg.getContent());
      assertTrue(msg.containsKey(getName()));
      assertTrue(msg.containsKey("Hello"));
    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    StandardResponseProducer producer = new StandardResponseProducer(HttpStatus.OK_200);
    return new StandaloneProducer(producer);
  }

  protected static HttpConnection createConnection() {
    HttpConnection c = new HttpConnection();
    c.setPort(PortManager.nextUnusedPort(Integer.parseInt(PROPERTIES.getProperty(JETTY_HTTP_PORT))));
    return c;
  }

  @Deprecated
  protected static ConfiguredProduceDestination createProduceDestination(int port) {
    return new ConfiguredProduceDestination(createURL(port));
  }

  protected static String createURL(int port) {
    return "http://localhost:" + port + URL_TO_POST_TO;
  }


  protected static StandaloneRequestor createRequestor(int port) {
    StandardHttpProducer producer = new StandardHttpProducer().withURL(createURL(port));
    producer.setContentTypeProvider(new MetadataContentTypeProvider(CONTENT_TYPE));
    producer.setResponseHeaderHandler(new ResponseHeadersAsMetadata());
    return new StandaloneRequestor(producer);
  }

  protected static AdaptrisMessage createMessage() {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    msg.addMetadata(CONTENT_TYPE, TEXT_PLAIN);
    return msg;
  }

  public class MyMimeEncoder extends AdaptrisMessageEncoderImp<HttpServletResponse, Object> {

    @Override
    public void writeMessage(AdaptrisMessage msg, HttpServletResponse target) throws CoreException {
      try {
        MultiPartOutput output = new MultiPartOutput(msg.getUniqueId());
        output.addPart(msg.getPayload(), "quoted-printable", "payload");
        output.addPart(serializeMetadata(msg.getMetadata()), "quoted-printable", "metadata");
        try (OutputStream out = new BufferedOutputStream(target.getOutputStream())) {
          out.write(output.getBytes());
        }
      } catch (Exception e) {
        ExceptionHelper.rethrowCoreException(e);
      }
    }

    @Override
    public AdaptrisMessage readMessage(Object source) throws CoreException {
      throw new UnsupportedOperationException();
    }

    private byte[] serializeMetadata(Set<MetadataElement> metadata) throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try {
        Properties p = new Properties();
        for (MetadataElement e : metadata) {
          p.setProperty(e.getKey(), e.getValue());
        }
        p.store(out, "");
      } finally {

      }
      return out.toByteArray();
    }
  }
}
