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

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import java.util.Enumeration;
import javax.mail.Header;
import javax.mail.internet.MimeBodyPart;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.stream.StreamUtil;
import com.adaptris.util.text.mime.BodyPartIterator;
import com.adaptris.util.text.mime.PartSelector;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Choose a specific mime part from an existing multipart message to become the payload of the AdaptrisMessage.
 * 
 * @config mime-part-selector-service
 * 
 * 
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("mime-part-selector-service")
@AdapterComponent
@ComponentProfile(summary = "Select a mime-part from the message and discards the others", tag = "service")
@DisplayOrder(order = {"selector", "markAsNonMime", "preserveHeadersAsMetadata", "headerPrefix", "preservePartHeadersAsMetadata",
    "partHeaderPrefix"})
public class MimePartSelector extends ServiceImp {

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean preserveHeadersAsMetadata;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean preservePartHeadersAsMetadata;
  @InputFieldDefault(value = "false")
  private Boolean markAsNonMime;
  @AdvancedConfig
  @InputFieldDefault(value = "")
  private String headerPrefix;
  @AdvancedConfig
  @InputFieldDefault(value = "")
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
      BodyPartIterator mp = MimeHelper.createBodyPartIterator(msg);
      MimeBodyPart part = selector.select(mp);
      if (part != null) {
        if (preserveHeadersAsMetadata()) {
          addHeadersAsMetadata(mp.getHeaders().getAllHeaders(), headerPrefix(), msg);
        }
        if (preservePartHeadersAsMetadata()) {
          addHeadersAsMetadata(part.getAllHeaders(), partHeaderPrefix(), msg);
        }
        StreamUtil.copyAndClose(part.getInputStream(), msg.getOutputStream());
        if (markAsNonMime()) {
          if (msg.headersContainsKey(CoreConstants.MSG_MIME_ENCODED)) {
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

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notNull(getSelector(), "selector");
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void closeService() {

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
    return BooleanUtils.toBooleanDefaultIfNull(getPreserveHeadersAsMetadata(), false);
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
    return BooleanUtils.toBooleanDefaultIfNull(getPreservePartHeadersAsMetadata(), false);
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
    selector = Args.notNull(mps, "selector");
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
    return BooleanUtils.toBooleanDefaultIfNull(getMarkAsNonMime(), false);
  }

  @Override
  public void prepare() throws CoreException {
  }


}
