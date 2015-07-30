package com.adaptris.core.services.splitter;

import static com.adaptris.core.ServiceCase.execute;
import static com.adaptris.core.services.splitter.SplitterCase.XML_MESSAGE;
import static com.adaptris.core.services.splitter.SplitterCase.createLineCountMessageInput;
import static com.adaptris.core.services.splitter.MessageSplitterServiceImp.KEY_CURRENT_SPLIT_MESSAGE_COUNT;

import java.util.List;

import junit.framework.TestCase;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.stubs.MockConnection;
import com.adaptris.core.stubs.MockMessageProducer;

public class BasicMessageSplitterServiceTest extends TestCase {

  protected static final String METADATA_KEY = "key";
  protected static final String METADATA_VALUE = "value";
  protected static final String REGEXP_DATA = "****|****|****|****|";

  public BasicMessageSplitterServiceTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testSetIgnoreSplitFailures() throws Exception {
    MessageSplitterServiceImp service = createServiceImpl(new LineCountSplitter(), new MockMessageProducer());
    assertNull(service.getIgnoreSplitMessageFailures());
    assertFalse(service.ignoreSplitMessageFailures());
    service.setIgnoreSplitMessageFailures(Boolean.TRUE);
    assertNotNull(service.ignoreSplitMessageFailures());
    assertEquals(Boolean.TRUE, service.getIgnoreSplitMessageFailures());
    assertTrue(service.ignoreSplitMessageFailures());
    service.setIgnoreSplitMessageFailures(null);
    assertNull(service.getIgnoreSplitMessageFailures());
    assertFalse(service.ignoreSplitMessageFailures());
  }

  public void testInit() throws Exception {
    BasicMessageSplitterService service = new BasicMessageSplitterService();
    try {
    service.init();
      fail();
    }
    catch (CoreException expected) {

    }
    service.setConnection(new NullConnection());
    service.setSplitter(new LineCountSplitter());
    service.setProducer(new NullMessageProducer());
    service.init();
  }

