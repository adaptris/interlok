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

package com.adaptris.core.socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/** Protocol interface for the Socket Listener.
 *  <p>In order to send and receive documents via the Socket Adapter, it is
 *  mandatory for an implementation of this interface to be available.
 *  </p>
 * @author  lchan
 */
public interface Protocol {
  
  /** Set the Socket that should be used for the next conversation.
   * 
   * @param s the socket.
   */
  void setSocket(Socket s);
   
  /** Send a document.
   * 
   * @param bytes the bytes to send.
   * @throws IOException wrapping any underlying exception.
   */
  void sendDocument(byte[] bytes) throws IOException;
  
  /** Return the reply from the receiving party.
   *  @return the resulting data or null if there was no data sent
   *  as part of the reply.
   *  @throws IOException if there was a failure getting the reply.
   *  @throws IllegalStateException if the previous command was not
   *  <code>sendDocument</code>
   *  @see #sendDocument(byte[]) 
   */
  byte[] getReplyAsBytes() throws IOException, IllegalStateException;
  
  /** Return true if the <code>sendDocument</code> was considered successful.
   * 
   * @return true if the document was successful.
   * @throws IllegalStateException if the previous 
   * @see #sendDocument(byte[]) 
   */
  boolean wasSendSuccess() throws IllegalStateException;

  /** Get the reply as an InputStream.
   *  @return the InputStream that wraps the reply, can be null if there
   *  was no reply.
   *  @throws IOException if there was a failure getting the reply.
   *  @throws IllegalStateException if the previous command was not
   *  <code>sendDocument</code>
   *  @see #getReplyAsBytes() 
   */
  InputStream getReplyAsStream() throws IOException, IllegalStateException;
  
  /** Receive a document.
   * 
   * @throws IOException wrapping any underlying exception.
   */
  void receiveDocument() throws IOException;
  
  /** Get the received document as a stream.
   *  @throws IOException if there was a failure g.etting the received doc.
   *  @throws IllegalStateException if the previous command was not
   *  <code>receiveDocument</code>
   * @return the input stream representing the document that was received.
   */
  InputStream getReceivedAsStream() throws IOException, IllegalStateException;
  
  /** Get the received document as a byte array.
   *  @throws IOException if there was a failure getting the received doc.
   *  @throws IllegalStateException if the previous command was not
   *  <code>receiveDocument</code>
   * @return the byte array
   */
  byte[] getReceivedAsBytes() throws IOException, IllegalStateException;
  
  /** The document receive was processed correctly, send a reply indicating it.
   *  @throws IOException if there was a failure sending the reply.
   *  @throws IllegalStateException if the previous command was not
   *  <code>receiveDocument</code>
   */
  void receiveDocumentSuccess() throws IOException, IllegalStateException;
  
  /** The document receive was processed incorrectly, send a reply indicating 
   *  it.
   *  @throws IOException if there was a failure sending the reply.
   *  @throws IllegalStateException if the previous command was not
   *  <code>receiveDocument</code>
   */
  void receiveDocumentError() throws IOException, IllegalStateException;
}
