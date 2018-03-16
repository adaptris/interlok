/*
 * Copyright 2018 Adaptris Ltd.
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

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.lms.ZipFileBackedMessageFactory;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Forces IO to happen from the message inputstream to outputstream.
 * 
 * <p>
 * This service is only included for completeness as there are only a limited number of use-cases for it. For instance, if your
 * message-factory is a {@link ZipFileBackedMessageFactory} ({@code mode=Uncompress}) and you do not do any IO on the message in the
 * workflow then the message may still be compressed when you write it out to the filesystem. Using this service simply forces IO to
 * happen on the message.
 * </p>
 */
@XStreamAlias("input-output-service")
@ComponentProfile(summary = "Force IO to happen on a message.", tag = "service", since = "3.7.1")
public class InputOutputService extends ServiceImp {

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try (InputStream in = msg.getInputStream(); OutputStream out = msg.getOutputStream()){
      IOUtils.copy(in, out);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    // nothing to do.
  }

  @Override
  protected void initService() throws CoreException {
    // nothing to do.
  }

  @Override
  protected void closeService() {
    // nothing to do.
  }

}
