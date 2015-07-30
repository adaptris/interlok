package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;

public class StringToHexServiceTest extends MetadataServiceExample {

  public StringToHexServiceTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  private static AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(HexToStringServiceTest.SOURCE_METADATA_KEY, HexToStringServiceTest.PLAIN_TEXT);
    return msg;
  }

  public void testService() throws Exception {
    StringToHexService service = new StringToHexService();
    service.setCharset(HexToStringService.UTF_8);
    service.setMetadataKeyRegexp(HexToStringServiceTest.SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(HexToStringServiceTest.SOURCE_METADATA_KEY));
    assertEquals(HexToStringServiceTest.HEX_TEXT, msg.getMetadataValue(HexToStringServiceTest.SOURCE_METADATA_KEY));
  }

  public void testService_BadEncodingChoice() throws Exception {
    StringToHexService service = new StringToHexService();
    service.setCharset("RandomEncoding!");
    service.setMetadataKeyRegexp(HexToStringServiceTest.SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testService_NoEncoding() throws Exception {
    StringToHexService service = new StringToHexService();
    service.setMetadataKeyRegexp(HexToStringServiceTest.SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(HexToStringServiceTest.SOURCE_METADATA_KEY));
    assertEquals(HexToStringServiceTest.HEX_TEXT, msg.getMetadataValue(HexToStringServiceTest.SOURCE_METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StringToHexService(".*Matching_MetadataKeys.*");
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!-- This is used to reformat strings stored in metadata.\n"
        + "It takes the metadata value and converts it from string into its hex representation\n" + "-->\n";
  }
}
