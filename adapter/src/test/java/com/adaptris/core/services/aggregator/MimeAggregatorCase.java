package com.adaptris.core.services.aggregator;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.aggregator.MimeAggregator;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.stubs.DefectiveMessageFactory;

public abstract class MimeAggregatorCase extends AggregatorCase {

  protected static final String PAYLOAD = "Pack my box with five dozen liquor jugs.";

  public MimeAggregatorCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testSetters() throws Exception {
    MimeAggregator aggr = createAggregatorForTests();
    assertNull(aggr.getEncoding());
    assertNull(aggr.getPartContentIdMetadataKey());
    aggr.setEncoding("base64");
    assertEquals("base64", aggr.getEncoding());
    aggr.setPartContentIdMetadataKey("myMetadataKey");
    assertEquals("myMetadataKey", aggr.getPartContentIdMetadataKey());
  }

  public void testJoinMessage_NoOverwriteMetadata() throws Exception {
    MimeAggregator aggr = createAggregatorForTests();
    aggr.setOverwriteMetadata(false);
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>");
    original.addMetadata("originalKey", "originalValue");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>world</document>");
    splitMsg2.addMetadata("originalKey", "newValue");
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[]
    {
        splitMsg1, splitMsg2
    }));
    assertEquals("originalValue", original.getMetadataValue("originalKey"));
  }

  public void testJoinMessage_OverwriteMetadata() throws Exception {
    MimeAggregator aggr = createAggregatorForTests();
    aggr.setOverwriteMetadata(true);
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("<envelope/>");
    original.addMetadata("originalKey", "originalValue");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>world</document>");
    splitMsg2.addMetadata("originalKey", "newValue");
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[]
    {
        splitMsg1, splitMsg2
    }));
    assertEquals("newValue", original.getMetadataValue("originalKey"));
  }

  public void testJoinMessage_Fails() throws Exception {
    MimeAggregator aggr = createAggregatorForTests();
    AdaptrisMessage original = new DefectiveMessageFactory().newMessage("<envelope/>");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>hello</document>");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("<document>world</document>");
    try {
      aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[]
      {
          splitMsg1, splitMsg2
      }));
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Override
  protected MimeAggregator createAggregatorForTests() {
    return new MimeAggregator();
  }

  protected AddMetadataService createAddMetadataService(String key) {
    AddMetadataService service = new AddMetadataService();
    service.addMetadataElement(key, "$UNIQUE_ID$");
    return service;
  }
}
