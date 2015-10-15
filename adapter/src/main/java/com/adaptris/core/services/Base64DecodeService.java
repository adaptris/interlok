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
import java.io.OutputStream;

import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.adaptris.util.text.mime.MimeConstants;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Base64 Decode the message.
 * 
 * @config base64-decode-service
 * 
 * @license BASIC
 */
@XStreamAlias("base64-decode-service")
public class Base64DecodeService extends ServiceImp {

  /**
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {

    OutputStream out = null;
    InputStream in = null;
    try {
      out = msg.getOutputStream();
      in = MimeUtility.decode(msg.getInputStream(), MimeConstants.ENCODING_BASE64);
      IOUtils.copy(in, out);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  public void close() {
    // TODO Auto-generated method stub

  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  public void init() throws CoreException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }
}
