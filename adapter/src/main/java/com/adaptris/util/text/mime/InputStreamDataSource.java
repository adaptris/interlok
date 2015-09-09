package com.adaptris.util.text.mime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;


/** A datasource on an arbitary input stream.
 *  @see javax.activation.DataSource
 */
public class InputStreamDataSource implements DataSource, MimeConstants {

  private InputStream in = null;
  private InternetHeaders header = null;
  private ByteArrayOutputStream out = new ByteArrayOutputStream();
  private String contentType = null;
  private String messageId = null;

  private InputStreamDataSource() {
  }

  /** Constructor.
   *  @param input the input stream.
   *  @throws IOException if there was an error reading the stream.
   *  @throws MessagingException if there was an error initialising the
   *  datasource.
   */
  public InputStreamDataSource(InputStream input)
  throws IOException, MessagingException {
    this();
    initialise(input);
  }

  private void initialise(InputStream input)
  throws IOException, MessagingException {
    in = input;
    header = new InternetHeaders(in);
  }

  /** @see javax.activation.DataSource#getContentType() */
  @Override
  public String getContentType() {
    if (contentType == null) {

      String[] s = header.getHeader(HEADER_CONTENT_TYPE);
      contentType = s[0];
    }
    return contentType;
  }

  /** @see javax.activation.DataSource#getInputStream() */
  @Override
  public java.io.InputStream getInputStream()
  throws java.io.IOException {
    return in;
  }

  /** @see javax.activation.DataSource#getName() */
  @Override
  public String getName() {
    if (messageId == null) {

      String[] s = header.getHeader(HEADER_MESSAGE_ID);
      messageId = s[0];
    }
    return messageId;
  }

  /** @see javax.activation.DataSource#getOutputStream() */
  @Override
  public java.io.OutputStream getOutputStream()
  throws java.io.IOException {
    return out;
  }

  /** Return the bytes stored by the outputstream
   *  @return the byte array.
   */
  public byte[] getBytes() {
    return out.toByteArray();
  }

  /** Return the InternetHeader object for further querying.
   *  @return the InternetHeaders object.
   */
  public InternetHeaders getHeaders() {
    return header;
  }
}
