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

import static com.adaptris.core.jms.FailoverPtpProducerTest.createFailoverConfigExample;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.adaptris.core.StandaloneConsumer;

public class FailoverPtpConsumerTest extends FailoverJmsConsumerCase {

  private static final Log LOG = LogFactory
      .getLog(FailoverPtpConsumerTest.class);

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneConsumer(createFailoverConfigExample(true),
        new PtpConsumer().withQueue("QueueName"));
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-Failover";
  }
}
