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

package com.adaptris.core.services.splitter;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.text.mime.BodyPartIterator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>MessageSplitter</code> which allows a single <code>AdaptrisMessage</code> that contains multiple mime
 * parts to be split into <code>AdaptrisMessage[]</code>.
 * </p>
 * <p>
 * The Message must be a mime encoded message.
 * </p>
 * 
 * @config mime-part-splitter
 * 
 * @author lchan
 */
@XStreamAlias("mime-part-splitter")
@DisplayOrder(order = {"copyMetadata", "copyObjectMetadata", "preserveHeaders", "headerPrefix"})
public class MimePartSplitter extends MessageSplitterImp {
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean preserveHeaders;
  @AdvancedConfig
  @InputFieldDefault(value = "")
  private String headerPrefix;

  /**
   *
   * @see MessageSplitter#splitMessage(AdaptrisMessage)
   */
  @Override
  public List<AdaptrisMessage> splitMessage(AdaptrisMessage msg) throws CoreException {
    List<AdaptrisMessage> result = new ArrayList<AdaptrisMessage>();
    AdaptrisMessageFactory fac = selectFactory(msg);
    try {
      BodyPartIterator mp = MimeHelper.createBodyPartIterator(msg);
      while (mp.hasNext()) {
        AdaptrisMessage splitMsg = fac.newMessage();
        MimeBodyPart part = (MimeBodyPart) mp.next();
        copy(part, splitMsg);
        copyMetadata(msg, splitMsg);
        result.add(splitMsg);
      }
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    logR.trace("Split gave " + result.size() + " messages");
    return result;
  }

  private void copy(MimeBodyPart src, AdaptrisMessage dest) throws IOException, MessagingException {
    OutputStream out = null;
    InputStream in = null;
    try {
      in = src.getInputStream();
      out = dest.getOutputStream();
      IOUtils.copy(in, out);
      copyHeaders(src, dest);
    }
    finally {
      closeQuietly(out);
      closeQuietly(in);
    }
  }
  private void copyHeaders(MimeBodyPart src, AdaptrisMessage dest) throws MessagingException {
    if (preserveHeaders()) {
      Enumeration e = src.getAllHeaders();
      while (e.hasMoreElements()) {
        Header h = (Header) e.nextElement();
        dest.addMetadata(defaultIfEmpty(getHeaderPrefix(), "") + h.getName(), h.getValue());
      }
    }
    return;
  }

  /**
   * Get the preserve headers flag.
   *
   * @return the flag.
   */
  public Boolean getPreserveHeaders() {
    return preserveHeaders;
  }

  /**
   * Set the preserve headers flag.
   * <p>
   * If set to true, then an attempt is made to copy all the headers from the mime part as metadata to the AdaptrisMessage object.
   * Each header can optionally be prefixed with the value specfied by the value of {@link #getHeaderPrefix()}
   * </p>
   *
   * @param b true or false.
   * @see #setHeaderPrefix(String)
   */
  public void setPreserveHeaders(Boolean b) {
    preserveHeaders = b;
  }

  public boolean preserveHeaders() {
    return getPreserveHeaders() != null ? getPreserveHeaders().booleanValue() : false;
  }

  /**
   * Set the header prefix.
   * <p>
   * The header prefix is used to prefix any headers that are preserved from the Mime Part
   * </p>
   *
   * @param s the prefix.
   */
  public void setHeaderPrefix(String s) {
    headerPrefix = s;
  }

  /**
   * Get the header prefix.
   *
   * @return the header prefix
   */
  public String getHeaderPrefix() {
    return headerPrefix;
  }

}
