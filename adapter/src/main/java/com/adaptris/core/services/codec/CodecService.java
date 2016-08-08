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

package com.adaptris.core.services.codec;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.*;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

public abstract class CodecService extends ServiceImp {

  private AdaptrisMessageEncoder encoder;
  @AdvancedConfig
  private AdaptrisMessageFactory messageFactory;

  /**
   * @see Service#doService(AdaptrisMessage)
   */
  public final void doService(AdaptrisMessage msg) throws ServiceException {
    registerEncoderMessageFactory();
    processMessage(msg);
  }

  public abstract void processMessage(AdaptrisMessage msg) throws ServiceException;

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void prepare() throws CoreException {
  }

  public AdaptrisMessageEncoder getEncoder() {
    return encoder;
  }

  public void setEncoder(AdaptrisMessageEncoder encoder) {
    this.encoder = encoder;
  }

  public AdaptrisMessageFactory getMessageFactory() {
    return messageFactory;
  }

  public void setMessageFactory(AdaptrisMessageFactory f) {
    messageFactory = f;
    registerEncoderMessageFactory();
  }

  private void registerEncoderMessageFactory() {
    if (encoder != null) {
      encoder.registerMessageFactory(defaultIfNull(getMessageFactory()));
    }
  }
}
