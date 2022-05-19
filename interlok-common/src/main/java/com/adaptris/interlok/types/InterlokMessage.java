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

package com.adaptris.interlok.types;

import com.adaptris.annotation.Removal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.Optional;

public interface InterlokMessage {

  /**
   * <p>
   * Returns a unique identifier for this message. The uniqueness of this identifier is dependent on the
   * implementation.
   * </p>
   * <p>
   * This is generally considered to be a system level identifier. Each time a message is created a new unique id should
   * be created. If an application of the framework requires a unique id that spans multiple messages (e.g. F4F
   * request-reply) it would be better to create an instance of
   * <code>AddMetadataService</code> that assigns a higher level application
   * unique id.
   * </p>
   *
   * @return a unique identifier for this message
   */
  String getUniqueId();

  /**
   * <p>
   * Sets this message's unique id. This is necessary to allow
   * <code>AdaptrisMessageFactory</code> to set unique ids, the downside is that
   * it makes unique ids mutable. Could get round this by putting unique id stuff in the message imp rather than the
   * factory if required.
   * </p>
   *
   * @param uniqueId the unique identifier for the message
   */
  void setUniqueId(String uniqueId);

  String getContent();

  void setContent(String payload, String encoding);

  /**
   * Returns a view of all the existing headers associated with the message.
   * <p>
   * Any changes to the returned {@link Map} are not guaranteed to be reflected in underlying map. You should treat the
   * returned Map as a read only view of the current message headers. Use {@link #addMessageHeader(String, String)} or
   * {@link #removeMessageHeader(String)} to manipulate individual headers.
   * </p>
   *
   * @return a read only view of the messages.
   */
  Map<String, String> getMessageHeaders();

  /**
   * Overwrite all the headers.
   * <p>
   * There is a problem with existing implementations that they do not obey this contract because of confusion with
   * {@code AdaptrisMessage#setMetadata(Set)}. In almost all cases they have opted to treat this method as additive
   * rather than replacing all metadata. <strong>There is no guarantee that implementations will clear and
   * overwrite all the headers</strong>. As a result we have opted to deprecate this method with a view to removing the
   * ambiguity with different methods.
   * </p>
   *
   * @param metadata the metadata
   * @deprecated since 4.5.0 because it implementations are additive; use {@link #addMessageHeaders(Map)} or
   * {@link #replaceAllMessageHeaders(Map)} instead. instead.
   */
  @Deprecated
  @Removal(version = "5.0.0")
  void setMessageHeaders(Map<String, String> metadata);

  /**
   * Add all the headers to the message
   * <p>
   * This is largely a semantic replacement for {@code setMessageHeaders(Map)} where the underlying implementations
   * did not do the right thing.
   * </p>
   * @implNote The default implementation simply uses {@link #addMessageHeader(String,String)} iterating over the
   *           Map
   * @param headers the headers to add
   */
  default void addMessageHeaders(Map<String, String> headers) {
    Optional.ofNullable(headers).ifPresent((h) -> h.forEach(this::addMessageHeader));
  }

  /**
   * Replace all the headers to the message
   * <p>
   * This is largely a semantic replacement for {@code setMessageHeaders(Map)} which defaults the correct behaviour.
   * </p>
   * @implNote The default implementation simply uses {@link #addMessageHeader(String,String)} iterating over the
   *           Map after calling {@link #clearMessageHeaders()}
   * @param headers the headers that replace the existing metadata.
   */
  default void replaceAllMessageHeaders(Map<String,String> headers) {
    clearMessageHeaders();
    addMessageHeaders(headers);
  }

  /**
   * Remove all the message headers.
   */
  void clearMessageHeaders();

  void addMessageHeader(String key, String value);

  void removeMessageHeader(String key);

  String getContentEncoding();

  void setContentEncoding(String payloadEncoding);

  /**
   * Return a reader representation of the payload.
   *
   * @return a reader that can be used to access the payload
   * @throws IOException if the Reader could not be created.
   */
  Reader getReader() throws IOException;

