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

import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Gzip the given payload.
 * <p>
 * This simply uses {@link GZIPOutputStream} in order to create the compressed bytes.
 * </p>
 * 
 * @config gzip-service
 * 
 * 
 */
@XStreamAlias("gzip-service")
@AdapterComponent
@ComponentProfile(summary = "GZIP the contents of the message", tag = "service,zip")
public class GzipService extends ServiceImp {

  /**
   *  @see com.adaptris.core.Service#doService(AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    InputStream in = null;
    GZIPOutputStream out = null;
    try {
      in = msg.getInputStream();
      out = new GZIPOutputStream(msg.getOutputStream());
      IOUtils.copy(in, out);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }

  @Override
  public void prepare() throws CoreException {
  }


  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }
}
