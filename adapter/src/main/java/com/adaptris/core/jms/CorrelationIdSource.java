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

import com.adaptris.core.AdaptrisMessage;

/**
 * <p>
 * Provides a <code>JMSCorrelationId</code> for the
 * <code>javax.jms.Message</code>.
 * </p>
 */
public interface CorrelationIdSource {

  /**
   * <p>
   * Provides a <code>JMSCorrelationId</code> for the
   * <code>javax.jms.Message</code>.
   * </p>
   * 
   * @param src the <code>AdaptrisMessage</code> being processed
   * @param dest the <code>javax.jms.Message</code> to send
   * @throws JMSException if encoutered setting <code>JMSCorrelationId</code>
   */
  void processCorrelationId(AdaptrisMessage src, Message dest)
      throws JMSException;

  /**
   * <p>
   * Provides a <code>JMSCorrelationId</code> for the
   * <code>javax.jms.Message</code>.
   * </p>
   * 
   * @param dest the <code>AdaptrisMessage</code> to be processed
   * @param src the <code>javax.jms.Message</code> that has been received
   * @throws JMSException if encoutered setting <code>JMSCorrelationId</code>
   */
  void processCorrelationId(Message src, AdaptrisMessage dest)
      throws JMSException;
}