  public void testServiceSetters() {
    BasicMessageSplitterService service = new BasicMessageSplitterService();
    assertEquals(NullConnection.class, service.getConnection().getClass());
    assertEquals(NullMessageProducer.class, service.getProducer().getClass());
    try {
      service.setConnection(null);
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {
      ;
    }
    assertEquals(NullConnection.class, service.getConnection().getClass());
    service.setConnection(new MockConnection());
    assertEquals(MockConnection.class, service.getConnection().getClass());
    try {
      service.setProducer(null);
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {
      ;
    }
    assertEquals(NullMessageProducer.class, service.getProducer().getClass());
    service.setProducer(new MockMessageProducer());
    assertEquals(MockMessageProducer.class, service.getProducer().getClass());
    try {
      service.setSplitter(null);
      fail("Expected IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {
      ;
    }
  }

  public void testServiceWithXmlSplitter() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MessageSplitterServiceImp service = createServiceImpl(new XpathMessageSplitter("/envelope/document", "UTF-8"), producer);
    AdaptrisMessage msg = createMessage(XML_MESSAGE);
    XpathMessageSplitter splitter = new XpathMessageSplitter("/envelope/document", "UTF-8");
    execute(service, msg);
    assertEquals("Number of messages", 3, producer.getMessages().size());
  }

  public void testServiceWithLineCountSplitter() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MessageSplitterServiceImp service = createServiceImpl(new LineCountSplitter(), producer);
    AdaptrisMessage msg = createMessage(createLineCountMessageInput());
    msg.addMetadata(METADATA_KEY, METADATA_VALUE);
    execute(service, msg);
    assertEquals("Number of messages", 10, producer.getMessages().size());
    assertEquals("splitCount metadata", 10, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
  }

  public void testServiceWithRegexpSplitter() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MessageSplitterServiceImp service = createServiceImpl(new SimpleRegexpMessageSplitter("\\|"), producer);
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    execute(service, msg);
    List<AdaptrisMessage> producedMessages = producer.getMessages();
    assertEquals(4, producedMessages.size());
    assertEquals("splitCount metadata", 4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
  }

  public void testDoServiceWithCopyObjectMetadata() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MessageSplitterServiceImp service = createServiceImpl(new SimpleRegexpMessageSplitter("\\|"), producer);
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    Object obj = "ABCDEFG";
    ((SimpleRegexpMessageSplitter) service.getSplitter()).setCopyObjectMetadata(true);
    msg.getObjectMetadata().put(obj, obj);
    ServiceCase.execute(service, msg);
    List<AdaptrisMessage> producedMessages = producer.getMessages();
    assertEquals(4, producedMessages.size());
    assertEquals("splitCount metadata", 4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    
    int count = 0;
    for (AdaptrisMessage m : producedMessages) {
      count ++;
      assertEquals(METADATA_VALUE, m.getMetadataValue(METADATA_KEY));
      assertTrue(m.getObjectMetadata().containsKey(obj));
      assertEquals(obj, m.getObjectMetadata().get(obj));
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

  public void testDoServiceWithoutCopyObjectMetadata() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MessageSplitterServiceImp service = createServiceImpl(new SimpleRegexpMessageSplitter("\\|"), producer);
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    ((SimpleRegexpMessageSplitter) service.getSplitter()).setCopyMetadata(true);
    Object obj = "ABCDEFG";
    msg.getObjectMetadata().put(obj, obj);
    execute(service, msg);
    List<AdaptrisMessage> producedMessages = producer.getMessages();
    assertEquals(4, producedMessages.size());
    assertEquals("splitCount metadata", 4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    int count = 0;
    for (AdaptrisMessage m : producedMessages) {
      count ++;
      assertEquals(METADATA_VALUE, m.getMetadataValue(METADATA_KEY));
      assertFalse(m.getObjectMetadata().containsKey(obj));
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

  public void testDoServiceWithCopyMetadata() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MessageSplitterServiceImp service = createServiceImpl(new SimpleRegexpMessageSplitter("\\|"), producer);
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    execute(service, msg);
    List<AdaptrisMessage> producedMessages = producer.getMessages();
    assertTrue(producedMessages.size() == 4);
    assertEquals("splitCount metadata", 4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));
    
    int count = 0;
    for (AdaptrisMessage m : producedMessages) {
      count ++;
      assertEquals(METADATA_VALUE, m.getMetadataValue(METADATA_KEY));
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

  public void testDoServiceWithoutCopyMetadata() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    MessageSplitterServiceImp service = createServiceImpl(new SimpleRegexpMessageSplitter("\\|"), producer);
    AdaptrisMessage msg = createMessage(REGEXP_DATA);
    ((SimpleRegexpMessageSplitter) service.getSplitter())
        .setCopyMetadata(false);
    ServiceCase.execute(service, msg);
    List<AdaptrisMessage> producedMessages = producer.getMessages();
    assertTrue(producedMessages.size() == 4);
    assertEquals("splitCount metadata", 4, Integer.parseInt(msg.getMetadataValue(MessageSplitterServiceImp.KEY_SPLIT_MESSAGE_COUNT)));

    int count = 0;
    for (AdaptrisMessage m : producedMessages) {
      count ++;
      assertTrue(null == m.getMetadataValue(METADATA_KEY));
      assertEquals(count, Integer.parseInt(m.getMetadataValue(KEY_CURRENT_SPLIT_MESSAGE_COUNT)));
    }
  }

  // BackReference Test no longer valid due to INTERLOK-145
  // public void testBackReferences() throws Exception {
  // BasicMessageSplitterService testObject = new BasicMessageSplitterService();
  // testObject.setConnection(new NullConnection());
  // assertEquals(1, testObject.getConnection().retrieveExceptionListeners().size());
  // assertTrue(testObject == testObject.getConnection().retrieveExceptionListeners().toArray()[0]);
  //
  // // Now marshall and see if it's the same.
  // XStreamMarshaller m = new XStreamMarshaller();
  // String xml = m.marshal(testObject);
  // BasicMessageSplitterService testObject2 = (BasicMessageSplitterService) m.unmarshal(xml);
  // // If the setter has been used, then these two will be "true"
  // assertNotNull(testObject2.getConnection());
  // assertEquals(1, testObject2.getConnection().retrieveExceptionListeners().size());
  // assertTrue(testObject2 == testObject2.getConnection().retrieveExceptionListeners().toArray()[0]);
  // }

  protected MessageSplitterServiceImp createServiceImpl(MessageSplitter splitter, MockMessageProducer producer) {
    BasicMessageSplitterService service = new BasicMessageSplitterService();
    service.setConnection(new NullConnection());
    service.setProducer(producer);
    service.setSplitter(splitter);
    return service;
  }

  protected AdaptrisMessage createMessage(String data) {
    return createMessage(AdaptrisMessageFactory.getDefaultInstance().newMessage(data));
  }

  protected AdaptrisMessage createMessage(AdaptrisMessage src) {
    src.addMetadata(METADATA_KEY, METADATA_VALUE);
    return src;
  }
}
