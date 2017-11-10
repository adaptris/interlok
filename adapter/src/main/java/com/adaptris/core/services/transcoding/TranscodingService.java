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

package com.adaptris.core.services.transcoding;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoder;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;

public abstract class TranscodingService extends ServiceImp {

  @Valid
  @NotNull
  private AdaptrisMessageEncoder encoder;
  @AdvancedConfig
  @Valid
  private AdaptrisMessageFactory messageFactory;

  public TranscodingService(){
  }

  public TranscodingService(AdaptrisMessageEncoder encoder){
    setEncoder(encoder);
  }

  /**
   * @see Service#doService(AdaptrisMessage)
   */
  public final void doService(AdaptrisMessage msg) throws ServiceException {
    encoder.registerMessageFactory(defaultIfNull(getMessageFactory()));
    transcodeMessage(msg);
  }

  public abstract void transcodeMessage(AdaptrisMessage msg) throws ServiceException;

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notNull(getEncoder(), "encoder");
      encoder.registerMessageFactory(defaultIfNull(getMessageFactory()));
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
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
    this.encoder = Args.notNull(encoder, "encoder");
  }

  public AdaptrisMessageFactory getMessageFactory() {
    return messageFactory;
  }

  public void setMessageFactory(AdaptrisMessageFactory f) {
    messageFactory = f;
  }
}
