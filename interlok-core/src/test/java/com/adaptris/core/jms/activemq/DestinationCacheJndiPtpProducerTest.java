/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.jms.activemq;
import static com.adaptris.core.BaseCase.start;
import static com.adaptris.core.BaseCase.stop;
import static com.adaptris.core.jms.activemq.EmbeddedActiveMq.createMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Map;
import javax.jms.Queue;
import javax.jms.Topic;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.PtpProducer;
import com.adaptris.core.jms.jndi.CachedDestinationJndiImplementation;
import com.adaptris.util.KeyValuePair;

public class DestinationCacheJndiPtpProducerTest extends JndiPtpProducerCase {

  @BeforeClass
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }
  
  @AfterClass
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }
  
  @Override
  protected CachedDestinationJndiImplementation createVendorImplementation() {
    return new CachedDestinationJndiImplementation();
  }

  @Test
  public void testProduceWithCache() throws Exception {
    DestinationCachingJndiVendorImpl sendVendorImp = new DestinationCachingJndiVendorImpl();
    sendVendorImp.setUseJndiForQueues(true);
    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";

    StandaloneProducer standaloneProducer = new StandaloneProducer(activeMqBroker.getJndiPtpConnection(sendVendorImp, false,
            queueName, topicName), new PtpProducer().withQueue((queueName)));
    try {
      start(standaloneProducer);
      standaloneProducer.doService(createMessage(null));
      standaloneProducer.doService(createMessage(null));
      assertEquals(1, sendVendorImp.queueCacheSize());
      assertTrue(sendVendorImp.queueCache().containsKey(queueName));
    }
    finally {
      stop(standaloneProducer);
    }
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testProduceWithCacheExceeded() throws Exception {
    DestinationCachingJndiVendorImpl jv = new DestinationCachingJndiVendorImpl(2);

    String queueName = testName.getMethodName() + "_queue";
    String topicName = testName.getMethodName() + "_topic";

    StandaloneProducer sp1 =
        new StandaloneProducer(activeMqBroker.getJndiPtpConnection(jv, false, queueName, topicName),
            new PtpProducer().withQueue("%message{testProduceWithCacheExceeded}"));

    jv.setUseJndiForQueues(true);
    jv.getJndiParams().addKeyValuePair(new KeyValuePair("queue.testProduceWithCacheExceeded1", "testProduceWithCacheExceeded1"));
    jv.getJndiParams().addKeyValuePair(new KeyValuePair("queue.testProduceWithCacheExceeded2", "testProduceWithCacheExceeded2"));

    try {
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
