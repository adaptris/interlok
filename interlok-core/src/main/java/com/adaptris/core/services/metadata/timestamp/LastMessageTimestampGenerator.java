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
package com.adaptris.core.services.metadata.timestamp;

import java.util.Date;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.metadata.AddTimestampMetadataService;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Timestamp Generator implementation that returns the last {@link Date} a message passed through the service.
 * 
 * 
 * @see AddTimestampMetadataService
 * @since 3.5.0
 */
@JacksonXmlRootElement(localName = "last-message-timestamp-generator")
@XStreamAlias("last-message-timestamp-generator")
public class LastMessageTimestampGenerator implements TimestampGenerator {

  private transient Date lastMsgDate = new Date(1);

  @Override
  public Date generateTimestamp(AdaptrisMessage msg) throws ServiceException {
    Date timestamp = lastMsgDate;
    lastMsgDate = new Date();
    return timestamp;
  }

}
