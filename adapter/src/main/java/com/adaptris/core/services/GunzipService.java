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

import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Unzip the given payload.
 * <p>
 * This simply uses <code>java.util.zip.GZIPInputStream</code> in order to extract the un-compressed bytes.
 * </p>
 * <p>
 * If the payload is not considered compressed, then a ServiceException will be thrown.
 * </p>
 * 
 * @config gunzip-service
 * 
 */
@XStreamAlias("gunzip-service")
public class GunzipService extends ServiceImp {

  /**
   * @see com.adaptris.core.Service#doService(AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    GZIPInputStream in = null;
    OutputStream out = null;

    try {
      out = msg.getOutputStream();
      in  = new GZIPInputStream(msg.getInputStream());
      StreamUtil.copyStream(in, out);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void prepare() throws CoreException {
  }

}
