package com.adaptris.core.http.jetty;

import static com.adaptris.core.http.jetty.JettyHelper.createChannel;
import static com.adaptris.core.http.jetty.JettyHelper.createConsumer;
import static com.adaptris.core.http.jetty.JettyHelper.createWorkflow;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.PortManager;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandaloneRequestor;
import com.adaptris.core.http.ConfiguredStatusProvider;
import com.adaptris.core.http.HttpProducerExample;
import com.adaptris.core.http.HttpStatusProvider.HttpStatus;
import com.adaptris.core.http.JdkHttpProducer;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.services.metadata.PayloadFromMetadataService;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

@SuppressWarnings("deprecation")
public class ResponseProducerTest extends HttpProducerExample {

  private static final String CUSTOM_VALUE2 = "CustomValue2";
  private static final String CUSTOM_VALUE1 = "CustomValue1";
  private static final String CUSTOM_HEADER2 = "CustomHeader2";
  private static final String CUSTOM_HEADER1 = "CustomHeader1";
  private static final String METADATA_REGEXP = "CustomHeader.*";
  private static final String TEXT_PLAIN = "text/plain";
  private static final String CONTENT_TYPE = "content.type";
  private static final String JETTY_HTTP_PORT = "jetty.http.port";
  private static final String URL_TO_POST_TO = "/url/to/post/to";
  private static final String TEXT = "ABCDEFG";

