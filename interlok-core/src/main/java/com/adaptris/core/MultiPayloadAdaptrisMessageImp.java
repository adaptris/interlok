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

package com.adaptris.core;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.interlok.resolver.UnresolvableException;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.util.IdGenerator;

/**
 * The standard implementation of multi-payload messages;
 * {@link MultiPayloadAdaptrisMessage} implementation created by
 * {@link MultiPayloadMessageFactory}.
 *
 * @author aanderson
 * @see MultiPayloadAdaptrisMessage
 * @see MultiPayloadMessageFactory
 * @see AdaptrisMessageImp
 * @since 3.9.3
 */
@ComponentProfile(summary = "A multi-payload message implementation", tag = "multi-payload,message", since="3.9.3")
public class MultiPayloadAdaptrisMessageImp extends AdaptrisMessageImp implements MultiPayloadAdaptrisMessage {

  public final static String PAYLOAD_METADATA_KEY_PREFIX = "PAYLOAD_";
  public final static String PAYLOAD_METADATA_KEY_FORMAT = PAYLOAD_METADATA_KEY_PREFIX + "%s_%s";
  public static final String EXPLICIT_PAYLOAD_REGEXP = "^.*%payload_id\\{([\\w!\\$\"#&%'\\*\\+,\\-\\.:=]+)\\}.*$";
  public static final String IMPLICIT_PAYLOAD_REGEXP = "^.*%payload\\{id:([\\w!\\$\"#&%'\\*\\+,\\-\\.:=]+)\\}.*$";

  private static final transient Pattern normalPayloadResolver = Pattern.compile(EXPLICIT_PAYLOAD_REGEXP);
  private static final transient Pattern dotAllPayloadResolver = Pattern.compile(EXPLICIT_PAYLOAD_REGEXP, Pattern.DOTALL);
  private static final transient Pattern normalPayloadResolver2 = Pattern.compile(IMPLICIT_PAYLOAD_REGEXP);
  private static final transient Pattern dotAllPayloadResolver2 = Pattern.compile(IMPLICIT_PAYLOAD_REGEXP, Pattern.DOTALL);

  private Map<String, Payload> payloads = new HashMap<>();

  @NotNull
  private String currentPayloadId = DEFAULT_PAYLOAD_ID;

  public MultiPayloadAdaptrisMessageImp(@NotNull String payloadId, IdGenerator guid, AdaptrisMessageFactory messageFactory) {
    this(payloadId, guid, messageFactory, new byte[0]);
  }

  public MultiPayloadAdaptrisMessageImp(@NotNull String payloadId, IdGenerator guid, AdaptrisMessageFactory messageFactory, byte[] payload) {
    super(guid, messageFactory);
    addPayload(payloadId, payload);
  }

