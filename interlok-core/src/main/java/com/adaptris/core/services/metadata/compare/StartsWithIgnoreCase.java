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

package com.adaptris.core.services.metadata.compare;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * Used with {@link MetadataComparisonService}.
 * 
 * @config metadata-starts-with-ignore-case
 * @author lchan
 * 
 */
@JacksonXmlRootElement(localName = "metadata-starts-with-ignore-case")
@XStreamAlias("metadata-starts-with-ignore-case")
@AdapterComponent
@ComponentProfile(summary = "Tests that a configured metadata value starts with the supplied value, ignoring case.", tag = "operator,comparator,metadata")
public class StartsWithIgnoreCase extends StartsWith {

  public StartsWithIgnoreCase() {
    super();
    setIgnoreCase(true);
  }

  public StartsWithIgnoreCase(String result) {
    this();
    setResultKey(result);
  }
}
