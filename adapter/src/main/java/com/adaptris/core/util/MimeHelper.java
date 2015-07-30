/*
 * $RCSfile: MimeHelper.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/11/12 10:26:54 $
 * $Author: lchan $
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
import com.adaptris.util.text.mime.MimeConstants;
import com.adaptris.util.text.mime.MultiPartInput;

/**
 * Utility for handling MIME messages.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public final class MimeHelper {
  private static final String MIME_BOUNDARY_PREFIX = "--";
  private static final String MULTIPART_MIXED_TYPE = "multipart/mixed"
      + "; boundary=";
  private static final String CRLF = "\r\n";

  /**
   * Parse an AdaptrisMessage into a MultiPartInput.
   *
   * @param msg the Message to parse.
   * @return a MultiPartInput ready for iterating, each invocation of
   *         {@link MultiPartInput#next() } returns a byte arra
   * @throws IOException if the message could not be parsed.
   * @see #create(AdaptrisMessage, boolean)
   * @throws MessagingException on any underlying MIME Exception
   */
  public static MultiPartInput create(AdaptrisMessage msg) throws IOException,
      MessagingException {
    return create(msg, true);
  }

  /**
   * Parse an AdaptrisMessage into a MultiPartInput.
   *
   * @param msg the Message to parse.
   * @param simplifiedIterator if true, then each invocation of
   *          {@link MultiPartInput#next() } returns a byte array rather than a
   *          {@link MimeBodyPart}.
   * @return a MultiPartInput ready for iterating.
   * @throws IOException if the message could not be parsed.
   * @throws MessagingException on any underlying MIME Exception
   * @see MultiPartInput#next()
   */
  public static MultiPartInput create(AdaptrisMessage msg,
                                      boolean simplifiedIterator)
      throws IOException, MessagingException {
    MultiPartInput result = null;
    InputStream in = null;
    OutputStream out = null;
    try {
      in = msg.getInputStream();
      result = new MultiPartInput(in, simplifiedIterator);
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
    catch (Exception e) {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
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
    InputStream in = null;
    OutputStream out = null;
    AdaptrisMessage parseable = src.getFactory().newMessage();
    try {
      out = parseable.getOutputStream();
      in = src.getInputStream();
      PrintWriter p = new PrintWriter(out);
      p.print(MimeConstants.HEADER_CONTENT_ID + ": " + src.getUniqueId() + CRLF);
      p.print(MimeConstants.HEADER_CONTENT_TYPE + ": " + MULTIPART_MIXED_TYPE
          + "\"" + boundary + "\"" + CRLF);
      p.print(MimeConstants.HEADER_MIME_VERSION + ": 1.0" + CRLF);
      p.print(CRLF);
      p.flush();
      IOUtils.copy(in, out);
    }
    finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
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
