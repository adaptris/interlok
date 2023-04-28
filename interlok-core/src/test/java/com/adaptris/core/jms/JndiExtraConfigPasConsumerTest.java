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

import static com.adaptris.core.jms.JndiExtraConfigPtpProducerTest.DEFAULT_FILE_SUFFIX;
import static com.adaptris.core.jms.JndiExtraConfigPtpProducerTest.DEFAULT_XML_COMMENT;
import static com.adaptris.core.jms.JndiExtraConfigPtpProducerTest.createJndiVendorImpExample;

import com.adaptris.core.StandaloneConsumer;

public class JndiExtraConfigPasConsumerTest
    extends com.adaptris.interlok.junit.scaffolding.jms.JmsConsumerCase {


  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneConsumer(
        createJndiVendorImpExample(new JmsConnection()),
        new PasConsumer().withTopic("jndiReferenceToTopic"));
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + DEFAULT_FILE_SUFFIX;
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + DEFAULT_XML_COMMENT;
  }
}
