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

package com.adaptris.core.stubs;

import com.adaptris.annotation.MarshallingCDATA;
import com.adaptris.util.GuidGenerator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

// For testing the @CDATA annotation is doing it's job for XStreamMarshaller
@XStreamAlias("xstream-cdata-wrapper")
public class XStreamCDataWrapper extends XStreamCDataWrapperImpl {

  @MarshallingCDATA
  private String rawValue;

  public XStreamCDataWrapper() {
    rawValue = new GuidGenerator().getUUID();
  }

  public String getRawValue() {
    return rawValue;
  }

  public void setRawValue(String s) {
    rawValue = s;
  }

}
