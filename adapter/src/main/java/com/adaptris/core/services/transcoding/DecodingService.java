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

package com.adaptris.core.services.transcoding;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoder;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Decodes the payload and updates the in flight message with the decoded output.
 *
 * @config decoding-service
 *
 */
@XStreamAlias("decoding-service")
@AdapterComponent
@ComponentProfile(summary = "Decodes the message", tag = "service")
public class DecodingService extends TranscodingService {

  public DecodingService(){
  }

  public DecodingService(AdaptrisMessageEncoder encoder) {
    super(encoder);
  }

  @InputFieldDefault(value = "false")
  private Boolean overrideMetadata;

  @Override
  public void transcodeMessage(AdaptrisMessage msg) throws ServiceException {
    InputStream msgIn = null;
    OutputStream msgOut = null;
    ByteArrayInputStream decodedPayload = null;
    try {
      msgIn = msg.getInputStream();
      AdaptrisMessage decodedMsg = getEncoder().readMessage(msgIn);
      for (MetadataElement me : decodedMsg.getMetadata()) {
        if (!isOverrideMetadata() && msg.headersContainsKey(me.getKey())){
          continue;
        }
        msg.addMetadata(me);
      }
      decodedPayload = new ByteArrayInputStream(decodedMsg.getPayload());
      msgOut = msg.getOutputStream();
      IOUtils.copy(decodedPayload, msgOut);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
    finally {
      IOUtils.closeQuietly(msgIn);
      IOUtils.closeQuietly(msgOut);
      IOUtils.closeQuietly(decodedPayload);
    }
  }

  boolean isOverrideMetadata(){
    return getOverrideMetadata() == null ? false : getOverrideMetadata();
  }

  public Boolean getOverrideMetadata(){
    return this.overrideMetadata;
  }

  /**
   * Set boolean value to control the overriding of metadata.
   *
   * <p>If true metadata when a metadata key from the decoded message has the same key as metadata in the in flight message it will be replaced with the value of the decoded one.</p>
   *
   * @param overrideMetadata Boolean value to control overriding of metadata.
   */
  public void setOverrideMetadata(Boolean overrideMetadata) {
    this.overrideMetadata = overrideMetadata;
  }

}
