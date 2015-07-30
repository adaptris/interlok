package com.adaptris.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Represents a <i>message</i> in the framework.
 * </p>
 */
public interface AdaptrisMessage {

  /**
   * <p>
   * Sets the passed <code>byte[]</code> as this message's payload.
   * </p>
   *
   * @param payload the payload
   */
  void setPayload(byte[] payload);

  /**
   * <p>
   * Returns <b>a copy</b> of this message's payload in its raw,
   * <code>byte[]</code> form.
   * </p>
   *
   * @return payload in <code>byte[]</code> form
   */
  byte[] getPayload();

  /**
   * Return the size of the payload.
   *
   * @return the size of the payload.
   */
  long getSize();

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
   * @param encoding the encoding for the writer, which will also be used to change the character encoding of the message.
   * @return an Writer that can be used to access the payload.
   * @throws IOException if the Writer could not be created.
   * @see #setCharEncoding(String)
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
   * Sets the character encoding associated with the payload. If a character
   * encoding is not explicitly set using this method, implementations are
   * expected to use the default platform character encoding.
   * </p>
   *
   * @param charEncoding the character encoding associated with the payload.
   */
  void setCharEncoding(String charEncoding);

  /**
   * <p>
   * Returns this message's character encoding, if one has been explicitly set,
   * otherwise <code>null</code>.
   * </p>
   *
   * @return this message's character encoding, if one has been explicitly set,
   *         otherwise <code>null</code>
   */
  String getCharEncoding();

  /**
   * <p>
   * Sets the passed <code>String</code> as the payload. The passed <code>String</code> is assumed to have the default platform
   * encoding and any previously set character encoding will be set to null. If a specific character encoding is required use
   * <code>setStringPayload(String, String)
   * </code>.
   * </p>
   * 
   * @param payload the payload to set
   * @deprecated you should use {@link #setStringPayload(String, String)} and formally declare the encoding you wish to use for this
   *             string; since 2.9.3
   */
  @Deprecated
  void setStringPayload(String payload);

  /**
   * <p>
   * Sets the passed <code>String</code> with the passed character encoding as
   * the payload. Passing null for <code>charEncoding</code> is the same as
   * using <code>setPayload(String)</code>.
   * </p>
   *
   * @param payload the payload
   * @param charEncoding the character encoding used by the payload
   */
  void setStringPayload(String payload, String charEncoding);

  /**
   * <p>
   * Returns a <code>String</code> representation of the payload. This
   * <code>String</code> will be created using the provided character encoding
   * if one has been set, otherwise the default platform character encoding will
   * be used. If you wish to apply a different character encoding use
   * <code>getPayload</code> to obtain a raw <code>byte[]</code>.
   * </p>
   *
   * @return a <code>String</code> representation of the payload
   */
  String getStringPayload();

  /**
   * <p>
   * Returns the <code>String</code> value associated with the passed
   * <code>key</code> or <code>null</code> if the <code>key</code> does not
   * exist. NB may also return <code>null</code> if <code>null</code> was
   * explicitly set as the value of the key. Use <code>containsKey</code> to
   * differentiate these cases.
   * </p>
   *
   * @param key the key to look for
   * @return the value associated with the key
   */
  String getMetadataValue(String key);

  /**
   * <p>
   * Returns the <code>MetadataElement</code> containing the passed
   * <code>key</code> or <code>null</code> if the <code>key</code> does not
   * exist. NB may also return <code>null</code> if <code>null</code> was
   * explicitly set as the value of the key. Use <code>containsKey</code> to
   * differentiate these cases.
   * </p>
   *
   * @param key the key to look for
   * @return the <code>MetadataElement</code> containing the key
   */
  MetadataElement getMetadata(String key);

  /**
   * <p>
   * Returns true if the message contains metadata against the passed key.
   * </p>
   *
   * @param key the key to look for
   * @return true if the message contains a metadata against the passed key
   */
  boolean containsKey(String key);

  /**
   * <p>
   * Puts the passed <code>value</code> into the metadata against the passed
   * <code>key</code>. If a value has previously been set against this
   * <code>key</code> it <b>will</b> be overwritten.
   * </p>
   *
   * @param key the key
   * @param value the value
   */
  void addMetadata(String key, String value);

  /**
   * <p>
   * Puts the passed <code>MetadataElement</code> into the metadata. If a value
   * has previously been set with the same <code>key</code> it <b>will</b> be
   * overwritten and a message to that effect should be logged.
   * </p>
   *
   * @param metadata the <code>MetadataElement</code> to add
   */
  void addMetadata(MetadataElement metadata);

  /**
   * <p>
   * Removes the passed <code>MetadataElement</code> from the metadata if it is
   * present.
   * </p>
   *
   * @param element the <code>MetadataElement</code> to remove
   */
  void removeMetadata(MetadataElement element);

  /**
   * <p>
   * Returns a shallow clone of this message's metadata.
   * </p>
   *
   * @return a <code>Set</code> of all <code>MetadataElements</code>s
   */
  Set<MetadataElement> getMetadata();

  /**
   * <p>
   * Adds all the passed metadata to this message's metadata. This <b>will</b>
   * overwrite the values associated with any pre-existing keys.
   * </p>
   *
   * @param metadata a <code>Set</code> of KeyValuePairs to add
   */
  void setMetadata(Set metadata);

  /**
   * <p>
   * Clears the current metadata.
   * </p>
   */
  void clearMetadata();

