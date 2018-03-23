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

package com.adaptris.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.stream.UnbufferedLineInputStream;
import com.adaptris.util.text.mime.BodyPartIterator;
import com.adaptris.util.text.mime.ByteArrayIterator;
import com.adaptris.util.text.mime.MimeConstants;
import com.adaptris.util.text.mime.MultiPartInput;

/**
 * Utility for handling MIME messages.
 *
 */
public abstract class MimeHelper {
  private static final String MIME_BOUNDARY_PREFIX = "--";
  private static final String MULTIPART_MIXED_TYPE = "multipart/mixed"
      + "; boundary=";
  private static final String CRLF = "\r\n";

  /**
   * Convenience method to create a {@link ByteArrayIterator} on a message allowing you to iterate a mime-payload.
   * 
   */
  public static ByteArrayIterator createByteArrayIterator(AdaptrisMessage msg) throws IOException, MessagingException {
    ByteArrayIterator result = null;
    try (InputStream in = msg.getInputStream()) {
      result = new ByteArrayIterator(in);
    } catch (Exception e) {
      String mimeBoundary = getBoundary(msg);
      result = createByteArrayIterator(mimeFaker(msg, mimeBoundary));
    }
    if (result == null) {
      throw new IOException("Could not parse " + msg.getUniqueId() + " into a standard MIME Multipart");
    }
    return result;
  }
  
  /**
   * Convenience method to create a {@link BodyPartIterator} allowing you to iterate a mime-payload.
   * 
   */
  public static BodyPartIterator createBodyPartIterator(AdaptrisMessage msg) throws IOException, MessagingException {
    BodyPartIterator result = null;
    try (InputStream in = msg.getInputStream()) {
      result = new BodyPartIterator(in);
    } catch (Exception e) {
      String mimeBoundary = getBoundary(msg);
      result = createBodyPartIterator(mimeFaker(msg, mimeBoundary));
    }
    if (result == null) {
      throw new IOException("Could not parse " + msg.getUniqueId() + " into a standard MIME Multipart");
    }
    return result;
  }

  /**
   * Parse an AdaptrisMessage into a MultiPartInput.
   *
   * @param msg the Message to parse.
   * @return a MultiPartInput ready for iterating, each invocation of {@link MultiPartInput#next() } returns a byte array
   * @throws IOException if the message could not be parsed.
   * @see #create(AdaptrisMessage, boolean)
   * @throws MessagingException on any underlying MIME Exception
   * @deprecated since 3.7.2 use {@link #createByteArrayIterator(AdaptrisMessage)} or
   *             {@link #createBodyPartIterator(AdaptrisMessage)} instead.
   */
  @Deprecated
  public static MultiPartInput create(AdaptrisMessage msg) throws IOException,
      MessagingException {
    return create(msg, true);
  }

  /**
   * Parse an AdaptrisMessage into a MultiPartInput.
   *
   * @param msg the Message to parse.
   * @param simplifiedIterator if true, then each invocation of {@link MultiPartInput#next() } returns a byte array rather than a
   *          {@link MimeBodyPart}.
   * @return a MultiPartInput ready for iterating.
   * @throws IOException if the message could not be parsed.
   * @throws MessagingException on any underlying MIME Exception
   * @see MultiPartInput#next()
   * @deprecated since 3.7.2 use {@link #createByteArrayIterator(AdaptrisMessage)} or
   *             {@link #createBodyPartIterator(AdaptrisMessage)} instead.
   */
  @Deprecated
  public static MultiPartInput create(AdaptrisMessage msg,
                                      boolean simplifiedIterator)
      throws IOException, MessagingException {
    MultiPartInput result = null;
    try (InputStream in = msg.getInputStream()) {
      result = new MultiPartInput(in, simplifiedIterator);
    }
    catch (Exception e) {
      String mimeBoundary = getBoundary(msg);
      result = create(mimeFaker(msg, mimeBoundary), simplifiedIterator);
    }
    if (result == null) {
      throw new IOException("Could not parse " + msg.getUniqueId()
          + " into a standard MIME Multipart");
    }
    return result;
  }

  // Attempt to treat it as a fake multipart bug#822
  private static AdaptrisMessage mimeFaker(AdaptrisMessage src, String boundary)
      throws IOException, MessagingException {
    AdaptrisMessage parseable = src.getFactory().newMessage();
    try (InputStream in = src.getInputStream(); OutputStream out = parseable.getOutputStream()) {
      PrintWriter p = new PrintWriter(out);
      p.print(MimeConstants.HEADER_CONTENT_ID + ": " + src.getUniqueId() + CRLF);
      p.print(MimeConstants.HEADER_CONTENT_TYPE + ": " + MULTIPART_MIXED_TYPE
          + "\"" + boundary + "\"" + CRLF);
      p.print(MimeConstants.HEADER_MIME_VERSION + ": 1.0" + CRLF);
      p.print(CRLF);
      p.flush();
      IOUtils.copy(in, out);
    }
    return parseable;
  }

  private static String getBoundary(AdaptrisMessage msg) throws IOException {
    UnbufferedLineInputStream in = null;
    String mimeBoundary = null;
    try {
      in = new UnbufferedLineInputStream(msg.getInputStream());
      mimeBoundary = in.readLine();
      if (!mimeBoundary.startsWith(MIME_BOUNDARY_PREFIX)) {
        throw new IOException("Could not parse " + msg.getUniqueId()
            + " into a standard MIME Multipart");
      }
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    return mimeBoundary.substring(2).trim();
  }
}
