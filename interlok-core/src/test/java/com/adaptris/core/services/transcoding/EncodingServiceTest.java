package com.adaptris.core.services.transcoding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.stubs.MockEncoder;
import com.adaptris.core.stubs.StubMessageFactory;
import com.adaptris.core.util.LifecycleHelper;

public class EncodingServiceTest extends TranscodingServiceCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
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

  @Test
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

  @Test
  public void testSetMessageFactory() throws Exception {
    EncodingService s = new EncodingService();
    assertNull(s.getMessageFactory());
    s = new EncodingService(new MockEncoder());
    assertNull(s.getMessageFactory());
    assertTrue(s.getEncoder().currentMessageFactory() instanceof DefaultMessageFactory);
    s = new EncodingService(new MockEncoder());
    AdaptrisMessageFactory amf = new StubMessageFactory();
    s.setMessageFactory(amf);
    try {
      LifecycleHelper.init(s);
      assertEquals(amf, s.getMessageFactory());
      assertTrue(s.getEncoder().currentMessageFactory() instanceof StubMessageFactory);
      assertEquals(amf, s.getEncoder().currentMessageFactory());
    } finally {
      LifecycleHelper.close(s);
    }
  }


  @Test
  public void testMockEncoder() throws Exception {
    EncodingService service = new EncodingService(new MockEncoder());
    AdaptrisMessage msg = createSimpleMessage();
    execute(service, msg);
    assertEquals(TEST_PAYLOAD, new String(msg.getPayload()));
  }

  @Test
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
