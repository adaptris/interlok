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

package com.adaptris.util.text.mime;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle creation of multipart mime output.
 * <p>
 * When a getBytes() is performed, then the output is something similar to
 * 
 * <pre>
 * {@code 
 *  Message-ID: db03b6ef-ffff-ffc0-019b-04e2b47a4d8e
 *  Mime-Version: 1.0
 *  Content-Type: multipart/mixed;
 *    boundary="----=_Part_1_33189144.1047351507632"
 *  Content-Length: 383
 * 
 *  ------=_Part_1_33189144.1047351507632
 *  Content-Id: AdaptrisMessage/payload
 * 
 *  This is the message 03/11/2003 01:57 PM
 * 
 *  ------=_Part_1_33189144.1047351507632
 *  Content-Id: AdaptrisMessage/metadata
 * 
 *  workflowId=loopback
 *  previousGuid=db03b6ef-ffff-ffc0-019b-04e2b47a4d8e
 *  emailmessageid=<200303110257.h2B2v9sC030299@localhost.localdomain>
 * 
 *  ------=_Part_1_33189144.1047351507632--
 * }
 * </pre>
 * 
 * <p>
 * With each additional call to <code>addPart()>/code> an additional mime
 *  bodypart is created.  Repeated calls to <code>getBytes()</code> will return the latest version of the MimeMultiPart as written
 * out. There is no guarantee that this is the same as the last call to <code>getBytes()</code> as the part id's may change.
 * </p>
 * <p>
 * If non-unique content-id's are used for each bodypart, then an invocation of <code>removePart(String)</code> will remove
 * <b>all</b> matching body parts.
 * </p>
 */
public class MultiPartOutput implements MimeConstants {
  private static final String DEFAULT_SUB_TYPE = "mixed";

  // private MimeMultipart multipart;
  private String messageId;
  private transient Logger logR = LoggerFactory.getLogger(this.getClass());
  private InternetHeaders mimeHeader;
  private List<KeyedBodyPart> parts;
  private String subType = "mixed";

  private MultiPartOutput() throws MessagingException {
    // multipart = new MimeMultipart();
    mimeHeader = new InternetHeaders();
    mimeHeader.setHeader(HEADER_MIME_VERSION, "1.0");
    parts = new ArrayList<KeyedBodyPart>();
    subType = DEFAULT_SUB_TYPE;
  }

  /**
   * Constructor.
   * <p>
   * This implicitly sets the multipart sub-type to be mixed.
   * </p>
   * 
   * @param mimeId the Message-ID header to assign to this multi-part
   * @throws MessagingException if the bytes did not contain a valid MimeMultiPart
   */
  public MultiPartOutput(String mimeId) throws MessagingException {
    this();
    if (mimeId != null && !mimeId.equals("")) {
      mimeHeader.setHeader(HEADER_MESSAGE_ID, mimeId);
    }
    else {
      throw new MessagingException("Message Id cannot be null");
    }
  }

  /**
   * Constructor.
   * 
   * @param mimeId the Message-ID header to assign to this multi-part
   * @param subtype the multi-part subtype.
   * @throws MessagingException if there was a failure to create the underlying Mime Multipart
   */
  public MultiPartOutput(String mimeId, String subtype) throws MessagingException {
    this(mimeId);
    this.subType = StringUtils.defaultIfBlank(subtype, DEFAULT_SUB_TYPE);
  }

  /**
   * Add a new part to the mime multipart.
   * 
   * @param payload the data.
   * @param encoding the encoding to apply
   * @param contentId the id to set the content with.
   * @throws MessagingException if there was a failure adding the part. MimeMultiPart
   * @throws IOException if there was an IOException
   */
  public void addPart(String payload, String encoding, String contentId) throws MessagingException, IOException {
    InternetHeaders header = new InternetHeaders();
    byte[] encodedBytes = encodeData(payload, encoding, header);
    MimeBodyPart part = new MimeBodyPart(header, encodedBytes);
    this.addPart(part, contentId);
  }

