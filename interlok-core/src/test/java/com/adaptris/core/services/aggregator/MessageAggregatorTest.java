package com.adaptris.core.services.aggregator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class MessageAggregatorTest {

  @Test
  public void testAggregateMessages() throws Exception {
    MessageAggregator aggregator = new MessageAggregator() {
      @Override
      public void joinMessage(AdaptrisMessage msg, Collection<AdaptrisMessage> msgs) {};
    };
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("o");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m1");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m2");
    aggregator.aggregate(original, Arrays.asList(new AdaptrisMessage[] {splitMsg1, splitMsg2}));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testJoin() throws Exception {
    MessageAggregator aggregator = new MessageAggregator() {};
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("o");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m1");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m2");
    aggregator.joinMessage(original, Arrays.asList(new AdaptrisMessage[] {splitMsg1, splitMsg2}));
  }

  @Test
  public void testCollect() throws Exception {
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m1");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m2");
    List<AdaptrisMessage> l = Arrays.asList(new AdaptrisMessage[] {splitMsg1, splitMsg2});
    assertSame(l, MessageAggregator.collect(l));
    assertEquals(2,  MessageAggregator.collect(new Iterable<AdaptrisMessage>() {
      @Override
      public Iterator<AdaptrisMessage> iterator() {
        return l.iterator();
      }
    }).size());
  }
}
