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
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MessageLoggerImpl;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * MessageLogger implementation that that logs unique-id, metadata and payload.
 * 
 * @config message-logging-with-payload
 */
@XStreamAlias("message-logging-with-payload")
@ComponentProfile(summary = "Log metadata and payload", since = "3.8.4")
public class PayloadMessageLogger extends MessageLoggerImpl {

  @InputFieldDefault(value = "true")
  private Boolean includeMetadata;

  public void setIncludeMetadata(Boolean includeMetadata) {
    this.includeMetadata = includeMetadata;
  }

  public Boolean getIncludeMetadata() {
    return includeMetadata;
  }

  @Override
  public String toString(AdaptrisMessage m) {
    ToStringBuilder builder = builder(m);
    if (includeMetadata()) {
      builder.append(FIELD_METADATA, format(m.getMetadata()));
    }
    builder.append(FIELD_PAYLOAD, m.getPayloadForLogging());
    return builder.toString();
  }

  private Boolean includeMetadata() {
    return BooleanUtils.toBooleanDefaultIfNull(includeMetadata, true);
  }
}
