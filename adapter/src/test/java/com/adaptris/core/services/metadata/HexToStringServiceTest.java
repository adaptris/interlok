package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;

public class HexToStringServiceTest extends MetadataServiceExample {

  public static final String PLAIN_TEXT = "hello world";
  public static final String HEX_TEXT = "68656c6c6f20776f726c64";

  public static final String SOURCE_METADATA_KEY = "sourceMetadataKey";
  public static final String BAD_METADATA_KEY = "badMetadataKey";

  public HexToStringServiceTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  private static AdaptrisMessage createMessage() {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(SOURCE_METADATA_KEY, HEX_TEXT);
    msg.addMetadata(BAD_METADATA_KEY, "SomethingThatIsn'tHex");
    return msg;
  }

  public void testService() throws Exception {
    HexToStringService service = new HexToStringService();
    service.setCharset("UTF-8");
    service.setMetadataKeyRegexp(SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(SOURCE_METADATA_KEY));
    assertEquals(PLAIN_TEXT, msg.getMetadataValue(SOURCE_METADATA_KEY));
  }

  public void testService_NotHex() throws Exception {
    HexToStringService service = new HexToStringService();
    service.setCharset("UTF-8");
    service.setMetadataKeyRegexp(BAD_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testService_BadEncodingChoice() throws Exception {
    HexToStringService service = new HexToStringService();
    service.setCharset("RandomEncoding!");
    service.setMetadataKeyRegexp(SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testService_NoEncoding() throws Exception {
    HexToStringService service = new HexToStringService();
    service.setMetadataKeyRegexp(SOURCE_METADATA_KEY);
    AdaptrisMessage msg = createMessage();
    execute(service, msg);
    assertTrue(msg.containsKey(SOURCE_METADATA_KEY));
    assertEquals(PLAIN_TEXT, msg.getMetadataValue(SOURCE_METADATA_KEY));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new HexToStringService(".*Matching_MetadataKeys_With_HexValues.*");
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!-- This is used to reformat strings stored in metadata.\n"
        + "It takes the metadata value and converts it from hex into its string representation\n"
        + "Note that while the conversion is strictly correct (depending on your encoding choice),\n"
        + "depending on the hex value, you might end up with a String that may not be valid for \n" + "some systems.\n"
        + "For instance it might have a reserved character, and not be valid as JMS metadata;\n"
        + "It might contain a vertical tab which would not be valid for insertion into XML.\n" + "-->\n";
  }
}
