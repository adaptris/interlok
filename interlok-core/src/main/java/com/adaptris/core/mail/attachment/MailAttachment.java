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

import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Representation of an attachment to a mail message.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public final class MailAttachment extends MailContent {

  private transient String filename;
  private transient String contentTransferEncoding = "base64";

  public MailAttachment(byte[] bytes, String fname) throws ParseException {
    super(bytes);
    filename = fname;
  }

  public MailAttachment(byte[] bytes, String fname, ContentType ctype) throws ParseException {
    super(bytes, ctype);
    filename = fname;
  }

  public String getFilename() {
    return filename;
  }

  public String getContentTransferEncoding() {
    return contentTransferEncoding;
  }

  public MailAttachment withContentTransferEncoding(String s) {
    contentTransferEncoding = defaultIfBlank(s, "base64");
    return this;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("ContentType", getContentType())
        .append("ContentTransferEncoding", getContentTransferEncoding()).append("payloadHash", payloadHash())
        .append("filename", getFilename()).toString();
  }
}
