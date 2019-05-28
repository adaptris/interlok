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

package com.adaptris.core.common;

import java.io.IOException;
import java.io.InputStream;

import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.types.MessageWrapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This {@code DataInputParameter} is used when you want to source data from the {@link com.adaptris.core.AdaptrisMessage} payload.
 * 
 * 
 * @author amcgrath
 * @config stream-payload-input-parameter
 * 
 */
@XStreamAlias("stream-payload-input-parameter")
public class PayloadStreamInputParameter
    implements DataInputParameter<InputStream>, MessageWrapper<InputStream> {

  public PayloadStreamInputParameter() {
    
  }
  
  @Override
  public InputStream extract(InterlokMessage message) throws InterlokException {
    InputStream result = null;
    try {
      result = message.getInputStream();
    } catch (IOException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
    return result;
  }

  @Override
  public InputStream wrap(InterlokMessage m) throws Exception {
    return extract(m);
  }

}
