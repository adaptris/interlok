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
import java.net.ServerSocket;

import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Vanilla TCP Socket consume connection.
 * 
 * @config tcp-consume-connection
 * 
 * @license STANDARD
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("tcp-consume-connection")
public class TcpConsumeConnection extends ConsumeConnection {

  /**
   * @see ConsumeConnection#createServerSocket()
   */
  public ServerSocket createServerSocket() throws IOException {
    return new ServerSocket(getListenPort(), getBacklog());
  }

  /**
   * 
   * @see com.adaptris.core.AdaptrisConnectionImp#initConnection()
   */
  @Override
  protected void initConnection() throws CoreException {
  }

}
