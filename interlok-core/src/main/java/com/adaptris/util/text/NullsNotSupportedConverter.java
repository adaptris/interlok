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

package com.adaptris.util.text;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * Implementation of NullConverter that throws an UnsupportedOperationException if null is the param.
 * 
 * @config nulls-not-supported-converter
 * 
 * @author lchan
 * 
 */
@JacksonXmlRootElement(localName = "nulls-not-supported-converter")
@XStreamAlias("nulls-not-supported-converter")
public class NullsNotSupportedConverter implements NullConverter {

  @Override
  public <T> T convert(T t) {
    if (t == null) {
      throw new UnsupportedOperationException("Null values are not supported");
    }
    return t;
  }

}