  /**
   * Add a new part to the mime multipart.
   * 
   * @param payload the data.
   * @param contentId the id to set the content with.
   * @throws MessagingException if there was a failure adding the part. MimeMultiPart
   * @throws IOException if there was an IOException
   */
  public void addPart(String payload, String contentId) throws MessagingException, IOException {
    addPart(payload, null, contentId);
  }

  /**
   * Add a new part to the mime multipart.
   * 
   * @param payload the data.
   * @param contentId the id to set the content with.
   * @throws MessagingException if there was a failure adding the part. MimeMultiPart
   * @throws IOException if there was an IOException
   */
  public void addPart(byte[] payload, String contentId) throws MessagingException, IOException {
    addPart(payload, null, contentId);
  }

  /**
   * Add a new part to the mime multipart.
   * 
   * @param payload the data.
   * @param encoding the encoding to apply
   * @param contentId the id to set the content with.
   * @throws MessagingException on error manipulating the bodypart
   * @throws IOException on general IO error.
   */
  public void addPart(byte[] payload, String encoding, String contentId) throws MessagingException, IOException {

    InternetHeaders header = new InternetHeaders();
    byte[] encodedBytes = encodeData(payload, encoding, header);
    MimeBodyPart part = new MimeBodyPart(header, encodedBytes);
    this.addPart(part, contentId);
  }

  /**
   * Add a new part to the mime multipart.
   * 
   * @param part an already existing mimebody part
   * @param contentId the id to set the content with.
   * @throws MessagingException on error manipulating the bodypart
   * @throws IOException on general IO error.
   */
  public void addPart(MimeBodyPart part, String contentId) throws MessagingException, IOException {
    part.setHeader(HEADER_CONTENT_ID, contentId);
    parts.add(new KeyedBodyPart(contentId, part));
  }

  /**
   * Remove a part from this multipart output.
   * <p>
   * If non-unique content-id's are used for each bodypart, then an invocation of <code>removePart(String)</code> will remove
   * <b>all</b> matching body parts.
   * </p>
   * 
   * @param contentId the content-id associated with a previously added part.
   * @throws MessagingException if there was a failure removing the part.
   * @throws IOException if there was an IOException
   */
  public void removePart(String contentId) throws MessagingException, IOException {

    ArrayList toRemove = new ArrayList();
    Iterator i = parts.iterator();
    while (i.hasNext()) {

      KeyedBodyPart k = (KeyedBodyPart) i.next();
      if (k.getKey().equals(contentId)) {
        toRemove.add(k);
      }
    }
    parts.removeAll(toRemove);
  }

  /**
   * Get bytes created by this multi-part.
   * 
   * @throws MessagingException if there was a failure retrieving the bytes.
   * @throws IOException if there was an IOException
   * @return the bytes represented by this MimeMultipartOutput
   */
  public byte[] getBytes() throws MessagingException, IOException {
    return toByteArray();
  }

  /**
   * Write the multipart to the given output stream.
   * 
   * @param out the output stream.
   * @throws MessagingException if there was a failure retrieving the bytes.
   * @throws IOException if there was an IOException
   */
  public void writeTo(OutputStream out) throws MessagingException, IOException {
    writeTo(out, null);
  }

  /**
   * Write the multipart to the given outputstream.
   * 
   * @param out the output stream.
   * @param tempFile if not null, then use the specified file to stream the parts to first.
   */
  public void writeTo(OutputStream out, File tempFile) throws MessagingException, IOException {
    if (tempFile == null) {
      inMemoryWrite(out);
    } else {
      writeViaTempfile(out, tempFile);
    }
  }

  private void writeViaTempfile(OutputStream out, File tempFile) throws MessagingException, IOException {
    try (FileOutputStream fileOut = new FileOutputStream(tempFile);
        CountingOutputStream counter = new CountingOutputStream(fileOut)) {
      MimeMultipart multipart = new MimeMultipart(subType);
      mimeHeader.setHeader(HEADER_CONTENT_TYPE, multipart.getContentType());
      // Write the part out to the stream first.
      for (KeyedBodyPart kbp : parts) {
        multipart.addBodyPart(kbp.getData());
      }
      multipart.writeTo(counter);
      counter.flush();
      mimeHeader.setHeader(HEADER_CONTENT_LENGTH, String.valueOf(counter.count()));
    }
    writeHeaders(mimeHeader, out);
    try (InputStream in = new FileInputStream(tempFile)) {
      IOUtils.copy(in, out);
    }
  }


