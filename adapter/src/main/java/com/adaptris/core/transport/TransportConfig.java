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

package com.adaptris.core.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.transport.Transport;
import com.adaptris.transport.TransportException;

/** This class is the base abstract class for configuration an arbitary
 *  Transport.
 * @author lchan
 * @author $Author: hfraser $
 */
public abstract class TransportConfig {

  private int timeout;
  private int blocksize;

  protected transient Logger logR = LoggerFactory.getLogger(this.getClass());

  /** @see Object#Object()
   *
   *
   */
  public TransportConfig() {
    setBlockSize(8192);
    setTimeoutMs(60000);
  }


  /** The transport package is traditionally configured using properties.
   * @return an implementation of the Transport
   * @throws TransportException on error.
   * @see Transport
   */
  public abstract Transport createTransport() throws TransportException;

  /** Set the blocksize.
   *
   * @param initialBlockSize the blocksize
   */
  public void setBlockSize(int initialBlockSize) {
    blocksize = initialBlockSize;
  }

  /** Set the timeout.
   *
   * @param dataTimeout the timeout in milliseconds after which an
   * operation is considered to have failed.
   */
  public void setTimeoutMs(int dataTimeout) {
    timeout = dataTimeout;
  }

  /** Get the blocksize.
   *
   * @return the blocksize.
   */
  public int getBlockSize() {
    return blocksize;
  }

  /** Get the timeout.
   *
   * @return the timeout.
   */
  public int getTimeoutMs() {
    return timeout;
  }

}
