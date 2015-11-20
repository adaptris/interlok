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

import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@code DataInputParameter} is used when you want to source data from the {@link com.adaptris.core.AdaptrisMessage} payload.
 * </p>
 * <p>
 * An example might be specifying that the XML content required for the {@link com.adaptris.core.services.xml.XPathService} can be
 * found in
 * the payload of an {@link com.adaptris.core.AdaptrisMessage}.
 * </p>
 * 
 * @author amcgrath
 * @config string-payload-data-input-parameter
 * 
 */
@XStreamAlias("string-payload-data-input-parameter")
public class StringPayloadDataInputParameter implements DataInputParameter<String> {

  public StringPayloadDataInputParameter() {
    
  }
  
  @Override
  public String extract(InterlokMessage message) {
    return message.getContent();
  }

}
