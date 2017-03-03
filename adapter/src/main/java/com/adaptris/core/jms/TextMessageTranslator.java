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
import javax.jms.TextMessage;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Translates between <code>AdaptrisMessage</code> and <code>javax.jms.TextMessages</code>. Assumes default platform encoding.
 * </p>
 * <p>
 * In the adapter configuration file this class is aliased as <b>text-message-translator</b> which is the preferred alternative to
 * the fully qualified classname when building your configuration.
 * </p>
 * 
 * @config text-message-translator
 * 
 */
@XStreamAlias("text-message-translator")
@DisplayOrder(order = {"metadataFilter", "moveMetadata", "moveJmsHeaders", "reportAllErrors"})
public final class TextMessageTranslator extends MessageTypeTranslatorImp {

  public TextMessageTranslator() {
    super();
  }

  public TextMessageTranslator(boolean moveJmsHeaders) {
    super(moveJmsHeaders);
  }

  /**
   * <p>
   * Translates an <code>AdaptrisMessage</code> into a <code>TextMessage</code>
   * using the default platform character encoding.
   * </p>
   *
   * @param msg the <code>AdaptrisMessage</code> to translate
   * @return a new <code>TextMessage</code>
   * @throws JMSException
   */
  public Message translate(AdaptrisMessage msg) throws JMSException {
    return helper.moveMetadata(msg, session.createTextMessage(msg.getContent()));
  }

  /**
   * <p>
   * Translates a <code>TextMessage</code> into an <code>AdaptrisMessage</code>
   * using the default platform character encoding.
   * </p>
   *
   * @param msg the <code>TextMessage</code> to translate
   * @return an <code>AdaptrisMessage</code>
   * @throws JMSException
   */
  public AdaptrisMessage translate(Message msg) throws JMSException {
    AdaptrisMessage result = currentMessageFactory().newMessage(((TextMessage) msg).getText());
    return helper.moveMetadata(msg, result);
  }
}
