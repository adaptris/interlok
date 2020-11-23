/*
 * Copyright 2017 Adaptris Ltd.
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

import javax.jms.Message;
import javax.jms.Session;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.metadata.RegexMetadataFilter;

public abstract class GenericMessageTypeTranslatorCase
    extends com.adaptris.interlok.junit.scaffolding.jms.MessageTypeTranslatorCase {


  @Test
  public void testMetadataConverter() throws Exception {

    MessageTypeTranslatorImp trans = createTranslator().withMetadataConverters(
        new StringMetadataConverter(new RegexMetadataFilter().withIncludePatterns(STRING_METADATA)),
        new IntegerMetadataConverter(new RegexMetadataFilter().withIncludePatterns(INTEGER_METADATA)),
        new BooleanMetadataConverter(
            new RegexMetadataFilter().withIncludePatterns(BOOLEAN_METADATA)));
    try {
      Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
      start(trans, session);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      addMetadata(msg);
      Message jmsMsg = trans.translate(msg);
      assertJmsProperties(jmsMsg);
    } finally {
      stop(trans);
    }
  }
}
