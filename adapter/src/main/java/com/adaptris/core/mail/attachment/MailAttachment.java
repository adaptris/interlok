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
