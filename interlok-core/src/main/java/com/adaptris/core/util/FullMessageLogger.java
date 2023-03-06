/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.util;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MessageLoggerImpl;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * MessageLogger implementation that that logs unique-id, metadata, payload, and events.
 * 
 * @config message-logging-full
 */
@JacksonXmlRootElement(localName = "message-logging-full")
@XStreamAlias("message-logging-full")
@ComponentProfile(summary = "Log everything, including payload and events", since = "3.8.4")
public class FullMessageLogger extends MessageLoggerImpl {


  @Override
  public String toString(AdaptrisMessage m) {
    return builder(m).append(FIELD_METADATA, format(m.getMetadata()))
        .append(FIELD_PAYLOAD, m.getPayloadForLogging())
        .append(FIELD_MESSAGE_EVENTS, m.getMessageLifecycleEvent()).toString();
  }

}