  /** Return the factory that created this message.
   *
   * @return the factory.
   */
  AdaptrisMessageFactory getFactory();

  /**
   * <p>
   * Returns a unique identifier for this message. The uniqueness of this
   * identifier is dependent on the implementation.
   * </p>
   * <p>
   * This is generally considered to be a system level identifier. Each time a
   * message is created a new unique id should be created. If an application of
   * the framework requires a unique id that spans multiple messages (e.g. F4F
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
   * it makes unique ids mutable. Could get round this by putting unique id
   * stuff in the message imp rather than the factory if required.
   * </p>
   *
   * @param uniqueId the unique identifier for the message
   */
  void setUniqueId(String uniqueId);

  /**
   * <p>
   * Returns a deep clone of this object.
   * </p>
   *
   * @return a deep clone of this object
   * @throws CloneNotSupportedException if the implementation does not support
   *           cloning
   */
  Object clone() throws CloneNotSupportedException;

  /**
   * <p>
   * Adds an event to this <code>AdaptrisMessage</code>'s
   * <code>MessageLifecycleEvent</code>.
   * </p>
   *
   * @param meg the <code>MessageEventGenerator</code>
   * @param wasSuccessful the status of the event
   */
  void addEvent(MessageEventGenerator meg, boolean wasSuccessful);

  /**
   * <p>
   * Returns this object's <code>MessageLifecycleEvent</code>.
   * </p>
   *
   * @return this object's <code>MessageLifecycleEvent</code>
   */
  MessageLifecycleEvent getMessageLifecycleEvent();

  /**
   * <p>
   * Uses the passed <code>AdaptrisMessageEncoder</code> to create an encoded
   * version of this <code>AdaptrisMessage</code>. If null is passed this method
   * returns <code>this.getPayload()</code>.
   * <p>
   *
   * @param encoder the <code>AdaptrisMessageEncoder</code> to use
   * @return a byte[] representation of this message
   * @throws CoreException wrapping any underlying Exceptions that may occur
   */
  byte[] encode(AdaptrisMessageEncoder encoder) throws CoreException;

  /**
   * <p>
   * Adds an <code>Object</code> to this message as metadata. Object metadata is
   * intended to be used within a single <code>Workflow</code> only and will not
   * be encoded or otherwise transported between Workflows.
   * </p>
   *
   * @param object the <code>Object</code> to set as metadata
   * @param key the key to store this object against.
   */
  void addObjectMetadata(String key, Object object);

  /**
   * <p>
   * Returns the <code>Map</code> of <code>Object</code> metadata.
   * </p>
   *
   * @return the <code>Map</code> of <code>Object</code> metadata
   */
  Map getObjectMetadata();

  /**
   * <p>
   * Overloaded <code>toString</code> method which allows client to specify
   * whether payload is logged. (NB by default standard <code>toString</code>
   * does not log the payload).
   * </p>
   *
   * @param includePayload true if payload should be included in return
   * @return a String representation of the message
   */
  String toString(boolean includePayload);

  /**
   * <p>
   * Overloaded <code>toString</code> method which allows client to specify
   * whether payload is logged. (NB by default standard <code>toString</code>
   * does log the payload).
   * </p>
   *
   * @param includePayload true if payload should be included in return
   * @param includeEvents true if the events should be included.
   * @return a String representation of the message
   */
  String toString(boolean includePayload, boolean includeEvents);

  /**
   * <p>
   * Returns the unique ID of the next <code>Service</code> to apply to the
   * message. Optional, used by <code>BranchingServiceCollection</code> and
   * possibly others later.
   * </p>
   *
   * @return the unique ID of the next <code>Service</code> to apply
   */
  String getNextServiceId();

  /**
   * <p>
   * Sets the unique ID of the next <code>Service</code> to apply to the
   * message.
   * </p>
   *
   * @param uniqueId the unique ID of the next <code>Service</code> to apply to
   *          the message
   */
  void setNextServiceId(String uniqueId);

  /**
   * <p>
   * First looks for a metadata value stored against the passed key taking
   * account of the passed key's case. If a value is found it is returned. Next
   * looks for a metadata value stored against the passed key ignoring. The
   * valued stored against the first key which matches the passed key ignoring
   * case is returned. As the underlying store is unordered, which key will
   * match first is undefined. If no key matches the passed key ignoring case,
   * null is returned.
   * </p>
   * <p>
   * E.g. where a message has the metdadata values "1" and "2" set against keys
   * "AAA" and "aaa" respectively, calling
   * <code>getMetadataValueIgnoreKeyCase("AAA")</code> will return "1', calling
   * <code>getMetadataValueIgnoreKeyCase("aaa")</code> will return "2', and
   * calling <code>getMetadataValueIgnoreKeyCase("aAA")</code> will return
   * either "1' or "2".
   * </p>
   *
   * @param key the key to look for
   * @return the value associated with the key or null
   */
  String getMetadataValueIgnoreKeyCase(String key);

  /**
   * <p>
   * <code>AdaptrisMessage</code>'s are equivalent for <i>tracking</i> if they
   * have the same unique Id, their payloads are equal, their char encodings are
   * equal and their metadata is equal. NB equality of metadata is based on key
   * only.
   * </p>
   *
   * @param other the message to compare
   * @return true if the passed message is equivalent for tracking
   */
  boolean equivalentForTracking(AdaptrisMessage other);
}
