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

package com.adaptris.core.services;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Changes the character encoding associated with a message.
 * <p>
 * this service does nothing with the data, but simply changes the character encoding associated with the message using
 * {@link com.adaptris.core.AdaptrisMessage#setCharEncoding(String)}. If this service is used, and there is no configured character encoding then the
 * character encoding associated with the message is set to null (which forces the platform default encoding).
 * </p>
 * 
 * @config change-char-encoding-service
 * 
 * 
 */
@XStreamAlias("change-char-encoding-service")
@AdapterComponent
@ComponentProfile(summary = "Change the character encoding of a message", tag = "service")
@DisplayOrder(order = {"charEncoding"})
public class ChangeCharEncodingService extends ServiceImp {

  private String charEncoding;

  public ChangeCharEncodingService() {
    super();
    setCharEncoding(null);
  }

  public ChangeCharEncodingService(String cs) {
    this();
    setCharEncoding(cs);
  }


  /**
   * @see com.adaptris.core.Service #doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    msg.setContentEncoding(getCharEncoding());
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }
  public String getCharEncoding() {
    return charEncoding;
  }

  /**
   * Set the character encoding
   *
   * @param s the character encoding
   */
  public void setCharEncoding(String s) {
    charEncoding = s;
  }

  @Override
  public void prepare() throws CoreException {
  }

}
