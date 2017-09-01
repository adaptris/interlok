/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.mail;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Store mail headers as metadata.
 * 
 * <p>
 * It's possible to have multiple mail headers of the same name (e.g. {@code Received} is often present many times indicating the
 * path through various MTAs). In this instance, only 1 of them will be preserved.
 * </p>
 * 
 * @author lchan
 *
 */
@XStreamAlias("mail-headers-as-metadata")
public class MetadataMailHeaders implements MailHeaderHandler {

  @InputFieldDefault(value = "")
  @InputFieldHint(style = "BLANKABLE")
  private String prefix;

  @AdvancedConfig
  @Valid
  private MetadataFilter headerFilter;

  public MetadataMailHeaders() {

  }

  public MetadataMailHeaders(String prefix) {
    this();
    setPrefix(prefix);
  }

  @Override
  public void handle(MimeMessage mime, AdaptrisMessage msg) throws MessagingException {
    Set<MetadataElement> metadata = new HashSet<MetadataElement>();
    Enumeration headers = mime.getAllHeaders();
    String pfx = StringUtils.defaultIfBlank(getPrefix(), "");
    while (headers.hasMoreElements()) {
      Header h = (Header) headers.nextElement();
      metadata.add(new MetadataElement(pfx + h.getName(), h.getValue()));
    }
    msg.setMetadata(filter().filter(metadata).toSet());
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public MetadataMailHeaders withPrefix(String p) {
    setPrefix(p);
    return this;
  }

  public MetadataFilter getHeaderFilter() {
    return headerFilter;
  }

  /**
   * Set the header filter to use.
   * <p>
   * In some instances, you might not want to preserve all mail headers (e.g. you don't care about the {@code Received-SPF} header).
   * Use a {@link MetadataFilter} to filter the keys before applying them to the message. If you configure a prefix, then you need
   * to take that into account when configuring the filter.
   * </p>
   * 
   * @param mf defaults to {@link NoOpMetadataFilter} if not specified.
   */
  public void setHeaderFilter(MetadataFilter mf) {
    this.headerFilter = Args.notNull(mf, "headerFilter");
  }

  public MetadataMailHeaders withHeaderFilter(MetadataFilter p) {
    setHeaderFilter(p);
    return this;
  }

  MetadataFilter filter() {
    return getHeaderFilter() != null ? getHeaderFilter() : new NoOpMetadataFilter();
  }
}