  public ResponseProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testSetters() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    try {
      responder.setSendMetadataRegexp(null);
      fail("null accepted");
    }
    catch (IllegalArgumentException e) {

    }
  }

  public void testNoObjectMetadata() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    StandaloneProducer p = new StandaloneProducer(responder);
    try {
      start(p);
      AdaptrisMessage msg = createMessage();
      p.doService(msg);
    }
    finally {
      stop(p);
    }
  }

  public void testDoService_LegacyResponseCode() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection,
        createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getStringPayload());
      assertFalse(msg.containsKey(CUSTOM_HEADER1));
      assertFalse(msg.containsKey(CUSTOM_HEADER2));

    }
    finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  public void testDoService() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection, createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getStringPayload());
      assertFalse(msg.containsKey(CUSTOM_HEADER1));
      assertFalse(msg.containsKey(CUSTOM_HEADER2));

    } finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  public void testDoService_NoFlush() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    responder.setFlushBuffer(Boolean.FALSE);
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection,
        createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getStringPayload());
      assertFalse(msg.containsKey(CUSTOM_HEADER1));
      assertFalse(msg.containsKey(CUSTOM_HEADER2));
    }
    finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  public void testResponseWithError() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.INTERNAL_ERROR_500);
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection,
        createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      fail("StandaloneRequestor.doService() success even though we should have got a 500 error back");
    }
    catch (ServiceException expected) {
      ;
    }
    finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  public void testResponseWithAdditionalHeaders() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    responder.getAdditionalHeaders().add(new KeyValuePair(CUSTOM_HEADER1, CUSTOM_VALUE1));
    responder.getAdditionalHeaders().add(new KeyValuePair(CUSTOM_HEADER2, CUSTOM_VALUE2));
    HttpConnection httpConnection = createConnection();

    Channel c = createChannel(httpConnection,
        createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getStringPayload());
      assertTrue(msg.containsKey(CUSTOM_HEADER1));
      assertTrue(msg.containsKey(CUSTOM_HEADER2));
      assertEquals(CUSTOM_VALUE1, msg.getMetadataValue(CUSTOM_HEADER1));
      assertEquals(CUSTOM_VALUE2, msg.getMetadataValue(CUSTOM_HEADER2));
    }
    finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  public void testResponseWithSendMetadataAsHeaders_Legacy() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    responder.setSendMetadataAsHeaders(true);
    responder.setSendMetadataRegexp(METADATA_REGEXP);
    AddMetadataService addMetadata = new AddMetadataService(Arrays.asList(new MetadataElement[]
    {
        new MetadataElement(CUSTOM_HEADER1, CUSTOM_VALUE1), new MetadataElement(CUSTOM_HEADER2, CUSTOM_VALUE2)
    }));
    HttpConnection httpConnection = createConnection();

    Channel c = createChannel(httpConnection,
        createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), new ServiceList(new Service[]
        {
            addMetadata, new StandaloneProducer(responder)
        })));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getStringPayload());
      assertTrue(msg.containsKey(CUSTOM_HEADER1));
      assertTrue(msg.containsKey(CUSTOM_HEADER2));
      assertEquals(CUSTOM_VALUE1, msg.getMetadataValue(CUSTOM_HEADER1));
      assertEquals(CUSTOM_VALUE2, msg.getMetadataValue(CUSTOM_HEADER2));
    }
    finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  public void testResponseWithSendMetadataAsHeaders() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addIncludePattern(METADATA_REGEXP);
    responder.setMetadataFilter(filter);
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
      assertEquals(TEXT, msg.getStringPayload());
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



  public void testResponseWithContentTypeMetadata() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    responder.setContentTypeKey("MyContentType");
    AddMetadataService addMetadata = new AddMetadataService(Arrays.asList(new MetadataElement[]
    {
      new MetadataElement("MyContentType", "text/xml")
    }));
    HttpConnection httpConnection = createConnection();

    Channel c = createChannel(httpConnection,
        createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), new ServiceList(new Service[]
        {
            addMetadata, new StandaloneProducer(responder)
        })));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getStringPayload());
      assertTrue(msg.containsKey("Content-Type"));
      assertEquals("text/xml", msg.getMetadataValue("Content-Type"));
    }
    finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  public void testResponseWithContentTypeMetadataButNoMetadata() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    responder.setContentTypeKey("MyContentType");
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection,
        createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), new ServiceList(new Service[]
        {
          new StandaloneProducer(responder)
        })));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertEquals(TEXT, msg.getStringPayload());
    }
    finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  public void testResponseWithZeroLengthPayload() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
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
      assertNotSame(TEXT, msg.getStringPayload());

    }
    finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  public void testResponseWithEncoder() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    responder.setEncoder(new MimeEncoder());
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection,
        createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertNotSame(TEXT, msg.getStringPayload());

    }
    finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  public void testResponseWithNoSendPayload() throws Exception {
    ResponseProducer responder = new ResponseProducer(HttpStatus.OK_200);
    responder.setSendPayload(false);
    HttpConnection httpConnection = createConnection();
    Channel c = createChannel(httpConnection,
        createWorkflow(createConsumer(URL_TO_POST_TO), new MockMessageProducer(), responder));
    StandaloneRequestor requestor = createRequestor(httpConnection.getPort());
    AdaptrisMessage msg = createMessage();
    try {
      c.requestStart();
      start(requestor);
      requestor.doService(msg);
      assertNotSame(TEXT, msg.getStringPayload());
    }
    finally {
      c.requestClose();
      stop(requestor);
      PortManager.release(httpConnection.getPort());
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    ResponseProducer producer = new ResponseProducer();
    producer.setStatusProvider(new ConfiguredStatusProvider(HttpStatus.OK_200));
    producer.setContentTypeKey("Content-Type-Metadata-Key");
    KeyValuePair header1 = new KeyValuePair("key1", "val1");
    KeyValuePair header2 = new KeyValuePair("key2", "val2");

    KeyValuePairSet headers = new KeyValuePairSet();
    headers.addKeyValuePair(header1);
    headers.addKeyValuePair(header2);
    producer.setAdditionalHeaders(headers);

    return new StandaloneProducer(producer);
  }

  private HttpConnection createConnection() {
    HttpConnection c = new HttpConnection();
    c.setPort(PortManager.nextUnusedPort(Integer.parseInt(PROPERTIES.getProperty(JETTY_HTTP_PORT))));
    return c;
  }

  private ConfiguredProduceDestination createProduceDestination(int port) {
    ConfiguredProduceDestination d = new ConfiguredProduceDestination("http://localhost:" + port
        + URL_TO_POST_TO);
    return d;
  }

  private StandaloneRequestor createRequestor(int port) {
    JdkHttpProducer producer = new JdkHttpProducer(createProduceDestination(port));
    producer.setContentTypeKey(CONTENT_TYPE);
    producer.setIgnoreReplyMetadata(false);
    producer.setReplyHttpHeadersAsMetadata(true);
    return new StandaloneRequestor(producer);
  }

  private AdaptrisMessage createMessage() {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage(TEXT);
    msg.addMetadata(CONTENT_TYPE, TEXT_PLAIN);
    return msg;
  }
}
