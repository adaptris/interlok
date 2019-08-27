/*
 * Copyright 2019 Adaptris Ltd.
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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.mail.internet.MimeBodyPart;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.text.mime.MultiPartOutput;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Constructs a multipart MIME payload from various sources.
 * 
 * @config multipart-message-builder
 * @since 3.9.0
 */
@XStreamAlias("multipart-message-builder")
@ComponentProfile(summary = "Creates a MIME payload from various sources", since = "3.9.0", tag = "mime")
public class MultipartMessageBuilder extends ServiceImp {

  @XStreamImplicit
  @NotNull
  private List<MimePartBuilder> mimeParts;
  @InputFieldDefault(value = "the messages unique-id")
  @InputFieldHint(expression = true)
  private String contentId;
  @InputFieldDefault(value = "mixed")
  @InputFieldHint(expression = true)
  private String mimeContentSubType;
  @AdvancedConfig
  @Valid
  @InputFieldDefault(value = "RemoveAllMetadataFilter")
  private MetadataFilter mimeHeaderFilter;

  public MultipartMessageBuilder() {
    setMimeParts(new ArrayList<MimePartBuilder>());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      MultiPartOutput output = createOutputPart(msg);
      for (MimePartBuilder builder : getMimeParts()) {
        MimeBodyPart part = builder.build(msg);
        output.addPart(part, part.getContentID());
      }
      try (OutputStream out = msg.getOutputStream()) {
        output.writeTo(out);
      }
      msg.addMetadata(CoreConstants.MSG_MIME_ENCODED, Boolean.TRUE.toString());
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  protected MultiPartOutput createOutputPart(AdaptrisMessage msg)
      throws Exception {
    MultiPartOutput output = new MultiPartOutput(contentId(msg), mimeContentSubType(msg));
    MetadataCollection metadata = mimeHeaderFilter().filter(msg);
    metadata.forEach((e) -> {
      output.setHeader(e.getKey(), e.getValue());
    });
    return output;
  }


  public List<MimePartBuilder> getMimeParts() {
    return mimeParts;
  }

  /**
   * Specify what is going to build the mime message.
   * 
   * @param parts the parts that will form the mime message.
   */
  public void setMimeParts(List<MimePartBuilder> parts) {
    this.mimeParts = Args.notNull(parts, "mime-parts");
  }

  public MetadataFilter getMimeHeaderFilter() {
    return mimeHeaderFilter;
  }

  /**
   * Set any additional headers that need to be set for this Mime Message
   * 
   * @param filter the metadata filter.
   */
  public void setMimeHeaderFilter(MetadataFilter filter) {
    this.mimeHeaderFilter = filter;
  }

  public String getContentId() {
    return contentId;
  }

  /**
   * Set the Content-ID for the Multipart,
   * 
   * @param s the Content-ID, which supports the {@code %message{}} syntax to resolve metadata;
   *        defaults to the messages unique id if no value is specified.
   */
  public void setContentId(String s) {
    this.contentId = s;
  }


  public String getMimeContentSubType() {
    return mimeContentSubType;
  }

  /**
   * Set the sub type for the Multipart
   * 
   * @param sub the content subtype, which supports the {@code %message{}} syntax to resolve
   *        metadata; defaults to 'mixed' if not specified.
   */
  public void setMimeContentSubType(String sub) {
    this.mimeContentSubType = sub;
  }

  public MultipartMessageBuilder withMimeHeaderFilter(MetadataFilter filter) {
    setMimeHeaderFilter(filter);
    return this;
  }

  public MultipartMessageBuilder withMimeParts(List<MimePartBuilder> list) {
    setMimeParts(list);
    return this;
  }

  public MultipartMessageBuilder withMimeParts(MimePartBuilder... builders) {
    return withMimeParts(new ArrayList<>(Arrays.asList(builders)));
  }

  public MultipartMessageBuilder withContentId(String s) {
    setContentId(s);
    return this;
  }

  public MultipartMessageBuilder withMimeContentSubType(String s) {
    setMimeContentSubType(s);
    return this;
  }

  private String contentId(AdaptrisMessage msg) {
    return StringUtils.defaultIfBlank(msg.resolve(getContentId()), msg.getUniqueId());
  }

  private MetadataFilter mimeHeaderFilter() {
    return ObjectUtils.defaultIfNull(getMimeHeaderFilter(), new RemoveAllMetadataFilter());
  }

  private String mimeContentSubType(AdaptrisMessage msg) {
    String sub = msg.resolve(getMimeContentSubType());
    return StringUtils.defaultIfBlank(sub, "mixed");
  }

}
