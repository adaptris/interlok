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

package com.adaptris.core.services.mime;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.mime.MultiPartInput;
import com.adaptris.util.text.mime.PartSelector;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Choose a specific mime part from an existing multipart message to become the payload of the AdaptrisMessage.
 * 
 * @config mime-part-selector-service
 * 
 * @license BASIC
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("mime-part-selector-service")
public class MimePartSelector extends ServiceImp {

  private Boolean preserveHeadersAsMetadata;
  private Boolean preservePartHeadersAsMetadata;
  private Boolean markAsNonMime;
  private String headerPrefix;
  private String partHeaderPrefix;
  @NotNull
  @Valid
  private PartSelector selector;


  public MimePartSelector() {
  }

  /**
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      MultiPartInput mp = MimeHelper.create(msg, false);
      MimeBodyPart part = selector.select(mp);
      if (part != null) {
        if (preserveHeadersAsMetadata()) {
          addHeadersAsMetadata(msg);
        }
        if (preservePartHeadersAsMetadata()) {
          addHeadersAsMetadata(part.getAllHeaders(), partHeaderPrefix(), msg);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtil.copyStream(part.getInputStream(), out);
        out.close();
        msg.setPayload(out.toByteArray());
        if (markAsNonMime()) {
          if (msg.containsKey(CoreConstants.MSG_MIME_ENCODED)) {
            msg.removeMetadata(msg.getMetadata(CoreConstants.MSG_MIME_ENCODED));
          }
        }
      }
      else {
        log.warn("Could not select a MimePart for extraction, ignoring");
      }
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  private void addHeadersAsMetadata(Enumeration e, String prefix,
                                    AdaptrisMessage msg) throws Exception {
    while (e.hasMoreElements()) {
      Header hdr = (Header) e.nextElement();
      msg.addMetadata(prefix + hdr.getName(), hdr.getValue());
    }
  }

  private void addHeadersAsMetadata(AdaptrisMessage msg) throws Exception {
    InternetHeaders hdrs = parseHeaders(msg);
    addHeadersAsMetadata(hdrs.getAllHeaders(), headerPrefix(), msg);
  }

  private InternetHeaders parseHeaders(AdaptrisMessage msg)
      throws MessagingException, IOException {
    InputStream in = msg.getInputStream();
    InternetHeaders hdrs = new InternetHeaders();
    hdrs.load(in);
    in.close();
    return hdrs;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  @Override
  public void close() {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    if (selector == null) {
      throw new CoreException("Mime part Selector may not be null");
    }
  }

  /**
   * @return the preserveHeadersAsMetadata
   */
  public Boolean getPreserveHeadersAsMetadata() {
    return preserveHeadersAsMetadata;
  }

  /**
   * Specify whether to preserve parsed mime headers as metadata.
   *
   * @param b the preserveHeadersAsMetadata to set, default is false
   */
  public void setPreserveHeadersAsMetadata(Boolean b) {
    preserveHeadersAsMetadata = b;
  }

  boolean preserveHeadersAsMetadata() {
    return getPreserveHeadersAsMetadata() != null ? getPreserveHeadersAsMetadata().booleanValue() : false;
  }

  /**
   * @return the headerPrefix
   */
  public String getHeaderPrefix() {
    return headerPrefix;
  }

  /**
   * Specify the header prefix to be used when preserving parsed mime headers.
   *
   * @param s the headerPrefix to set, default is ""
   */
  public void setHeaderPrefix(String s) {
    headerPrefix = s;
  }

  String headerPrefix() {
    return defaultIfEmpty(getHeaderPrefix(), "");
  }

  /**
   * @return the partHeaderPrefix
   */
  public String getPartHeaderPrefix() {
    return partHeaderPrefix;
  }

  /**
   * Specify the header prefix to be used when preserving the parts headers.
   *
   * @param s the partHeaderPrefix to set, default is ""
   */
  public void setPartHeaderPrefix(String s) {
    partHeaderPrefix = s;
  }

  String partHeaderPrefix() {
    return defaultIfEmpty(getPartHeaderPrefix(), "");
  }

  /**
   * @return the preservePartHeadersAsMetadata
   */
  public Boolean getPreservePartHeadersAsMetadata() {
    return preservePartHeadersAsMetadata;
  }

  /**
   * Specify whether to preserve the parts headers as metadata.
   *
   * @param b the preservePartHeadersAsMetadata to set, default false.
   */
  public void setPreservePartHeadersAsMetadata(Boolean b) {
    preservePartHeadersAsMetadata = b;
  }

  boolean preservePartHeadersAsMetadata() {
    return getPreservePartHeadersAsMetadata() != null ? getPreservePartHeadersAsMetadata().booleanValue() : false;
  }

  /**
   * @return the selector
   */
  public PartSelector getSelector() {
    return selector;
  }

  /**
   * Set the mime part selector.
   *
   * @param mps the selector to set
   */
  public void setSelector(PartSelector mps) {
    selector = mps;
  }

  /**
   * @return the markAsNonMime
   */
  public Boolean getMarkAsNonMime() {
    return markAsNonMime;
  }

  /**
   * After processing, mark the AdaptrisMessage as non longer being MimeEncoded.
   *
   * @param b the markAsNonMime to set
   * @see CoreConstants#MSG_MIME_ENCODED
   */
  public void setMarkAsNonMime(Boolean b) {
    markAsNonMime = b;
  }

  boolean markAsNonMime() {
    return getMarkAsNonMime() != null ? getMarkAsNonMime().booleanValue() : false;
  }

  @Override
  public void prepare() throws CoreException {
  }


}
