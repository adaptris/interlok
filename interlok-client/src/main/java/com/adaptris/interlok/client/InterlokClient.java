package com.adaptris.interlok.client;

import java.util.Map;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.SerializableMessage;

/**
 * Base interface for clients to submit messages into an Interlok instance.
 * 
 * 
 */
public interface InterlokClient {

  /**
   * Connect Interlok.
   * 
   * @throws InterlokException wrapping any exceptions
   */
  void connect() throws InterlokException;

  /**
   * Disconnect from interlok.
   * 
   * @throws InterlokException wrapping any exceptions
   */
  void disconnect() throws InterlokException;

  /**
   * Convenience form of {@link #processAsync(MessageTarget, SerializableMessage)} that uses
   * {@link com.adaptris.interlok.types.DefaultSerializableMessage}.
   * 
   * @param f the target
   * @param payload the payload
   * @param metadata the metadata.
   * @throws InterlokException wrapping any failures to find the workflow to target.
   */
  void processAsync(MessageTarget f, String payload, Map<String, String> metadata) throws InterlokException;

  /**
   * Publish a message.
   * 
   * @param f the filter for narrowing down a workflow
   * @param m the message
   * @throws InterlokException wrapping any failures to find the workflow to target.
   */
  void processAsync(MessageTarget f, SerializableMessage m) throws InterlokException;

  /**
   * Send a message to the workflow and wait for a reply.
   * 
   * @param f the target for the message
   * @param m the message
   * @return a new SerializablMessage instance containing the contents of the message at the end of
   *         the workflow.
   * @throws InterlokException wrapping any failures.
   */
  SerializableMessage process(MessageTarget f, SerializableMessage m) throws InterlokException;
}
