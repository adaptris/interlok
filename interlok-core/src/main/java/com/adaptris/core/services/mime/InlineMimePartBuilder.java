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

import static com.adaptris.util.text.mime.MimeConstants.HEADER_CONTENT_TYPE;

import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.common.ByteArrayFromMetadata;
import com.adaptris.core.common.ByteArrayFromObjectMetadata;
import com.adaptris.core.common.ByteArrayFromPayload;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.types.MessageWrapper;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.text.mime.MimeUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Builds a MIME Body part by rendering a byte array as the content of the part.
 * 
 * <p>
 * Used as part of a {@link MultipartMessageBuilder} service; this constructs a {@link MimeBodyPart} from the configured
 * {@code body} configuration. Depending on your use case you might opt to use {@link ByteArrayFromPayload} or
 * {@link ByteArrayFromMetadata} to generate the actual contents for the part.
 * </p>
 * <p>
 * You can also specify the various headers that will be associated with the {@link MimeBodyPart} such as the {@code Content-ID},
 * {@code Content-Type} or {@code Content-Transfer-Encoding}. Additional non standard headers can be added by configuring a
 * {@link #setPartHeaderFilter(MetadataFilter)} to filter out metadata to include as part of the MimeBodyPart headers.
 * </p>
 * 
 * @config inline-mime-body-part-builder
 * @since 3.9.0
 */
@ComponentProfile(summary = "Builds a MIME Body part using a byte array", since = "3.9.0")
@DisplayOrder(order = {"contentId", "contentType", "contentEncoding", "body", "partHeaderFilter"})
@XStreamAlias("inline-mime-body-part-builder")
public class InlineMimePartBuilder implements MimePartBuilder {

  private static final GuidGenerator ID_GEN = new GuidGenerator();

  @InputFieldDefault(value = "the payload")
  @Valid
  private MessageWrapper<byte[]> body;
  @InputFieldDefault(value = "RemoveAllMetaddata")
  @Valid
  private MetadataFilter partHeaderFilter;
  @InputFieldDefault(value = "none")
  @InputFieldHint(expression = true)
  @AdvancedConfig
  private String contentEncoding;
  @InputFieldDefault(value = "application/octet-stream")
  @InputFieldHint(expression = true)
  private String contentType;
  @InputFieldDefault(value = "auto-generated GUID")
  @InputFieldHint(expression = true)
  private String contentId;

  public InlineMimePartBuilder() {

  }


  @Override
  public MimeBodyPart build(AdaptrisMessage msg) throws Exception {
    InternetHeaders hdrs = new InternetHeaders();
    byte[] encodedData = MimeUtils.encodeData(body().wrap(msg), contentEncoding(msg), hdrs);
    hdrs.addHeader(HEADER_CONTENT_TYPE, contentType(msg));
    // This allows the metadata filter to override the content-type.
    MetadataCollection metadata = partHeaderFilter().filter(msg);
    metadata.forEach((e) -> {
      hdrs.addHeader(e.getKey(), e.getValue());
    });
    MimeBodyPart part = new MimeBodyPart(hdrs, encodedData);
    // This means the content-id is always from the configured contentID expression.
    part.setContentID(contentId(msg));
    return part;
  }


  public MessageWrapper<byte[]> getBody() {
    return body;
  }

  /**
   * Set where the body of the MimeBodyPart is going to come from.
   * 
   * @param body the location of the body for the mime part; the default if not specified is the payload as a byte-array.
   * @see ByteArrayFromMetadata
   * @see ByteArrayFromObjectMetadata
   * @see ByteArrayFromPayload
   */
  public void setBody(MessageWrapper<byte[]> body) {
    this.body = Args.notNull(body, "body");
  }

  public MetadataFilter getPartHeaderFilter() {
    return partHeaderFilter;
  }

  /**
   * Set any additional headers that need to be set for this nested part.
   * 
   * @param filter the metadata filter.
   */
  public void setPartHeaderFilter(MetadataFilter filter) {
    this.partHeaderFilter = filter;
  }

  public String getContentEncoding() {
    return contentEncoding;
  }

  /**
   * Set the Content-Transfer-Encoding for the part.
   * 
   * <p>
   * Set the encoding of the mime part; if the mime part contains text you probably don't need to
   * specify this; any RFC2045 value is supported such as
   * {@code base64,quoted-printable,uuencode,x-uuencode,x-uue,binary,7bit,8bit} and is passed
   * directly to {@code MimeUtility#encode(java.io.OutputStream, String, String)}.
   * </p>
   * 
   * @param s the Content-Transfer-Encoding, which supports the {@code %message{}} syntax to resolve
   *        metadata, default is 'null', no encoding.
   */
  public void setContentEncoding(String s) {
    this.contentEncoding = s;
  }

  public String getContentType() {
    return contentType;
  }

  /**
   * Set the Content-Type for the part.
   * 
   * @param s the Content-Type, which supports the {@code %message{}} syntax to resolve metadata; if
   *        not specified defaults to {@code application/octet-stream}
   */
  public void setContentType(String s) {
    this.contentType = s;
  }

  public String getContentId() {
    return contentId;
  }

  /**
   * Set the Content-ID for the part,
   * 
   * @param s the Content-ID, which supports the {@code %message{}} syntax to resolve metadata;
   *        defaults to a new GUID if no value is specified.
   */
  public void setContentId(String s) {
    this.contentId = s;
  }

  public InlineMimePartBuilder withBody(MessageWrapper<byte[]> body) {
    setBody(body);
    return this;
  }

  public InlineMimePartBuilder withContentEncoding(String s) {
    setContentEncoding(s);
    return this;
  }

  public InlineMimePartBuilder withContentId(String s) {
    setContentId(s);
    return this;
  }

  public InlineMimePartBuilder withContentType(String s) {
    setContentType(s);
    return this;
  }

  public InlineMimePartBuilder withPartHeaderFilter(MetadataFilter filter) {
    setPartHeaderFilter(filter);
    return this;
  }

  private MetadataFilter partHeaderFilter() {
    return ObjectUtils.defaultIfNull(getPartHeaderFilter(), new RemoveAllMetadataFilter());
  }

  private String contentEncoding(AdaptrisMessage m) {
    return m.resolve(getContentEncoding());
  }

  private String contentType(AdaptrisMessage msg) {
    return StringUtils.defaultIfBlank(msg.resolve(getContentType()), "application/octet-stream");
  }

  private String contentId(AdaptrisMessage msg) {
    return StringUtils.defaultIfBlank(msg.resolve(getContentId()), ID_GEN.getUUID());
  }

  private MessageWrapper<byte[]> body() {
    return ObjectUtils.defaultIfNull(getBody(), (m) -> {
      // Since we know it's an adaptrisMessage, otherwise some dirtiness with ByteArrayOutputStreams
      return ((AdaptrisMessage) m).getPayload();
    });
  }
}