  public MultiPayloadAdaptrisMessageImp(@NotNull String payloadId, IdGenerator guid, AdaptrisMessageFactory messageFactory, String content, Charset encoding) {
    super(guid, messageFactory);
    addContent(payloadId, content, encoding != null ? encoding.toString() : null);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void switchPayload(@NotNull String payloadId) {
    currentPayloadId = payloadId;
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public boolean hasPayloadId(@NotNull String payloadId) {
    return payloads.containsKey(payloadId);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void setCurrentPayloadId(@NotNull String payloadId) {
    Payload payload = payloads.remove(currentPayloadId);
    currentPayloadId = payloadId;
    payloads.put(currentPayloadId, payload);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public String getCurrentPayloadId() {
    return currentPayloadId;
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public Set<String> getPayloadIDs() {
    return payloads.keySet();
  }

  /**
   * Check if the current payload contains a metadata with the give key
   *
   * @param key
   * @return true if the current payload contains a metadata with the give key
   */
  @Override
  public boolean payloadHeadersContainsKey(String key) {
    return payloadHeadersContainsKey(getCurrentPayloadId(), key);
  }

  /**
   * Check if the payload with the given id contains a metadata with the give key
   *
   * @param payloadId
   * @param key
   * @return true if the payload with the given id contains a metadata with the give key
   */
  @Override
  public boolean payloadHeadersContainsKey(String payloadId, String key) {
    return headersContainsKey(payloadMessageHeaderKey(payloadId, key));
  }

  /**
   * Return metadata value for the current payload and key
   *
   * @param key
   * @return metadata value for the current payload and key
   */
  @Override
  public String getPayloadMessageHeaderValue(String key) {
    return getPayloadMessageHeaderValue(getCurrentPayloadId(), key);
  }

  /**
   * Return metadata value for the given payload id and key
   *
   * @param payloadId
   * @param key
   * @return metadata value for the given payload id and key
   */
  @Override
  public String getPayloadMessageHeaderValue(String payloadId, String key) {
    return getMetadataValue(payloadMessageHeaderKey(payloadId, key));
  }

  /**
   * Return all the metadata for the current payload removing the payload prefixes e.g. 'PAYLOAD_payload-id_key' will become 'key'
   *
   * @return all the metadata for the given payload removing the payload prefixes
   */
  @Override
  public Map<String, String> getPayloadMessageHeaders() {
    return getPayloadMessageHeaders(getCurrentPayloadId());
  }

  /**
   * Return all the metadata for a given payload id removing the payload prefixes e.g. 'PAYLOAD_payload-id_key' will become 'key'
   *
   * @param payloadId
   * @return all the metadata for a given payload id removing the payload prefixes
   */
  @Override
  public Map<String, String> getPayloadMessageHeaders(String payloadId) {
    String payloadKeyPrefix = String.format(PAYLOAD_METADATA_KEY_FORMAT, payloadId, "");
    return getMessageHeaders().entrySet().stream().filter(e -> e.getKey().startsWith(payloadKeyPrefix)).collect(Collectors.toMap(e -> {
      return e.getKey().substring(payloadKeyPrefix.length());
    }, e -> {
      return e.getValue();
    }));
  }

  /**
   * Add a metadata for the current payload
   *
   * @param key
   * @param value
   */
  @Override
  public void addPayloadMessageHeader(String key, String value) {
    addPayloadMessageHeader(getCurrentPayloadId(), key, value);
  }

  /**
   * Add a metadata for the given payload id
   *
   * @param payloadId
   * @param key
   * @param value
   */
  @Override
  public void addPayloadMessageHeader(String payloadId, String key, String value) {
    addMessageHeader(payloadMessageHeaderKey(payloadId, key), value);
  }

  /**
   * Remove a metadata for the current payload and the given key
   *
   * @param key
   */
  @Override
  public void removePayloadMessageHeader(String key) {
    removePayloadMessageHeader(getCurrentPayloadId(), key);
  }

  /**
   * Remove a metadata for the given payload id and the given key
   *
   * @param payloadId
   * @param key
   */
  @Override
  public void removePayloadMessageHeader(String payloadId, String key) {
    removeMessageHeader(payloadMessageHeaderKey(payloadId, key));
  }

  private String payloadMessageHeaderKey(String payloadId, String key) {
    return String.format(PAYLOAD_METADATA_KEY_FORMAT, payloadId, key);
  }

  /**
   * @see AdaptrisMessage#equivalentForTracking
   *      (com.adaptris.core.AdaptrisMessage).
   */
  @Override
  public boolean equivalentForTracking(AdaptrisMessage o) {
    if (!(o instanceof MultiPayloadAdaptrisMessage)) {
      return false;
    }
    MultiPayloadAdaptrisMessage other = (MultiPayloadAdaptrisMessage) o;
    if (!StringUtils.equals(getUniqueId(), other.getUniqueId())) {
      return false;
    }
    if (!getMetadata().equals(other.getMetadata())) {
      return false;
    }
    if (getPayloadCount() != other.getPayloadCount()) {
      return false;
    }
    for (String id : payloads.keySet()) {
      if (!other.hasPayloadId(id)) {
        return false;
      } else if (!Arrays.equals(getPayload(id), other.getPayload(id))) {
        return false;
      } else if (!StringUtils.equals(getContentEncoding(id), other.getContentEncoding(id))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Set the current payload data.
   *
   * @param bytes
   *          The payload data.
   * @see AdaptrisMessage#setPayload(byte[])
   */
  @Override
  public void setPayload(byte[] bytes) {
    addPayload(currentPayloadId, bytes);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void addPayload(@NotNull String payloadId, byte[] bytes) {
    byte[] pb;
    if (bytes == null) {
      pb = new byte[0];
    } else {
      pb = bytes;
    }
    Payload payload;
    if (payloads.containsKey(payloadId)) {
      payload = payloads.get(payloadId);
      payload.data = pb;
    } else {
      payload = new Payload(pb);
    }
    payloads.put(payloadId, payload);
    currentPayloadId = payloadId;
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void deletePayload(@NotNull String payloadId) {
    payloads.remove(payloadId);
  }

  /**
   * Get the current payload data.
   *
   * @return The payload data.
   * @see AdaptrisMessage#getPayload()
   */
  @Override
  public byte[] getPayload() {
    return getPayload(currentPayloadId);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public byte[] getPayload(@NotNull String payloadId) {
    return payloads.get(payloadId).payload();
  }

  /**
   * Get the current payload size.
   *
   * @return The payload size.
   * @see AdaptrisMessage#getSize()
   */
  @Override
  public long getSize() {
    return getSize(currentPayloadId);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public int getPayloadCount() {
    return payloads.size();
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public long getSize(@NotNull String payloadId) {
    return getPayload(payloadId).length;
  }

  /**
   * @see InterlokMessage#setContent(String, String).
   */
  @Override
  public void setContent(String payloadString, String charEnc) {
    addContent(currentPayloadId, payloadString, charEnc);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void setContent(String payloadId, String payloadString, String charEnc) {
    addContent(payloadId, payloadString, charEnc);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void addContent(@NotNull String payloadId, String payloadString) {
    String encoding = null;
    if (payloads.containsKey(payloadId)) {
      encoding = getContentEncoding(payloadId);
    }
    addContent(payloadId, payloadString, encoding);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void addContent(@NotNull String payloadId, String payloadString, String charEnc) {
    byte[] payload;
    if (payloadString != null) {
      Charset charset = Charset.forName(StringUtils.defaultIfBlank(charEnc, Charset.defaultCharset().name()));
      payload = payloadString.getBytes(charset);
    } else {
      payload = new byte[0];
    }
    setContentEncoding(payloadId, charEnc);
    addPayload(payloadId, payload);
  }

  /**
   * Get the current payload content.
   *
   * @return The payload content.
   * @see AdaptrisMessage#getContent()
   */
  @Override
  public String getContent() {
    return getContent(currentPayloadId);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public String getContent(@NotNull String payloadId) {
    byte[] payload = getPayload(payloadId);
    if (isEmpty(getContentEncoding())) {
      return new String(payload);
    } else {
      return new String(payload, Charset.forName(getContentEncoding()));
    }
  }

  @Override
  public String getPayloadForLogging() {
    StringBuffer sb = new StringBuffer("{");
    boolean c = false;
    for (String id : payloads.keySet()) {
      if (c) {
        sb.append(",");
      } else {
        c = true;
      }
      sb.append(id);
      if (id.equals(currentPayloadId)) {
        sb.append(":");
        sb.append(getContent(id));
      }
    }
    return sb.append("}").toString();
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void setContentEncoding(String enc) {
    setContentEncoding(currentPayloadId, enc);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public void setContentEncoding(@NotNull String payloadId, String enc) {
    String contentEncoding = enc != null ? Charset.forName(enc).name() : getFactory().getDefaultCharEncoding();
    Payload payload;
    if (payloads.containsKey(payloadId)) {
      payload = payloads.get(payloadId);
      payload.encoding = contentEncoding;
    } else {
      payload = new Payload(contentEncoding, new byte[0]);
    }
    payloads.put(payloadId, payload);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public String getContentEncoding() {
    return getContentEncoding(currentPayloadId);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public String getContentEncoding(@NotNull String payloadId) {
    return payloads.containsKey(payloadId) ? payloads.get(payloadId).encoding : getFactory().getDefaultCharEncoding();
  }

  /**
   * @see Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    MultiPayloadAdaptrisMessageImp result = (MultiPayloadAdaptrisMessageImp) super.clone();
    // clone the payloads.
    result.payloads = new HashMap<>();
    for (String payloadId : payloads.keySet()) {
      Payload payload = payloads.get(payloadId);
      result.addPayload(payloadId, payload.payload());
      result.setContentEncoding(payloadId, payload.encoding);
    }
    result.switchPayload(currentPayloadId);
    return result;
  }

  /**
   * @see AdaptrisMessage#getInputStream()
   */
  @Override
  public InputStream getInputStream() {
    return getInputStream(currentPayloadId);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public InputStream getInputStream(@NotNull String payloadId) {
    byte[] payload = getPayload(payloadId);
    return payload != null ? new ByteArrayInputStream(payload) : new ByteArrayInputStream(new byte[0]);
  }

  /**
   * @see AdaptrisMessage#getOutputStream()
   */
  @Override
  public OutputStream getOutputStream() {
    return getOutputStream(currentPayloadId);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public OutputStream getOutputStream(@NotNull String payloadId) {
    return new ByteFilterStream(payloadId, new ByteArrayOutputStream());
  }

  /**
   * @see InterlokMessage#getWriter()
   */
  @Override
  public Writer getWriter() throws IOException
  {
    return getWriter(currentPayloadId);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public Writer getWriter(@NotNull String payloadId) throws IOException
  {
    return getWriter(payloadId, getContentEncoding(payloadId));
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public Writer getWriter(@NotNull String payloadId, String encoding) throws IOException
  {
    OutputStream outputStream = getOutputStream(payloadId);
    return encoding != null ? new OutputStreamWriter(outputStream, encoding) : new OutputStreamWriter(outputStream);
  }

  /**
   * Resolve against this message's payloads or metadata.
   *
   * This is a helper method that allows you to pass in {@code %payload_id{pl1}}
   * and get the payload associated with {@code pl1}, or {@code %message{key1}}
   * and get the metadata associated with {@code key1}. Strings that do not match
   * that format will be returned as is. Support for punctuation characters is
   * down to the implementation; the standard implementations only support a
   * limited subset of punctuation characters in addition to standard word
   * characters ({@code [a-zA-Z_0-9]}); they are {@code _!"#&'+,-.:=}. The magic
   * values {@code %message{%uniqueId}} and {@code %message{%size}} should return
   * the message unique-id and message size respectively.
   *
   * @param target
   *          The string to resolve.
   * @param dotAll
   *          Whether to resolve in {@link java.util.regex.Pattern#DOTALL} mode,
   *          allowing you to match against multiple lines.
   * @return The original string, the matched payload, an item of metadata, or
   *         null (if none exists).
   */
  @Override
  public String resolve(String target, boolean dotAll) {
    if (target == null) {
      return null;
    }
    target = super.resolve(target, dotAll);
    // resolve any %payload{id:...}'s or %payload_id{...}'s before attempting any %message{...}'s
    Pattern pattern = dotAll ? dotAllPayloadResolver2 : normalPayloadResolver2;
    target = resolve(target, pattern, false);
    pattern = dotAll ? dotAllPayloadResolver : normalPayloadResolver;
    return resolve(target, pattern, true);
  }

  private String resolve(String target, Pattern pattern, boolean defaultPattern) {
    Matcher m = pattern.matcher(target);
    while (m.matches()) {
      String key = m.group(1);
      if (!hasPayloadId(key)) {
        throw new UnresolvableException("Could not resolve payload ID [" + key + "]");
      }
      target = target.replace(String.format(defaultPattern ? "%%payload_id{%s}" : "%%payload{id:%s}", key), getContent(key));
      m = pattern.matcher(target);
    }
    return target;
  }

  private class ByteFilterStream extends FilterOutputStream {
    private final String payloadId;

    ByteFilterStream(@NotNull String payloadId, ByteArrayOutputStream out) {
      super(out);
      this.payloadId = payloadId;
      payloads.put(payloadId, new Payload(out));
    }

    @Override
    public void close() throws IOException {
      super.close();
      addPayload(payloadId, ((ByteArrayOutputStream) super.out).toByteArray());
    }
  }

  private class Payload {
    String encoding = getFactory().getDefaultCharEncoding();
    private byte[] data;
    private ByteArrayOutputStream stream;

    Payload(String encoding, @NotNull byte[] data) {
      this.encoding = encoding;
      this.data = data;
    }

    Payload(@NotNull byte[] data) {
      this.data = data;
    }

    Payload(@NotNull ByteArrayOutputStream stream) {
      this.stream = stream;
    }

    byte[] payload() {
      if (stream != null) {
        return stream.toByteArray();
      }
      return data;
    }
  }
}
