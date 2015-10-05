package com.adaptris.interlok.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

public interface InterlokMessage {
  
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
  public String getUniqueId();

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
  public void setUniqueId(String uniqueId);

  public String getContent();

  public void setContent(String payload, String encoding);

  /**
   * Returns a view of all the existing headers associated with the message.
   * <p>
   * Any changes to the returned {@link Map} are not guaranteed to be reflected in underlying map.
   * You should treat the returned Map as a read only view of the current message headers. Use
   * {@link #addMessageHeader(String, String)} or {@link #removeMessageHeader(String)} to manipulate
   * individual headers.
   * </p>
   * 
   * @return a read only view of the messages.
   */
  public Map<String, String> getMessageHeaders();

  /**
   * Overwrite all the headers.
   * <p>
   * Clear and overwrite all the headers
   * </p>
   * 
   * @param metadata
   */
  public void setMessageHeaders(Map<String, String> metadata);

  public void addMessageHeader(String key, String value);

  public void removeMessageHeader(String key);

  public String getContentEncoding();

  public void setContentEncoding(String payloadEncoding);
  
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
   * Adds an <code>Object</code> to this message as metadata. Object metadata is
   * intended to be used within a single <code>Workflow</code> only and will not
   * be encoded or otherwise transported between Workflows.
   * </p>
   *
   * @param object the <code>Object</code> to set as metadata
   * @param key the key to store this object against.
   */
  void addObjectHeader(Object key, Object object);

  /**
   * <p>
   * Returns the <code>Map</code> of <code>Object</code> metadata.
   * </p>
   *
   * @return the <code>Map</code> of <code>Object</code> metadata
   */
  Map<Object,Object> getObjectHeaders();
  
  /**
   * <p>
   * Returns true if the message contains metadata against the passed key.
   * </p>
   *
   * @param key the key to look for
   * @return true if the message contains a metadata against the passed key
   */
  boolean headersContainsKey(String key);

}
