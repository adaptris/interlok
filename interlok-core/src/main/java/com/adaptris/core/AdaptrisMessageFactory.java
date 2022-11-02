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

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * A <code>static</code> factory which returns implementations of
 * <code>AdaptrisMessage</code>.
 * </p>
 */
@Slf4j
@NoArgsConstructor
public abstract class AdaptrisMessageFactory {

  /**
   * If set as as system property then the fully qualified classname is used as the default message factory.
   */
  public static final String OVERRIDE_DEFAULT_MSG_FACTORY_PROP = "interlok.default.message.factory";

  /**
   * If set as an env var then the fully qualified classname is used as the default message factory.
   * <p>This will take precedence over the system property</p>
   *
   * @see #OVERRIDE_DEFAULT_MSG_FACTORY_PROP
   */
  public static final String OVERRIDE_DEFAULT_MSG_FACTORY_ENV = "INTERLOK_DEFAULT_MSG_FACTORY";

  /**
   * If set as as system property then the fully qualified classname is used as the default ID generator.
   */
  public static final String OVERRIDE_DEFAULT_MSGID_GEN_PROP = "interlok.default.msgid.generator";
  /**
   * If set as an env var then the fully qualified classname is used as the default ID Generator
   * <p>This will take precedence over the system property</p>
   *
   * @see #OVERRIDE_DEFAULT_MSGID_GEN_PROP
   */
  public static final String OVERRIDE_DEFAULT_MSGID_GEN_KEY = "INTERLOK_DEFAULT_MSGID_GENERATOR";

  private static final AdaptrisMessageFactory DEFAULT_INSTANCE = createDefaultFactory();

  private static final IdGenerator DEFAULT_GENERATOR = createDefaultIdGenerator();

  @AdvancedConfig
  @Valid
  private IdGenerator uniqueIdGenerator;

  /**
   * Get the default implementationion of AdaptrisMessageFactory.
   *
   * <p>
   * Generally speaking, the appropriate message factory should be already be configured explicitly and available to be
   * used. This method is simply here for those instances where no AdaptrisMessageFactory is available.
   * </p>
   *
   * @return a AdaptrisMessageFactory implementation
   * @see AdaptrisMessageWorker#getMessageFactory()
   * @see AdaptrisMessageEncoder#currentMessageFactory()
   * @see DefaultMessageFactory
   */
  public static AdaptrisMessageFactory getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  /**
   * Convenience method for null protection.
   *
   * @param f the configured message factory.
   * @return the configured message factory or the default instance if it is null.
   */
  public static AdaptrisMessageFactory defaultIfNull(AdaptrisMessageFactory f) {
    return Optional.ofNullable(f).orElse(DEFAULT_INSTANCE);
  }

  /**
   * <p>
   * Returns a new <code>AdaptrisMessage</code> with the specified payload and metadata.
   * </p>
   *
   * @param payload  the <code>byte[]</code> payload
   * @param metadata a <code>Set</code> of <code>MetadataElement</code>s
   * @return a new <code>AdaptrisMessage</code>
   */
  @SuppressWarnings("rawtypes")
  public abstract AdaptrisMessage newMessage(byte[] payload, Set metadata);

  /**
   * <p>
   * Returns a new <code>AdaptrisMessage</code> with the specified payload and metadata.
   * </p>
   *
   * @param payload the <code>byte[]</code> payload
   * @return a new <code>AdaptrisMessage</code>
   */
  public abstract AdaptrisMessage newMessage(byte[] payload);

  /**
   * <p>
   * Returns a new <code>AdaptrisMessage</code> with the specified payload and metadata. Uses default platform character
   * encoding.
   * </p>
   *
   * @param payload  the <code>String</code> payload
   * @param metadata a <code>Set</code> of <code>MetadataElement</code>s
   * @return a new <code>AdaptrisMessage</code>
   */
  @SuppressWarnings("rawtypes")
  public abstract AdaptrisMessage newMessage(String payload, Set metadata);

  /**
   * <p>
   * Returns a new <code>AdaptrisMessage</code> with the specified payload and metadata. Uses default platform character
   * encoding.
   * </p>
   *
   * @param payload the <code>String</code> payload
   * @return a new <code>AdaptrisMessage</code>
   */
  public abstract AdaptrisMessage newMessage(String payload);

  /**
   * <p>
   * Returns a new <code>AdaptrisMessage</code> with the specified payload and metadata. Uses default platform character
   * encoding.
   * </p>
   *
   * @param payload      the <code>String</code> payload
   * @param metadata     a <code>Set</code> of <code>MetadataElement</code>s
   * @param charEncoding the <code>String</code>'s character encoding
   * @return a new <code>AdaptrisMessage</code>
   * @throws UnsupportedEncodingException if the passed character encoding is not supported
   */
  @SuppressWarnings("rawtypes")
  public abstract AdaptrisMessage newMessage(String payload,
      String charEncoding, Set metadata)
      throws UnsupportedEncodingException;

