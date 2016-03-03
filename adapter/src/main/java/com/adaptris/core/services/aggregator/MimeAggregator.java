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

package com.adaptris.core.services.aggregator;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.OutputStream;
import java.util.Collection;

import javax.validation.constraints.Pattern;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.text.mime.MultiPartOutput;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link MessageAggregator} implementation that creates a new mime part for each message that needs to be joined up.
 * 
 * <p>
 * The pre-split message is always treated as the first part of the resulting multipart message; the payloads from the split
 * messages form the second and subsequent parts. If the split message contains a specific metadata key (as configured by
 * {@link #setPartContentIdMetadataKey(String)}) then the corresponding value will be used as that parts <code>Content-Id</code>. If
 * the metadata key does not exist, or is not configured, then the split message's unique-id will be used. If the same
 * <code>Content-Id</code> is observed for multiple split messages then results are undefined. The most likely situation is that
 * parts will be lost and only one preserved.
 * </p>
 * <p>
 * Note that the first part's <code>Content-Id</code> will always be the original messages unique-id. Also, if the original message
 * was a Multipart message, then this will be added as a single part to the resulting multipart message (giving you a nested
 * multipart as the first part).
 * </p>
 * <p>
 * As a result of this join operation, the message will be marked as MIME encoded using {@link com.adaptris.core.CoreConstants#MSG_MIME_ENCODED}
 * metadata.
 * </p>
 * 
 * @config mime-aggregator
 * @see CoreConstants#MSG_MIME_ENCODED
 * @author lchan
 * 
 */
@XStreamAlias("mime-aggregator")
@DisplayOrder(order = {"encoding", "overwriteMetadata", "partContentIdMetadataKey"})
public class MimeAggregator extends MessageAggregatorImpl {

  @Pattern(regexp = "base64|quoted-printable|uuencode|x-uuencode|x-uue|binary|7bit|8bit")
  @AdvancedConfig
  private String encoding;
  @AdvancedConfig
  private String partContentIdMetadataKey;

  @Override
  public void joinMessage(AdaptrisMessage original, Collection<AdaptrisMessage> messages) throws CoreException {
    OutputStream out = null;
    try {
      MultiPartOutput output = new MultiPartOutput(original.getUniqueId());
      output.addPart(original.getPayload(), getEncoding(), original.getUniqueId());
      for (AdaptrisMessage m : messages) {
        output.addPart(m.getPayload(), getEncoding(), getContentId(m));
        overwriteMetadata(m, original);
      }
      out = original.getOutputStream();
      output.writeTo(out);
      original.addMetadata(CoreConstants.MSG_MIME_ENCODED, Boolean.TRUE.toString());
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    finally {
      closeQuietly(out);
    }
  }

  /**
   * @return the encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Set the encoding to be used for the content.
   * 
   * @param s the encoding to set, defaults to no-encoding (null)
   */
  public void setEncoding(String s) {
    this.encoding = s;
  }

  /**
   * @return the partContentIdMetadataKey
   */
  public String getPartContentIdMetadataKey() {
    return partContentIdMetadataKey;
  }

  /**
   * Set the content ID for a given mime part based on a metadata key.
   * 
   * @param s the partContentIdMetadataKey to set
   */
  public void setPartContentIdMetadataKey(String s) {
    this.partContentIdMetadataKey = s;
  }

  protected String getContentId(AdaptrisMessage msg) {
    String result = msg.getUniqueId();
    if (!isEmpty(getPartContentIdMetadataKey()) && msg.containsKey(getPartContentIdMetadataKey())) {
      result = msg.getMetadataValue(getPartContentIdMetadataKey());
    }
    return result;
  }
}
