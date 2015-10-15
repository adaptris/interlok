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

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;


/**
 * Representation of an attachment to a mail message.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public final class MailAttachment extends MailContent {

  private String filename;

  private MailAttachment() throws ParseException {
    super();
  }

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

  @Override
  public String toString() {
    return super.toString() + " filename=[" + getFilename() + "]";
  }
}
