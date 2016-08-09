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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoder;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.io.IOUtils;

import java.io.OutputStream;

/**
 * Encodes the in flight message and sets the payload to the encoded output.
 * 
 * @config encoding-service
 *
 */
@XStreamAlias("encoding-service")
@AdapterComponent
@ComponentProfile(summary = "Encodes the message", tag = "service")
public class EncodingService extends CodecService {

  public EncodingService(){
  }

  public EncodingService(AdaptrisMessageEncoder encoder) {
    super(encoder);
  }

  @Override
  public void codecAction(AdaptrisMessage msg) throws ServiceException {
    OutputStream out = null;
    try {
      out = msg.getOutputStream();
      getEncoder().writeMessage(msg,out);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }
}
