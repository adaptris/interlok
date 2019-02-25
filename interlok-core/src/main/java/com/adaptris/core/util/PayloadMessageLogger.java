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
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * MessageLogger implementation that that logs unique-id, metadata and payload.
 * 
 * @config message-logging-with-payload
 */
@XStreamAlias("message-logging-with-payload")
@ComponentProfile(summary = "Log metadata and payload", since = "3.8.4")
public class PayloadMessageLogger extends MessageLoggerImpl {

  @Override
  public String toString(AdaptrisMessage m) {
    return builder(m).append(FIELD_METADATA, format(m.getMetadata()))
        .append(FIELD_PAYLOAD, m.getPayloadForLogging()).toString();
  }
}
