package com.adaptris.core.mail.attachment;

import java.security.MessageDigest;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;

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

  @Override
  public String toString() {
    return "[" + this.getClass() + "] ContentType=[" + getContentType()
        + "] Payload(MD5)=["
        + (payloadHash == null ? "Not available" : payloadHash) + "]";
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
