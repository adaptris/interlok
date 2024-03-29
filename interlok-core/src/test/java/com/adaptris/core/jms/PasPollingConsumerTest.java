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

import java.util.concurrent.TimeUnit;

import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.util.TimeInterval;

public class PasPollingConsumerTest extends PollingJmsConsumerCase {


  @Override
  protected Object retrieveObjectForSampleConfig() {
    PasPollingConsumer consumer = createConsumer();
    consumer.setPoller(new FixedIntervalPoller(new TimeInterval(1L, TimeUnit.MINUTES)));
    consumer.setUserName("user-name");
    consumer.setPassword("password");
    consumer.setClientId("client-id");
    consumer.setSubscriptionId("subscription-id");
    consumer.setPoller(new FixedIntervalPoller(new TimeInterval(1L, TimeUnit.MINUTES)));
    consumer.setReacquireLockBetweenMessages(true);
    consumer.setTopic("MyTopic");
    StandaloneConsumer result = new StandaloneConsumer();
    result.setConsumer(consumer);

    return result;
  }

  @Override
  protected PasPollingConsumer createConsumer() {
    return new PasPollingConsumer();
  }

}
