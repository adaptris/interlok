/*
 * $Author: lchan $
 * $RCSfile: Attachment.java,v $
 * $Revision: 1.4 $
 * $Date: 2005/04/18 17:58:23 $
 */
package com.adaptris.mail;

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
    return "filename [" + filename + "] content-type [" + contentType + "]";
  }
}