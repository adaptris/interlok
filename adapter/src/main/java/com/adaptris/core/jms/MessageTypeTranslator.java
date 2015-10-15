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
import javax.jms.Session;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageTranslator;

// abstract factory pattern

/**
 * <p>
 * Interface that translate <code>AdaptrisMessage</code>s to the various type of
 * <code>javax.jms.Message</code>s, and vice versa.
 * </p>
 */
public interface MessageTypeTranslator extends AdaptrisComponent,
    AdaptrisMessageTranslator {

  /**
   * <p>
   * Translates the passed <code>AdaptrisMessage</code> into an instance of a
   * subclass of <code>javax.jms.Message</code>.
   * </p>
   * 
   * @param msg the <code>AdaptrisMessage</code> to translate
   * @return a <code>javax.jms.Message</code>
   * @throws JMSException
   */
  Message translate(AdaptrisMessage msg) throws JMSException;

  /**
   * <p>
   * Translates the passed <code>javax.jms.Message</code> into an instance of
   * <code>AdaptrisMessage</code>.
   * </p>
   * 
   * @param msg the <code>javax.jms.Message</code> to translate
   * @return a <code>AdaptrisMessage</code>
   * @throws JMSException
   */
  AdaptrisMessage translate(Message msg) throws JMSException;

  /**
   * Register the JMS session with this message translator.
   * 
   * @param s the session.
   */
  void registerSession(Session s);

  /**
   * Obtain the JMS session currently registered.
   * 
   * @return the session
   */
  Session currentSession();
}
