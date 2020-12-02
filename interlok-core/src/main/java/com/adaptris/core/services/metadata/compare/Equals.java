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
import com.adaptris.core.MetadataElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.StringUtils;

/**
 * Used with {@link MetadataComparisonService}.
 * 
 * @config metadata-equals
 * @author lchan
 * 
 */
@XStreamAlias("metadata-equals")
@AdapterComponent
@ComponentProfile(summary = "Tests that a configured metadata value equals the supplied value.", tag = "operator,comparator,metadata")
public class Equals extends ComparatorImpl {

  public Equals() {
    super();
  }

  public Equals(String result) {
    this();
    setResultKey(result);
  }

  @Override
  public MetadataElement compare(MetadataElement firstItem, MetadataElement secondItem) {
    return new MetadataElement(getResultKey(), String.valueOf(compare(firstItem.getValue(), secondItem.getValue())));
  }

  @Override
  protected boolean compare(String a, String b) {
    return StringUtils.equals(a, b);
  }
}
