package com.adaptris.core.services.aggregator;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.NullService;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.splitter.SplitJoinService;
import com.adaptris.core.services.splitter.SplitJoinServiceTest;
import com.adaptris.core.services.splitter.XpathDocumentCopier;

public class NullMessageAggregatorTest extends AggregatingServiceExample {

  public NullMessageAggregatorTest(String name) {
    super(name);
  }

  public void testJoinMessage() throws Exception {
    NullMessageAggregator aggr = createAggregatorForTests();
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("Goodbye");
    original.addMetadata("originalKey", "originalValue");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("Cruel");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("World");
    splitMsg1.addMetadata("originalKey", "newValue");
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[]
    {
        splitMsg1, splitMsg2
    }));
    assertEquals("Goodbye", original.getStringPayload());
    assertEquals("originalValue", original.getMetadataValue("originalKey"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SplitJoinService service = new SplitJoinService();
    service.setService(SplitJoinServiceTest.wrap(new LogMessageService(), new NullService()));
    service.setSplitter(new XpathDocumentCopier("count(//invoice-lines)"));
    service.setAggregator(new NullMessageAggregator());
    return service;
  }

  protected NullMessageAggregator createAggregatorForTests() {
    return new NullMessageAggregator();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!--"
        + "\nThis aggregator implementation does nothing, and will not change the original message."
        + "\n-->\n";
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-" + createAggregatorForTests().getClass().getSimpleName();
  }
}
