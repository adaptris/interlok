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
import org.apache.commons.io.IOUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Base64 Decode the message.
 * 
 * @config base64-decode-service
 * 
 * 
 */
@JacksonXmlRootElement(localName = "base64-decode-service")
@XStreamAlias("base64-decode-service")
@AdapterComponent
@ComponentProfile(summary = "Base64 decode the message", tag = "service,base64")
public class Base64DecodeService extends Base64Service {

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try (InputStream in = style().decoder().wrap(msg.getInputStream()); OutputStream out = msg.getOutputStream()) {
      IOUtils.copy(in, out);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
  }

}
