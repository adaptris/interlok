/*
 * Copyright 2018 Adaptris Ltd.
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Attempts to use the {@link AdaptrisMessage#getUniqueId()} as the {@code JMSCorrelationID} and vice versa.
 * 
 * @config message-id-correlation-id-source
 * @since 3.7.3
 */
@XStreamAlias("message-id-correlation-id-source")
@ComponentProfile(since = "3.7.3")
public class MessageIdCorrelationIdSource implements CorrelationIdSource {
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void processCorrelationId(AdaptrisMessage src, Message dest) throws JMSException {
    dest.setJMSCorrelationID(src.getUniqueId());
  }

  @Override
  public void processCorrelationId(Message src, AdaptrisMessage dest) throws JMSException {
    String id = src.getJMSCorrelationID();
    if (!StringUtils.isEmpty(id)) {
      dest.setUniqueId(id);
    } else {
      log.debug("No JMSCorrelationID; not modifying unique-id");
    }
  }

}
