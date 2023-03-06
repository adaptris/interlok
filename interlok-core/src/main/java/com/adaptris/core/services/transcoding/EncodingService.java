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

import java.io.OutputStream;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoder;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Encodes the in flight message and sets the payload to the encoded output.
 * 
 * @config encoding-service
 *
 */
@JacksonXmlRootElement(localName = "encoding-service")
@XStreamAlias("encoding-service")
@AdapterComponent
@ComponentProfile(summary = "Encodes the message", tag = "service")
public class EncodingService extends TranscodingService {

  public EncodingService(){
  }

  public EncodingService(AdaptrisMessageEncoder encoder) {
    super(encoder);
  }

  @Override
  public void transcodeMessage(AdaptrisMessage msg) throws ServiceException {
    try (OutputStream out = msg.getOutputStream()) {
      getEncoder().writeMessage(msg,out);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }
}
