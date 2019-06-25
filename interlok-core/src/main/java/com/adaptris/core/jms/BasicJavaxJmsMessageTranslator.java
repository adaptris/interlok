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

import javax.jms.JMSException;
import javax.jms.Message;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Translates between <code>AdaptrisMessage</code> and <code>javax.jms.Messages</code>
 * </p>
 * <p>
 * This implementation should be used as a last resort. It is assumed that you will be translating full implementations such as
 * TextMessage/ObjectMessage/XmlMessage/MultipartMessage, however when that is not possible you can use this implementation that
 * will simply move metadata and headers, no payload translation is performed.
 * </p>
 * 
 * @config basic-javax-jms-message-translator
 * 
 */
@XStreamAlias("basic-javax-jms-message-translator")
@DisplayOrder(order = {"metadataFilter", "moveMetadata", "moveJmsHeaders", "reportAllErrors"})
public class BasicJavaxJmsMessageTranslator extends MessageTypeTranslatorImp {

  public BasicJavaxJmsMessageTranslator() {
    super();
  }
  
  
  /**
   * <p>
   * Translates an <code>AdaptrisMessage</code> into a <code>Message</code>
   * </p>
   * 
   * @param msg the <code>AdaptrisMessage</code> to translate
   * @return a new <code>Message</code>
   * @throws JMSException
   */
  @Override
  public Message translate(AdaptrisMessage msg) throws JMSException {
    Message message = session.createMessage();
    helper.moveMetadata(msg, message);
    return message;
  }

  /**
   * <p>
   * Translates a basic <code>Message</code> into an <code>AdaptrisMessage</code>
   * </p>
   * 
   * @param msg the <code>Message</code> to translate
   * @return an <code>AdaptrisMessage</code>
   * @throws JMSException
   */
  @Override
  public AdaptrisMessage translate(Message msg) throws JMSException {
    AdaptrisMessage message = currentMessageFactory().newMessage();
    helper.moveMetadata(msg, message);
    return message;
  }

}
