package com.adaptris.core.jms.activemq;

import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;

import java.util.Map;

import javax.jms.Queue;
import javax.jms.Topic;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.MetadataDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.jndi.CachedDestinationJndiImplementation;
import com.adaptris.core.stubs.MockMessageListener;
import com.adaptris.util.KeyValuePair;

public class DestinationCacheJndiPtpProducerTest extends JndiPtpProducerCase {

  public DestinationCacheJndiPtpProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected CachedDestinationJndiImplementation createVendorImplementation() {
    return new CachedDestinationJndiImplementation();
  }

  public void testProduceWithCache() throws Exception {
    EmbeddedActiveMq activeMqBroker = new EmbeddedActiveMq();
    DestinationCachingJndiVendorImpl sendVendorImp = new DestinationCachingJndiVendorImpl();
    sendVendorImp.setUseJndiForQueues(true);
    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";

    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPtpConnection(sendVendorImp, false,
        queueName, topicName), new PtpProducer(new ConfiguredProduceDestination(queueName)));
    try {
      activeMqBroker.start();
      start(standaloneProducer);
      standaloneProducer.doService(createMessage(null));
      standaloneProducer.doService(createMessage(null));
      assertEquals(1, sendVendorImp.queueCacheSize());
      assertTrue(sendVendorImp.queueCache().containsKey(queueName));
    }
    finally {
      activeMqBroker.destroy();
      stop(standaloneProducer);
    }
  }

  public void testProduceWithCacheExceeded() throws Exception {
    EmbeddedActiveMq broker = new EmbeddedActiveMq();
    DestinationCachingJndiVendorImpl jv = new DestinationCachingJndiVendorImpl(2);
    MockMessageListener jms = new MockMessageListener();
    MetadataDestination dest = new MetadataDestination();
    dest.addKey("testProduceWithCacheExceeded");

    String queueName = getName() + "_queue";
    String topicName = getName() + "_topic";

    StandaloneProducer sp1 = new StandaloneProducer(broker.getJndiPtpConnection(jv, false, queueName, topicName), new PtpProducer(
        dest));
    jv.setUseJndiForQueues(true);
    jv.getJndiParams().addKeyValuePair(new KeyValuePair("queue.testProduceWithCacheExceeded1", "testProduceWithCacheExceeded1"));
    jv.getJndiParams().addKeyValuePair(new KeyValuePair("queue.testProduceWithCacheExceeded2", "testProduceWithCacheExceeded2"));

    try {
      broker.start();
      start(sp1);
      AdaptrisMessage m1 = createMessage(null);
      m1.addMetadata("testProduceWithCacheExceeded", queueName);
      AdaptrisMessage m2 = createMessage(null);
      m2.addMetadata("testProduceWithCacheExceeded", "testProduceWithCacheExceeded2");
      AdaptrisMessage m3 = createMessage(null);
      m3.addMetadata("testProduceWithCacheExceeded", "testProduceWithCacheExceeded1");
      sp1.doService(m1);
      sp1.doService(m2);
      sp1.doService(m2); // This 2nd produce will expire get(QUEUE) on a LRU
                         // basis.
      sp1.doService(m3);
      assertEquals(2, jv.queueCacheSize());
      assertFalse(jv.queueCache().containsKey(queueName));
      assertTrue(jv.queueCache().containsKey("testProduceWithCacheExceeded1"));
      assertTrue(jv.queueCache().containsKey("testProduceWithCacheExceeded2"));
    }
    finally {
      broker.destroy();
      stop(sp1);
    }
  }

  private class DestinationCachingJndiVendorImpl extends CachedDestinationJndiImplementation {

    public DestinationCachingJndiVendorImpl() {
      super();
    }

    public DestinationCachingJndiVendorImpl(int i) {
      super(i);
    }

    public int queueCacheSize() {
      return queues.size();
    }

    public int topicCacheSize() {
      return topics.size();
    }

    public Map<String, Queue> queueCache() {
      return queues;
    }

    public Map<String, Topic> topicCache() {
      return topics;
    }
  }
}
