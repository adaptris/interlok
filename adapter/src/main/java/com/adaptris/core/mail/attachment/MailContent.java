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

package com.adaptris.core.mail.attachment;

import java.security.MessageDigest;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.adaptris.util.text.Conversion;

/**
 * Representation of the body of a mail message.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public class MailContent {

  private transient ContentType contentType;
  private transient byte[] payload;
  private transient String payloadHash;

  protected MailContent() throws ParseException {
    contentType = new ContentType("application/octet-stream");
  }

  public MailContent(byte[] bytes) throws ParseException {
    this(bytes, new ContentType("application/octet-stream"));
  }

  public MailContent(byte[] bytes, ContentType ctype) throws ParseException {
    this();
    if (ctype != null) {
      contentType = ctype;
    }
    payload = bytes;
    payloadHash = calculateHash(payload);
  }

  public byte[] getBytes() {
    return payload;
  }

  public String getContentType() {
    return contentType.toString();
  }

  protected String payloadHash() {
    return payloadHash;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("ContentType", getContentType())
        .append("payloadHash", payloadHash()).toString();
  }

  private static String calculateHash(byte[] b) {
    String result = null;
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(b);
      byte[] hash = md.digest();
      result = Conversion.byteArrayToBase64String(hash);
    }
    catch (Exception e) {
      ;
    }
    return result;
  }
}
