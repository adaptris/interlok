package com.adaptris.core.services.codec;

import com.adaptris.core.*;
import com.adaptris.core.stubs.MockEncoder;
import com.adaptris.core.util.LifecycleHelper;

public class DecodingServiceTest extends CodecServiceCase {

  private static final String OVERRIDE_HEADER_VALUE = "value";

  public DecodingServiceTest(String name) {
    super(name);
  }

  public void testInit() throws Exception {
    DecodingService service = new DecodingService();
    try {
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {
    }
    service.setEncoder(new MockEncoder());
    LifecycleHelper.init(service);
    service = new DecodingService(new MockEncoder());
    LifecycleHelper.init(service);
  }

  public void testValid() throws Exception {
    DecodingService service = new DecodingService(new MockEncoder());
    AdaptrisMessage msg = createSimpleMessage();
    execute(service, msg);
    assertEquals(TEST_PAYLOAD, new String(msg.getPayload()));
  }

  public void testMimeEncoder() throws Exception {
    DecodingService service = new DecodingService(new MimeEncoder());
    AdaptrisMessage msg = createMimeMessage();
    assertFalse(msg.headersContainsKey(TEST_METADATA_KEY));
    assertFalse(msg.headersContainsKey(TEST_METADATA_KEY_2));
    execute(service, msg);
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY));
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY_2));
    assertEquals(TEST_METADATA_VALUE, msg.getMetadataValue(TEST_METADATA_KEY));
    assertEquals(TEST_METADATA_VALUE_2, msg.getMetadataValue(TEST_METADATA_KEY_2));
    assertEquals(TEST_PAYLOAD, new String(msg.getPayload()));
  }

  public void testMimeEncoder_OverrideHeader() throws Exception {
    DecodingService service = new DecodingService(new MimeEncoder());
    service.setOverrideHeaders(true);
    AdaptrisMessage msg = createMimeMessage();
    msg.addMetadata(TEST_METADATA_KEY, OVERRIDE_HEADER_VALUE);
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY));
    assertEquals(OVERRIDE_HEADER_VALUE, msg.getMetadataValue(TEST_METADATA_KEY));
    execute(service, msg);
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY));
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY_2));
    assertEquals(TEST_METADATA_VALUE, msg.getMetadataValue(TEST_METADATA_KEY));
    assertEquals(TEST_METADATA_VALUE_2, msg.getMetadataValue(TEST_METADATA_KEY_2));
    assertEquals(TEST_PAYLOAD, new String(msg.getPayload()));
  }

  public void testMimeEncoder_DoNotOverrideHeader() throws Exception {
    DecodingService service = new DecodingService(new MimeEncoder());
    service.setOverrideHeaders(false);
    AdaptrisMessage msg = createMimeMessage();
    msg.addMetadata(TEST_METADATA_KEY, OVERRIDE_HEADER_VALUE);
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY));
    assertEquals(OVERRIDE_HEADER_VALUE, msg.getMetadataValue(TEST_METADATA_KEY));
    execute(service, msg);
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY));
    assertTrue(msg.headersContainsKey(TEST_METADATA_KEY_2));
    assertEquals(OVERRIDE_HEADER_VALUE, msg.getMetadataValue(TEST_METADATA_KEY));
    assertEquals(TEST_METADATA_VALUE_2, msg.getMetadataValue(TEST_METADATA_KEY_2));
    assertEquals(TEST_PAYLOAD, new String(msg.getPayload()));
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    DecodingService decodingService = new DecodingService();
    decodingService.setEncoder(new MimeEncoder());
    decodingService.setOverrideHeaders(false);
    return decodingService;
  }

}
