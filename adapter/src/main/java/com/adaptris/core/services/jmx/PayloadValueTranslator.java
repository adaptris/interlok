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

package com.adaptris.core.services.jmx;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of {@link ValueTranslator} will pull the string payload value from the
 * {@link com.adaptris.core.AdaptrisMessage} to be used as a Jmx operation parameter. Conversely we can also take a
 * string result from a Jmx operation call and overwrite the payload with the new value.
 * </p>
 * 
 * @author amcgrath
 * @config jmx-payload-value-translator
 * @since 3.0.3
 */
@XStreamAlias("jmx-payload-value-translator")
public class PayloadValueTranslator extends ValueTranslatorImp {
  
  
  public PayloadValueTranslator() {
    super();
  }
  
  @Override
  public Object getValue(AdaptrisMessage message) {
    return message.getContent();
  }

  @Override
  public void setValue(AdaptrisMessage message, Object object) {
    message.setContent((String) object, message.getContentEncoding());
  }

}
