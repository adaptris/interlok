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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Null implemenation of <code>AdaptrisConnection</code>.
 * </p>
 * 
 * @config null-connection
 */
@XStreamAlias("null-connection")
@AdapterComponent
@ComponentProfile(summary = "The default NO-OP connection", tag = "connections")
public class NullConnection extends AdaptrisConnectionImp {

  public NullConnection() {
    super();
    setConnectionErrorHandler(null);
  }

  public NullConnection(String uniqueId) {
    this();
    setUniqueId(uniqueId);
  }

  @Override
  protected void initConnection() throws CoreException {
    ;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#startConnection()
   */
  @Override
  protected void startConnection() throws CoreException {
    ;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#stopConnection()
   */
  @Override
  protected void stopConnection() {
    ;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#closeConnection()
   */
  @Override
  protected void closeConnection() {
    ;
  }

  @Override
  protected void prepareConnection() throws CoreException {
  }

}