  private void inMemoryWrite(OutputStream out) throws MessagingException, IOException {
    MimeMultipart multipart = new MimeMultipart(subType);
    ByteArrayOutputStream partOut = new ByteArrayOutputStream();
    mimeHeader.setHeader(HEADER_CONTENT_TYPE, multipart.getContentType());
    // Write the part out to the stream first.
    for (KeyedBodyPart kbp : parts) {
      multipart.addBodyPart(kbp.getData());
    }
    multipart.writeTo(partOut);
    mimeHeader.setHeader(HEADER_CONTENT_LENGTH, String.valueOf(partOut.size()));
    writeHeaders(mimeHeader, out);
    out.write(partOut.toByteArray());
  }

  /**
   * Set an arbitary header to the headers prefixed to the start of the multipart.
   * <p>
   * The Content-Type and Content-Length will always be overridden with the content-type and length of the mime multipart
   * </p>
   * 
   * @param key the key
   * @param value the value.
   * @see javax.mail.internet.InternetHeaders
   */
  public void setHeader(String key, String value) {
    mimeHeader.setHeader(key, value);
  }

  /**
   * Return the underlying mime header that will be used to write the headers.
   * 
   * @return the internet header.
   */
  public InternetHeaders getMimeHeader() {
    return mimeHeader;
  }

  private byte[] toByteArray() throws MessagingException, IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    writeTo(out);
    return out.toByteArray();
  }

  /**
   * Encode the data.
   */
  private static byte[] encodeData(byte[] data, String encoding, InternetHeaders header) throws MessagingException, IOException {

    byte[] toEncode = data == null ? new byte[0] : data;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (OutputStream encodedOut = wrap(out, encoding, header)) {
      encodedOut.write(toEncode);
    }
    return out.toByteArray();
  }

  private static byte[] encodeData(String data, String encoding, InternetHeaders header) throws MessagingException, IOException {
    String toEncode = defaultIfEmpty(data, "");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (PrintStream print = new PrintStream(wrap(out, encoding, header))) {
      print.print(toEncode);
    }
    return out.toByteArray();
  }

  private static OutputStream wrap(OutputStream original, String encoding, InternetHeaders hdrs) throws MessagingException {
    OutputStream encodedOut = original;
    if (encoding != null) {
      encodedOut = MimeUtility.encode(original, encoding);
      hdrs.setHeader(HEADER_CONTENT_ENCODING, encoding);
    }
    return encodedOut;
  }

  /**
   * Write the internet headers out to the supplied outputstream
   */
  private static void writeHeaders(InternetHeaders header, OutputStream out) throws IOException, MessagingException {

    Enumeration e = header.getAllHeaderLines();
    PrintStream p = new PrintStream(out);
    while (e.hasMoreElements()) {
      p.println(e.nextElement().toString());
    }
    p.println("");
    p.flush();
  }

  private class KeyedBodyPart {

    private String id;
    private MimeBodyPart part;

    KeyedBodyPart(String contentId, MimeBodyPart bodypart) {
      this.id = contentId;
      this.part = bodypart;
    }

    MimeBodyPart getData() {
      return part;
    }

    String getKey() {
      return id;
    }

  }


  // We override every method becuase of INTERLOK-1926
  private class CountingOutputStream extends FilterOutputStream {
    private long count = 0;

    public CountingOutputStream(OutputStream out) {
      super(out);
    }

    @Override
    public void write(int b) throws IOException {
      out.write(b);
      count += 1;
    }

    @Override
    public void write(byte b[]) throws IOException {
      out.write(b, 0, b.length);
      count += b.length;
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
      if ((off | len | b.length - (len + off) | off + len) < 0) throw new IndexOutOfBoundsException();
      out.write(b, off, len);
      count += len;
    }

    long count() {
      return count;
    }
  }
}