  /**
   * <p>
   * Returns a new <code>AdaptrisMessage</code> with the specified payload and metadata. Uses default platform character
   * encoding.
   * </p>
   *
   * @param payload      the <code>String</code> payload
   * @param charEncoding the <code>String</code>'s character encoding
   * @return a new <code>AdaptrisMessage</code>
   * @throws UnsupportedEncodingException if the passed character encoding is not supported
   */
  public abstract AdaptrisMessage newMessage(String payload, String charEncoding)
      throws UnsupportedEncodingException;

  /**
   * Returns a new <code>AdaptrisMessage</code> with an empty payload but with selected metadata from the source.
   * <p>
   * The new AdaptrisMessage will have the same message id and MessageLifecycleEvent will be cloned from the original.
   * Object metadata will also be preserved.
   * </p>
   *
   * @param source                 the original AdaptrisMessage
   * @param metadataKeysToPreserve a list of keys to transfer to the new Message; if null, then all keys.
   * @return a new {@code AdaptrisMessage}
   * @throws CloneNotSupportedException if the MleMarkers could not be cloned.
   */
  public abstract AdaptrisMessage newMessage(AdaptrisMessage source, Collection<String> metadataKeysToPreserve)
      throws CloneNotSupportedException;

  /**
   * Return the default character encoding for the message.
   *
   * @return the defaultCharEncoding
   */
  public abstract String getDefaultCharEncoding();

  /**
   * Set the default character encoding to be applied to the message upon creation.
   * <p>
   * If not explicitly configured, then the platform default character encoding should be used.
   * </p>
   *
   * @param s the defaultCharEncoding to set
   * @see AdaptrisMessage#setCharEncoding(String)
   * @see AdaptrisMessage#setContentEncoding(String)
   */
  @SuppressWarnings("deprecation")
  public abstract void setDefaultCharEncoding(String s);

  /**
   * <p>
   * Returns a new <code>AdaptrisMessage</code>. Payload and metadata are null.
   * </p>
   *
   * @return a new <code>AdaptrisMessage</code> with the specified payload and metadata
   */
  public abstract AdaptrisMessage newMessage();

  /**
   * @return the uniqueIdGenerator
   */
  public IdGenerator getUniqueIdGenerator() {
    return uniqueIdGenerator;
  }

  /**
   * Set the unique id generator used for messages.
   * <p>
   * In some situations you may not want to use the default {@link GuidGenerator} instance when assigning unique ids to
   * messages. This allows you to change the {@link IdGenerator} used both for message ids unique ids associated with
   * {@link MessageLifecycleEvent}.
   * </p>
   *
   * @param s the uniqueIdGenerator to set
   */
  public void setUniqueIdGenerator(IdGenerator s) {
    this.uniqueIdGenerator = s;
  }

  protected IdGenerator uniqueIdGenerator() {
    return Optional.ofNullable(getUniqueIdGenerator()).orElse(DEFAULT_GENERATOR);
  }

  protected static IdGenerator createDefaultIdGenerator() {
    IdGenerator guid = create(resolve(OVERRIDE_DEFAULT_MSGID_GEN_KEY, OVERRIDE_DEFAULT_MSGID_GEN_PROP,
        GuidGenerator.class.getCanonicalName()));
    log.debug("Using {} as default ID Generator for messages", guid.getClass().getCanonicalName());
    return guid;
  }

  protected static AdaptrisMessageFactory createDefaultFactory() {
    AdaptrisMessageFactory f = create(resolve(OVERRIDE_DEFAULT_MSG_FACTORY_ENV, OVERRIDE_DEFAULT_MSG_FACTORY_PROP,
        DefaultMessageFactory.class.getCanonicalName()));
    log.debug("Using {} as default MessageFactory Implementation", f.getClass().getCanonicalName());
    return f;
  }

  @SuppressWarnings("unchecked")
  private static <T> T create(String name) {
    try {
      return (T) Class.forName(name).getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Resolve a value from an environment variable or system property.
   *
   * @param envKey       the environment variable, this takes precedence over system properties.
   * @param sysPropKey   the system property.
   * @param defaultValue the default value if neither keys exist.
   * @return the resolved value.
   */
  protected static String resolve(String envKey, String sysPropKey, String defaultValue) {
    String envValue = System.getenv(envKey);
    return StringUtils.defaultIfBlank(
        Optional.ofNullable(envValue).orElse(System.getProperty(sysPropKey, defaultValue)), defaultValue);
  }

}
