package com.adaptris.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.util.text.Conversion;
import com.adaptris.util.text.HexDump;

/** Abstract implementation of HttpMessage.
 *  <p>This class provides default implementations of the DataSource 
 * components required
 *  by the interface.
 */
abstract class MessageImp implements HttpMessage {

  private HttpHeaders header;
  protected transient Log logR = null;
  protected ByteArrayInputStream input = null;
  protected ByteArrayOutputStream output = null;
  private MessageDigest md5;


  protected MessageImp() {
    logR = LogFactory.getLog(this.getClass());
    output = new ByteArrayOutputStream();    
    header = new HttpHeaders();
    try {
      md5 = MessageDigest.getInstance("MD5");      
    } catch (Exception ignoredIntentionally) {
      ;
    }
  }

  /** @see javax.activation.DataSource#getInputStream() */
  public InputStream getInputStream() throws java.io.IOException {
    return input;
  }

  /** @see javax.activation.DataSource#getOutputStream() */
  public OutputStream getOutputStream() throws java.io.IOException {
    return output;
  }

  /** @see javax.activation.DataSource#getName()
   */
  public String getName() {
    return getHeaders().get(Http.HEADER_MESSAGE_ID);
  }

  /** @see javax.activation.DataSource#getContentType()
   */
  public String getContentType() {
    return getHeaders().get(Http.CONTENT_TYPE);
  }

  /** @see HttpMessage#getHeaders()
   */
  public HttpHeaders getHeaders() {
    return header;
  }

  /** @see HttpMessage#setHeaders(HttpHeaders)
   */
  public void setHeaders(HttpHeaders h) {
    header = h;
  }

  /** @see Object#toString()
   * 
   */
  public String toString() {
    StringBuffer sb = new StringBuffer();
    try {
      sb.append("Data (md5) [");
      sb.append(getHash());
      sb.append("] Headers: \n");
      sb.append(getHeaders());
    } catch (Exception e) {
      ;
    }
    return sb.toString();
  }
  
  protected void logMessageInfo() {
    Log socketLogger = Http.getSocketLogger();
    if (socketLogger.isTraceEnabled()) {
      socketLogger.trace(toVerboseString());
    } else {
      logR.trace(toString());
    }    
  }

  /** Provide some way of getting extended information of this object.
   * 
   */
  private String toVerboseString() {
    StringBuffer sb = new StringBuffer();
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      sb.append("Headers: \n");
      sb.append(getHeaders());
      sb.append("\nData :\n");
      output.writeTo(out);
      sb.append(HexDump.parse(out.toByteArray()));      
    } catch (Exception e) {
      ;
    }
    return sb.toString();
  }
  
  private String getHash() {
    md5.reset();
    byte[] digest = md5.digest(output.toByteArray());
    return (Conversion.byteArrayToBase64String(digest));        
  }
}