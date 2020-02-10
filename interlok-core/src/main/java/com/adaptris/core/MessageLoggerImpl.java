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
package com.adaptris.core;

import java.util.Collection;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class MessageLoggerImpl implements MessageLogger {

  protected static final String FIELD_UNIQUE_ID = "uniqueId";
  protected static final String FIELD_METADATA = "metadata";
  protected static final String FIELD_MESSAGE_EVENTS = "message events";
  protected static final String FIELD_PAYLOAD = "payload";

  public static final MessageLogger LAST_RESORT_LOGGER = new MessageLoggerImpl() {

    @Override
    public String toString(AdaptrisMessage m) {
      return builder(m).append(FIELD_METADATA, format(m.getMetadata()))
          .append(FIELD_PAYLOAD, m.getPayloadForLogging()).toString();
    }
    
  };

  protected ToStringBuilder builder(AdaptrisMessage msg) {
    return new ToStringBuilder(msg, ToStringStyle.SHORT_PREFIX_STYLE).append(FIELD_UNIQUE_ID,
        msg.getUniqueId());
  }


  protected Collection<MetadataElement> format(Collection<MetadataElement> set) {
    MetadataCollection metadata = new MetadataCollection();
    set.stream().forEach(e -> {
      metadata.add(wrap(e.getKey(), e.getValue()));
    });
    return metadata;
  }

  protected MetadataElement wrap(String key, String value) {
    return new FormattedElement(key, value);
  }

  private static class FormattedElement extends MetadataElement {
    private static final long serialVersionUID = 2019040201L;

    public FormattedElement(String key, String value) {
      super(key, value);
    }

    @Override
    public String toString() {
      return String.format("[%s]=[%s]", getKey(), getValue());
    }
  }

}
