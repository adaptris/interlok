package com.adaptris.core.services.aggregator;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.conditional.conditions.ConditionImpl;

public class MessageAggregatorTest {

  @Test
  public void testAggregateMessages() throws Exception {
    MessageAggregator aggregator = new MessageAggregator() {
      @Override
      public void joinMessage(AdaptrisMessage msg, Collection<AdaptrisMessage> msgs) {
      };
    };
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("o");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m1");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m2");
    aggregator.aggregate(original, Arrays.asList(new AdaptrisMessage[] { splitMsg1, splitMsg2 }));
  }

  @Test
  public void testJoin() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      MessageAggregator aggregator = new MessageAggregator() {
      };
      AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("o");
      AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m1");
      AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m2");
      aggregator.joinMessage(original, Arrays.asList(new AdaptrisMessage[] { splitMsg1, splitMsg2 }));
    });
  }

  @Test
  public void testCollect() throws Exception {
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m1");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m2");
    List<AdaptrisMessage> l = Arrays.asList(new AdaptrisMessage[] { splitMsg1, splitMsg2 });
    assertSame(l, MessageAggregator.collect(l));
    assertEquals(2, MessageAggregator.collect(new Iterable<AdaptrisMessage>() {
      @Override
      public Iterator<AdaptrisMessage> iterator() {
        return l.iterator();
      }
    }).size());
  }

  @Test
  public void testImplFilter() throws Exception {
    AppendingMessageAggregator aggregator = new AppendingMessageAggregator();
    aggregator.setFilterCondition(new EvenOddCondition());
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m1");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("m2");
    List<AdaptrisMessage> l = Arrays.asList(new AdaptrisMessage[] { splitMsg1, splitMsg2 });
    assertEquals(1, aggregator.filter(l).size());
  }

  // Have a condition that every other call passes
  public static class EvenOddCondition extends ConditionImpl {
    private int numberOfCalls = 0;

    @Override
    public boolean evaluate(AdaptrisMessage message) throws CoreException {
      numberOfCalls++;
      return numberOfCalls % 2 == 0;
    }

    @Override
    public void close() {
      throw new RuntimeException();
    }
  }
}
