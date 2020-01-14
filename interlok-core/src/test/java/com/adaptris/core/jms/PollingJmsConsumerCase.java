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

package com.adaptris.core.jms;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.util.TimeInterval;

public abstract class PollingJmsConsumerCase extends JmsConsumerCase {

  protected abstract JmsPollingConsumerImpl createConsumer();

  @Test
  public void testSetReceiveWait() throws Exception {
    JmsPollingConsumerImpl consumer = createConsumer();
    assertNull(consumer.getReceiveTimeout());
    assertEquals(2000, consumer.receiveTimeout());

    TimeInterval interval = new TimeInterval(1L, TimeUnit.MINUTES);
    TimeInterval bad = new TimeInterval(-1L, TimeUnit.MILLISECONDS);

    consumer.setReceiveTimeout(interval);
    assertEquals(interval, consumer.getReceiveTimeout());
    assertEquals(interval.toMilliseconds(), consumer.receiveTimeout());

    consumer.setReceiveTimeout(bad);
    assertEquals(bad, consumer.getReceiveTimeout());
    assertEquals(2000L, consumer.receiveTimeout());

    consumer.setReceiveTimeout(null);
    assertNull(consumer.getReceiveTimeout());
    assertEquals(2000, consumer.receiveTimeout());

  }
}
