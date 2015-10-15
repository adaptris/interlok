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

package com.adaptris.transport;

import java.io.InputStream;
import java.io.InterruptedIOException;

/** Interface specifying transport functionality.
 */
public interface TransportLayer {
  /** Send some bytes.
   *  <p>Send the contents of the byte array
   *  @param bytes the byte array to be sent
   *  @see #send(byte[], int, int)
   *  @throws TransportException An error was encountered with the transport
   *  @throws IllegalStateException  The object isn't connected!
   */
  void send(byte[] bytes) throws TransportException, IllegalStateException;

  /** Send some bytes.
   *  @param bytes The byte array
   *  @param offset the offset to start from, 0 being the start of the array
   *  @param len how many bytes to send
   *  @throws TransportException An error was encountered with the transport
   *  @throws IllegalStateException  if the underlying transport is not in the
   *  appropriate state
   */
  void send(byte[] bytes, int offset, int len)
    throws TransportException, IllegalStateException;

  /** Send <code>len</code> bytes from <code>in</code>.
   *  @param in The inputstream containing data
   *  @param len how many bytes to send
   *  @throws TransportException An error was encountered with the transport
   *  @throws IllegalStateException  if the underlying transport is not in the
   *  appropriate state
   */
  void send(InputStream in, int len)
    throws TransportException, IllegalStateException;

  /** Receive some bytes.
   *  <ul>
   *  <li>The receive will block until Timeout is reached, blocksize is read, 
   *  or there is nothing left to read.
   *  </li>
   *  <li>If there is no data to be read because we are at "EOF" then null
   *  should be returned</li>
   *  </ul>
   *  @return An array of bytes
   *  @see #setBlockSize(int)
   *  @see #setTimeout(int)
   *  @throws TransportException An error was encountered with the transport
   *  @throws InterruptedIOException The timeout was exceeded
   *  @throws IllegalStateException  if the underlying transport is not in the
   *  appropriate state
   */
  byte[] receive()
    throws InterruptedIOException, TransportException, IllegalStateException;

  /** Read a single byte.
   *  <ul>
   *  <li>The receive will block until Timeout is reached or until a single byte
   *  is read</li>
   *  <li>If there is no data to be read because we are at "EOF" then -1
   *  should be returned</li>
   *  </ul>
   *  @see #setTimeout(int)
   *  @throws TransportException An error was encountered with the transport
   *  @throws InterruptedIOException The timeout was exceeded
   *  @throws IllegalStateException  if the underlying transport is not in the
   *  appropriate state
   *  @return a single byte.
   */
  byte read()
    throws InterruptedIOException, TransportException, IllegalStateException;
    
  /** Rewind the last read.
   *  <p>Rewind the last read operation by the number of bytes specified.
   *  <p>It is implementation specific as to how many bytes are available to be
   *  rewound.  The general contract is that it should be possible to rewind
   *  at least the number of bytes that were returned by the last <code>.
   *  read()</code> or <code>receive()</code> operation.  
   *  <p>It is 
   *  possible for the rewind operation will fail if the block size is changed.
   * @param size the number of bytes to rewind.
   * @throws TransportException if the rewind failed.
   */
  void rewind(int size) throws TransportException;

  /** Set the max block size to read.
   *  <p> This method will affect the next call to <code>receive()</code> and
   *  all subsequent calls.
   *  @param size the maximum block size to read
   *  @see #receive()
   *  @see #rewind(int)
   *  @throws TransportException An error was encountered with the transport
   */
  void setBlockSize(int size) throws TransportException;

  /** Set the Timeout to wait for data.
   *  @param ms the length of time in milliseconds
   *  @throws TransportException An error was encountered with the transport
   */
  void setTimeout(int ms) throws TransportException;

  /** Close the underlying transport layer.
   */
  void close();

}