  /**
   * Return a writer ready for writing the payload.
   *
   * @return an Writer that can be used to write the payload using the existing encoding.
   * @throws IOException if the Writer could not be created.
   */
  Writer getWriter() throws IOException;

  /**
   * Return a writer ready for writing the payload.
   *
   * @param encoding the encoding for the writer, which will also be used to change the character encoding of the
   *                 message.
   * @return an Writer that can be used to access the payload.
   * @throws IOException if the Writer could not be created.
   * @see #setContentEncoding(String)
   */
  Writer getWriter(String encoding) throws IOException;

  /**
   * Return an inputstream representation of the payload.
   *
   * @return an InputStream that can be used to access the payload.
   * @throws IOException if the InputStream could not be created.
   */
  InputStream getInputStream() throws IOException;

  /**
   * Return an ouputstream ready for writing the payload.
   *
   * @return an OutputStream that can be used to access the payload.
   * @throws IOException if the OutputStream could not be created.
   */
  OutputStream getOutputStream() throws IOException;


  /**
   * <p>
   * Adds an <code>Object</code> to this message as metadata. Object metadata is intended to be used within a single
   * <code>Workflow</code> only and will not be encoded or otherwise transported between Workflows.
   * </p>
   *
   * @param object the <code>Object</code> to set as metadata
   * @param key    the key to store this object against.
   */
  void addObjectHeader(Object key, Object object);

  /**
   * <p>
   * Returns the <code>Map</code> of <code>Object</code> metadata.
   * </p>
   *
   * @return the <code>Map</code> of <code>Object</code> metadata
   */
  Map<Object, Object> getObjectHeaders();

  /**
   * <p>
   * Returns true if the message contains metadata against the passed key.
   * </p>
   *
   * @param key the key to look for
   * @return true if the message contains a metadata against the passed key
   */
  boolean headersContainsKey(String key);

  /**
   * Resolve against this message's metadata.
   *
   * @param s string to resolve.
   * @return the original string, an item of metadata, or null (if the metadata key does not exist).
   * @implSpec The default implementation simply calls {@link #resolve(String, boolean)}.
   * @see #resolve(String, boolean)
   */
  default String resolve(String s) {
    return resolve(s, false);
  }

  /**
   * Resolve against this message's metadata.
   *
   * <p>
   * This is a helper method that allows you to pass in {@code %message{key1}} and get the metadata associated with
   * {@code key1}. Strings that do not match that format will be returned as is. Support for punctuation characters is
   * down to the implementation; the standard implementations only support a limited subset of punctuation characters in
   * addition to standard word characters ({@code [a-zA-Z_0-9]}); They are {@code _!"#&'+,-.:=}. The magic values
   * {@code %message{%uniqueId}} and {@code %message{%size}} should return the message unique-id and message size
   * respectively
   * </p>
   *
   * @param s         string to resolve.
   * @param multiline whether to resolve in {@link java.util.regex.Pattern#DOTALL} mode, allowing you to match against
   *                  multiple lines.
   * @return the original string, an item of metadata, or null (if the metadata key does not exist).
   */
  String resolve(String s, boolean multiline);

  /**
   * Resolve against this message's object metadata.
   *
   * <p>
   * This is a helper method that allows you to pass in {@code %messageObject{key1}} and get the object metadata
   * associated with {@code key1} (or null)Support for punctuation characters in the key nameis down to the
   * implementation; the standard implementations only support a limited subset of punctuation characters in addition to
   * standard word characters ({@code [a-zA-Z_0-9]}); They are {@code _!"#&'+,-.:=}.
   * </p>
   *
   * @param s string to resolve.
   * @return the object (or null).
   */
  Object resolveObject(String s);

  /**
   * Wrap the interlok message as another type of thing.
   *
   * @param wrapper an implementation of {@link MessageWrapper}
   * @return the wrapped object
   */
  default <T extends Object> T wrap(MessageWrapper<T> wrapper) throws Exception {
    return wrapper.wrap(this);
  }

}
