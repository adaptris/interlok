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

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The multi-payload message factory which returns an implementations of
 * <code>MultiPayloadAdaptrisMessage</code>.
 *
 * <pre>{@code
 * <message-factory class="multi-payload-message-factory">
 *   <default-char-encoding>UTF-8</default-char-encoding>
 *   <default-payload-id>payload-1</default-payload-id>
 * </message-factory>
 * }</pre>
 *
 * @author aanderson
 * @config multi-payload-message-factory
 * @see AdaptrisMessageFactory
 * @see MultiPayloadAdaptrisMessage
 * @since 3.9.x
 */
@XStreamAlias("multi-payload-message-factory")
@DisplayOrder(order = { "defaultCharEncoding" })
public class MultiPayloadMessageFactory extends AdaptrisMessageFactory {
  private static final Logger log = LoggerFactory.getLogger(MultiPayloadMessageFactory.class);

  @AdvancedConfig(rare = true)
  private String defaultCharEncoding = "UTF-8";

  private String defaultPayloadId = MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID;

  public MultiPayloadMessageFactory() {
    super();
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public AdaptrisMessage newMessage(byte[] payload) {
    return newMessage(defaultPayloadId(), payload, null);
  }

  public AdaptrisMessage newMessage(@NotNull String payloadId, byte[] payload) {
    return newMessage(payloadId, payload, null);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public AdaptrisMessage newMessage(byte[] payload, Set metadata) {
    return newMessage(defaultPayloadId(), payload, metadata);
  }

  /**
   * Create a new multi-payload message, with the given ID, payload, and metadata.
   *
   * @param payloadId
   *          The payload ID to use.
   * @param payload
   *          The payload.
   * @param metadata
   *          Any metadata.
   * @return The new multi-payload message.
   */
  public AdaptrisMessage newMessage(@NotNull String payloadId, byte[] payload, Set metadata) {
    AdaptrisMessage result = new MultiPayloadAdaptrisMessageImp(payloadId, uniqueIdGenerator(), this, payload);
    if (!isEmpty(getDefaultCharEncoding())) {
      result.setContentEncoding(getDefaultCharEncoding());
    }
    result.setMetadata(metadata);
    return result;
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public AdaptrisMessage newMessage(String payload) {
    return newMessage(defaultPayloadId(), payload, defaultCharEncoding, null);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public AdaptrisMessage newMessage(String payload, String charEncoding) {
    return newMessage(defaultPayloadId(), payload, charEncoding, null);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public AdaptrisMessage newMessage(String payload, Set metadata) {
    return newMessage(defaultPayloadId(), payload, defaultCharEncoding, metadata);
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public AdaptrisMessage newMessage(String payload, String charEncoding, Set metadata) {
    return newMessage(defaultPayloadId(), payload, charEncoding, metadata);
  }

  /**
   * Create a new multi-payload message, with the given ID, and payload.
   *
   * @param payloadId
   *          The payload ID to use.
   * @param content
   *          The payload content.
   * @param charEncoding
   *          The content encoding.
   * @return The new multi-payload message.
   */
  public AdaptrisMessage newMessage(@NotNull String payloadId, String content, String charEncoding) {
    return newMessage(payloadId, content, charEncoding, null);
  }

  /**
   * Create a new multi-payload message, with the given ID, payload, and metadata.
   *
   * @param payloadId
   *          The payload ID to use.
   * @param content
   *          The payload content.
   * @param charEncoding
   *          The content encoding.
   * @param metadata
   *          Any metadata.
   * @return The new multi-payload message.
   */
  public AdaptrisMessage newMessage(@NotNull String payloadId, String content, String charEncoding, Set metadata) {
    Charset charset = null;
    try {
      charset = Charset.forName(charEncoding);
    } catch (IllegalArgumentException e) {
      log.warn("Character set [" + charEncoding + "] is not available");
    }
    AdaptrisMessage result = new MultiPayloadAdaptrisMessageImp(payloadId, uniqueIdGenerator(), this, content, charset);
    result.setMetadata(metadata);
    return result;
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public AdaptrisMessage newMessage(AdaptrisMessage source, Collection<String> metadataKeysToPreserve)
      throws CloneNotSupportedException {
    return newMessage(defaultPayloadId(), source, metadataKeysToPreserve);
  }

  public AdaptrisMessage newMessage(@NotNull String payloadId, AdaptrisMessage source,
      Collection<String> metadataKeysToPreserve) throws CloneNotSupportedException {
    MultiPayloadAdaptrisMessage result = (MultiPayloadAdaptrisMessage) newMessage();
    result.setUniqueId(source.getUniqueId());
    result.setCurrentPayloadId(payloadId);
    if (metadataKeysToPreserve == null) {
      result.setMetadata(source.getMetadata());
    } else {
      for (String metadataKey : metadataKeysToPreserve) {
        if (source.headersContainsKey(metadataKey)) {
          result.addMetadata(metadataKey, source.getMetadataValue(metadataKey));
        }
      }
    }
    MessageLifecycleEvent mle = result.getMessageLifecycleEvent();
    List<MleMarker> markers = source.getMessageLifecycleEvent().getMleMarkers();
    for (MleMarker marker : markers) {
      mle.addMleMarker((MleMarker) marker.clone());
    }
    result.getObjectHeaders().putAll(source.getObjectHeaders());
    return result;
  }

  /**
   * {@inheritDoc}.
   */
  @Override
  public AdaptrisMessage newMessage() {
    AdaptrisMessage m = new MultiPayloadAdaptrisMessageImp(defaultPayloadId(), uniqueIdGenerator(), this);
    if (!isEmpty(getDefaultCharEncoding())) {
      m.setContentEncoding(getDefaultCharEncoding());
    }
    return m;
  }

  /**
   * @return the defaultCharEncoding
   */
  @Override
  public String getDefaultCharEncoding() {
    return defaultCharEncoding;
  }

  /**
   * Set the default character encoding to be applied to the message upon
   * creation.
   * <p>
   * If not explicitly configured, then the platform default character encoding
   * will be used.
   * </p>
   *
   * @param s
   *          the defaultCharEncoding to set
   * @see AdaptrisMessage#setCharEncoding(String)
   */
  @Override
  public void setDefaultCharEncoding(String s) {
    defaultCharEncoding = s;
  }

  /**
   * Get the default payload ID.
   *
   * @return The default payload ID.
   */
  public String getDefaultPayloadId() {
    return defaultPayloadId;
  }

  /**
   * Set the default payload ID.
   *
   * @param defaultPayloadId
   *          The default payload ID.
   */
  public void setDefaultPayloadId(String defaultPayloadId) {
    this.defaultPayloadId = defaultPayloadId;
  }

  private String defaultPayloadId() {
    if (isEmpty(defaultPayloadId)) {
      return MultiPayloadAdaptrisMessage.DEFAULT_PAYLOAD_ID;
    }
    return defaultPayloadId;
  }
}
