package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;

public class MapMetadataServiceTest extends MetadataServiceExample {

  private static final String METADATA_KEY = "theMetadataKey";
  private static final String BASIC_NEW_VALUE = "newMetadataValueToBeSet";
  private static final String BASIC_MATCH_VALUE = "theMetadataValueToMatchAgainst";

  private static final String REGEXP_VALUE_TO_MATCH = "ABCEF_VER_PDF";
  private static final String REGEXP = "(.*)_(.*)_(.*)";
  private static final String REGEX_MATCH_GROUP = "{2}";
  private static final String REGEX_NEW_METADATA_VALUE = "VER";

  private static final String UNMATCHED_REGXP = "ABCDEFGHIJKL(.*)";

  public MapMetadataServiceTest(String arg0) {
    super(arg0);
  }

  private MapMetadataService createService() {
    KeyValuePairList kvps = new KeyValuePairList();
    kvps.addKeyValuePair(new KeyValuePair(BASIC_MATCH_VALUE, BASIC_NEW_VALUE));
    kvps.addKeyValuePair(new KeyValuePair(UNMATCHED_REGXP, BASIC_NEW_VALUE));
    kvps.addKeyValuePair(new KeyValuePair(REGEXP, REGEX_MATCH_GROUP));
    MapMetadataService service = new MapMetadataService();
    service.setMetadataKeyMap(kvps);
    service.setMetadataKey(METADATA_KEY);
    return service;
  }

  public void testSetter() throws Exception {
    MapMetadataService service = createService();
    try {
      service.setMetadataKeyMap(null);
      fail("Success with metadata keys null");
    }
    catch (IllegalArgumentException expected) {
      ;
    }
  }

  public void testReplacementNoMatch() throws Exception {
    MapMetadataService service = new MapMetadataService();
    service.setMetadataKey(null);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("zzzzzzzz");
    execute(service, msg);
    assertFalse(msg.containsKey(METADATA_KEY));
  }

  public void testReplacementNoMatchingKey() throws Exception {
    MapMetadataService service = createService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("zzzzzzzz");
    execute(service, msg);
    assertFalse(msg.containsKey(METADATA_KEY));
  }

  public void testReplacement() throws Exception {
    MapMetadataService service = createService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("zzzzzzzz");
    msg.addMetadata(METADATA_KEY, BASIC_MATCH_VALUE);
    execute(service, msg);
    assertTrue(msg.getMetadataValue(METADATA_KEY).equals(BASIC_NEW_VALUE));
  }

  public void testRegexGroupReplacement() throws Exception {
    MapMetadataService service = createService();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage("zzzzzzzz");
    msg.addMetadata(METADATA_KEY, REGEXP_VALUE_TO_MATCH);
    service.doService(msg);
    assertTrue(msg.getMetadataValue(METADATA_KEY).equals(REGEX_NEW_METADATA_VALUE));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return createService();
  }
}
