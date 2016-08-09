package com.adaptris.core.services.codec;

import com.adaptris.core.*;
import com.adaptris.core.stubs.MockEncoder;
import com.adaptris.core.stubs.StubMessageFactory;
import com.adaptris.core.util.LifecycleHelper;

public class EncodingServiceTest extends CodecServiceCase {

  public EncodingServiceTest(String name) {
    super(name);
  }

  public void testInit() throws Exception {
    EncodingService service = new EncodingService();
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {
    }
    service.setEncoder(new MockEncoder());
    LifecycleHelper.init(service);
    service = new EncodingService(new MockEncoder());
    LifecycleHelper.init(service);
  }

  public void testSetEncoder() throws Exception {
    EncodingService s = new EncodingService();
    assertNull(s.getEncoder());
    MockEncoder me = new MockEncoder();
    s = new EncodingService(me);
    assertEquals(me, s.getEncoder());
    s = new EncodingService();
    s.setEncoder(me);
    assertEquals(me, s.getEncoder());
  }

  public void testSetMessageFactory() throws Exception {
    EncodingService s = new EncodingService();
    assertNull(s.getMessageFactory());
    s = new EncodingService(new MockEncoder());
    assertNull(s.getMessageFactory());
    assertTrue(s.getEncoder().currentMessageFactory() instanceof DefaultMessageFactory);
    s = new EncodingService(new MockEncoder());
    AdaptrisMessageFactory amf = new StubMessageFactory();
    s.setMessageFactory(amf);
    assertEquals(amf, s.getMessageFactory());
    assertTrue(s.getEncoder().currentMessageFactory() instanceof StubMessageFactory);
    assertEquals(amf, s.getEncoder().currentMessageFactory());
  }


  public void testMockEncoder() throws Exception {
    EncodingService service = new EncodingService(new MockEncoder());
    AdaptrisMessage msg = createSimpleMessage();
    execute(service, msg);
    assertEquals(TEST_PAYLOAD, new String(msg.getPayload()));
  }

  public void testMimeEncoder() throws Exception {
    EncodingService service = new EncodingService(new MimeEncoder());
    AdaptrisMessage msg = createSimpleMessage();
    execute(service, msg);
    MimeEncoder me = new MimeEncoder();
    AdaptrisMessage encodedMessage  = me.decode(msg.getPayload());
    assertTrue(encodedMessage.headersContainsKey(TEST_METADATA_KEY));
    assertTrue(encodedMessage.headersContainsKey(TEST_METADATA_KEY_2));
    assertEquals(TEST_METADATA_VALUE, encodedMessage.getMetadataValue(TEST_METADATA_KEY));
    assertEquals(TEST_METADATA_VALUE_2, encodedMessage.getMetadataValue(TEST_METADATA_KEY_2));
    assertEquals(TEST_PAYLOAD, new String(encodedMessage.getPayload()));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    EncodingService encodingService = new EncodingService();
    encodingService.setEncoder(new MimeEncoder());
    return encodingService;
  }

}
