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

package com.adaptris.mail;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attachment class.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public final class Attachment {

  private byte[] payload;
  private String filename;
  private String contentType;
  private String encoding;
  private transient Logger logR = LoggerFactory.getLogger(this.getClass());

  private Attachment() {
  }

  Attachment(byte[] bytes, String fname, String type, String encoding) {
    this();
    this.payload = bytes;
    this.filename = fname;
    this.encoding = encoding;
    if (type != null) {
      this.contentType = type;
    }
    else {
      contentType = "application/octet-stream";
    }
  }

  /**
   * Get the underlying attachment as bytes.
   * 
   * @return the attachment.
   */
  public byte[] getBytes() {
    return payload;
  }

  /**
   * Get the filename associated with this attachment.
   * 
   * @return the filename (or null if not set).
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Get the contentType associated with this attachment.
   * 
   * @return the contentType (or null if not set).
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Get the encoding for the attachment mime body part
   * 
   * @return the encoding
   */
  public String getEncoding(){
    return encoding;
  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("filename", getFilename())
        .append("contentType", getContentType())
        .toString();
  }
}
