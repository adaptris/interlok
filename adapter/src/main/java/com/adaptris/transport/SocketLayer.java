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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/** For handling communications to and from a Socket.
 */
final class SocketLayer implements TransportLayer {
  private Socket socket = null;
  private OutputStream socketOut = null;
  private InputStream socketIn = null;
  private int blockSize = 1024;
  private ByteBuffer readBuffer;
  private boolean useBuffer = false;

  SocketLayer(Socket s) {
    socket = s;
  }

  /**
   *  @see com.adaptris.transport.TransportLayer#setTimeout(int)
   */
  public void setTimeout(int ms) throws TransportException {
    try {
      socket.setSoTimeout(ms);
    }
    catch (Exception e) {
      throw new TransportException(e);
    }
  }

  /**
   *  @see com.adaptris.transport.TransportLayer#setBlockSize(int)
   */
  public void setBlockSize(int size) throws TransportException {
    if (size < 0) {
      throw new TransportException("Invalid block size " + size);
    }
    blockSize = size;
    readBuffer = null;
  }

  /**
   *  @see com.adaptris.transport.TransportLayer#send(byte[])
   */
  public void send(byte[] bytes)
    throws TransportException, IllegalStateException {
    this.send(bytes, 0, bytes.length);
  }

  /**
   *  @see com.adaptris.transport.TransportLayer#send(byte[], int, int)
   */
  public void send(byte[] bytes, int offset, int len)
    throws TransportException, IllegalStateException {
    try {
      if (socketOut == null) {
        socketOut = this.socket.getOutputStream();
      }
      socketOut.write(bytes, offset, len);
      socketOut.flush();
    }
    catch (Exception e) {
      throw new TransportException(e);
    }
  }

  /**
   *  @see com.adaptris.transport.TransportLayer#send(java.io.InputStream, int)
   */
  public void send(InputStream input, int len)
    throws TransportException, IllegalStateException {
    try {
      if (socketOut == null) {
        socketOut = this.socket.getOutputStream();
      }
      byte[] bytes = new byte[len];
      int n = input.read(bytes);
      socketOut.write(bytes, 0, n);
      socketOut.flush();
    }
    catch (Exception e) {
      throw new TransportException(e);
    }
  }

  /**
   *  @see com.adaptris.transport.TransportLayer#receive()
   */
  public byte[] receive()
    throws InterruptedIOException, TransportException, IllegalStateException {
    return this.receive(blockSize);
  }

  /**
   *  @see com.adaptris.transport.TransportLayer#read()
   */
  public byte read()
    throws InterruptedIOException, TransportException, IllegalStateException {
    return this.receive(1)[0];
  }

  /**
   *  @see com.adaptris.transport.TransportLayer#close()
   */
  public void close() {
    try {
      if (socket != null) {
        socket.close();
      }
    }
    catch (Exception ignored) {
      ;
    }
    finally {
      socketOut = null;
      socketIn = null;
      socket = null;
    }
  }

  /** @see TransportLayer#rewind(int)
   */
  public void rewind(int size) throws TransportException {
    if (size > blockSize) {
      throw new TransportException("Request rewind exceeds " + blockSize);
    }
    if (readBuffer != null) {
      readBuffer.position(readBuffer.array().length - size);
    }
    useBuffer = true;
  }

  private int readFromBuffer(byte[] b) {
    int read = 0;
    if (readBuffer.hasRemaining()) {
      if (readBuffer.remaining() < b.length) {
        read = readBuffer.remaining();
        readBuffer.get(b, 0, read);
        readBuffer = null;
      }
      else {
        read = blockSize;
        readBuffer.get(b);
      }
    }
    else {
      readBuffer = null;
    }
    return read;
  }

  private byte[] receive(int readSize)
    throws InterruptedIOException, TransportException, IllegalStateException {
    byte[] b = new byte[readSize];

    int read = 0;
    if (readSize == 0) {
      return b;
    }
    try {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      if (socketIn == null) {
        socketIn = socket.getInputStream();
      }

      if (readBuffer != null && useBuffer) {
        read = readFromBuffer(b);
      }
      else {
        readBuffer = null;
      }
      if (read < readSize) {
        int bytesRead = socketIn.read(b, read, readSize - read);
        read += bytesRead;
      }
      if (readBuffer == null) {
        readBuffer = ByteBuffer.wrap(makeCopy(b));
        useBuffer = false;
      }
      output.write(b, 0, read);
      b = output.toByteArray();
    }
    catch (InterruptedIOException e) {
      throw e;
    }
    catch (Exception e) {
      throw new TransportException(e);
    }
    return b;

  }

  private static byte[] makeCopy(byte[] bytes) throws IOException {
    byte[] b = new byte[bytes.length];
    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    in.read(b);
    return b;
  }
}
