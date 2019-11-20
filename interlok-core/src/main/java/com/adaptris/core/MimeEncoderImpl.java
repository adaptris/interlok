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

package com.adaptris.core;

import static com.adaptris.core.util.MetadataHelper.convertFromProperties;
import static com.adaptris.core.util.MetadataHelper.convertToProperties;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.validation.constraints.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.PropertyHelper;
import com.adaptris.util.text.mime.BodyPartIterator;
import com.adaptris.util.text.mime.ByteArrayDataSource;
import com.adaptris.util.text.mime.MimeConstants;

public abstract class MimeEncoderImpl extends AdaptrisMessageEncoderImp {

  protected static final String PAYLOAD_CONTENT_ID = "AdaptrisMessage/payload";
  protected static final String METADATA_CONTENT_ID = "AdaptrisMessage/metadata";
  protected static final String EXCEPTION_CONTENT_ID = "AdaptrisMessage/exception";

  @Pattern(regexp = "base64|quoted-printable|uuencode|x-uuencode|x-uue|binary|7bit|8bit")
  @AdvancedConfig
  private String metadataEncoding;
  @Pattern(regexp = "base64|quoted-printable|uuencode|x-uuencode|x-uue|binary|7bit|8bit")
  @AdvancedConfig
  private String payloadEncoding;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean retainUniqueId;

  public MimeEncoderImpl() {
    super();
  }

  protected MimeBodyPart payloadAsMimePart(AdaptrisMessage m) throws Exception {
    MimeBodyPart p = new MimeBodyPart();
    p.setDataHandler(new DataHandler(new MessageDataSource(m)));
    if (!isEmpty(getPayloadEncoding())) {
      p.setHeader(MimeConstants.HEADER_CONTENT_ENCODING, getPayloadEncoding());
    }
    return p;
  }

  protected MimeBodyPart asMimePart(Exception e) throws Exception {
    MimeBodyPart p = new MimeBodyPart();
    try (ByteArrayOutputStream out = new ByteArrayOutputStream(); PrintStream printer = new PrintStream(out, true)) {
      e.printStackTrace(printer);
      p.setDataHandler(new DataHandler(new ByteArrayDataSource(out.toByteArray())));
    }
    return p;
  }

  protected void addPartsToMessage(BodyPartIterator input, AdaptrisMessage msg) throws IOException, MessagingException {
    MimeBodyPart payloadPart = Args.notNull(input.getBodyPart(PAYLOAD_CONTENT_ID), "payload");
    MimeBodyPart metadataPart = Args.notNull(input.getBodyPart(METADATA_CONTENT_ID), "metadata");
    try (InputStream payloadIn = payloadPart.getInputStream();
        InputStream metadata = metadataPart.getInputStream();
        OutputStream out = msg.getOutputStream()) {
      IOUtils.copy(payloadIn, out);
      msg.setMetadata(getMetadataSet(metadata));
    }
    if (retainUniqueId()) {
      msg.setUniqueId(input.getMessageID());
    }
  }

  protected void addPartsToMessage(BodyPartIterator input, MultiPayloadAdaptrisMessage msg) throws IOException, MessagingException {
    for (int i = 0; i < input.size(); i++) {
      MimeBodyPart payloadPart = Args.notNull(input.getBodyPart(i), "payload");
      String id = payloadPart.getContentID();
      if (!id.startsWith(PAYLOAD_CONTENT_ID)) {
        continue;
      }
      id = id.substring(PAYLOAD_CONTENT_ID.length() + 1);
      msg.switchPayload(id);
      try (InputStream payloadIn = payloadPart.getInputStream();
           OutputStream out = msg.getOutputStream()) {
        IOUtils.copy(payloadIn, out);
      }
    }
    MimeBodyPart metadataPart = Args.notNull(input.getBodyPart(METADATA_CONTENT_ID), "metadata");
    try (InputStream metadata = metadataPart.getInputStream()) {
      msg.setMetadata(getMetadataSet(metadata));
    }
    if (retainUniqueId()) {
      msg.setUniqueId(input.getMessageID());
    }
  }

  /**
   * <p>
   * Returns the payload MIME encoding.
   * </p>
   *
   * @return the payload MIME encoding
   */
  public String getPayloadEncoding() {
    return payloadEncoding;
  }

  /**
   * <p>
   * Returns the metadata MIME encoding.
   * </p>
   *
   * @return the metadata MIME encoding
   */
  public String getMetadataEncoding() {
    return metadataEncoding;
  }

  /**
   * <p>
   * Sets the payload MIME encoding.
   * </p>
   *
   * @param encoding the payload MIME encoding
   */
  public void setPayloadEncoding(String encoding) {
    payloadEncoding = encoding;
  }

  /**
   * <p>
   * Sets the metadata MIME encoding.
   * </p>
   *
   * @param encoding the metadata MIME encoding
   */
  public void setMetadataEncoding(String encoding) {
    metadataEncoding = encoding;
  }

  /**
   * <p>
   * Returns true if the original ID of a decoded message should be retained for the new message.
   * </p>
   * 
   * @return true if the original ID of a decoded message should be retained for the new message
   */
  public Boolean getRetainUniqueId() {
    return retainUniqueId;
  }

  /**
   * <p>
   * Sets whether the original ID of a decoded message should be retained for the new message.
   * </p>
   * 
   * @param b true if the original ID should be retained
   */
  public void setRetainUniqueId(Boolean b) {
    retainUniqueId = b;
  }

  public boolean retainUniqueId() {
    return BooleanUtils.toBooleanDefaultIfNull(getRetainUniqueId(), false);
  }


  protected static byte[] getMetadata(AdaptrisMessage msg) throws IOException {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Properties metadata = convertToProperties(msg.getMetadata());
      metadata.store(out, "");
      return out.toByteArray();
    }
  }

  protected static Set<MetadataElement> getMetadataSet(InputStream in) throws IOException {
    return convertFromProperties(PropertyHelper.loadQuietly(in));
  }

  
  private static class MessageDataSource implements DataSource {
    private AdaptrisMessage wrapped;

    private MessageDataSource(AdaptrisMessage msg) {
      wrapped = msg;
    }
    @Override
    public String getContentType() {
      return "application/octet-stream";
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return wrapped.getInputStream();
    }

    @Override
    public String getName() {
      return wrapped.getUniqueId();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      throw new UnsupportedOperationException();
    }
  }
}
