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

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

//For testing the @@XStreamImplicit annotation being parsed by adp-core-apt for for XStreamMarshaller
@XStreamAlias("xstream-implicit-wrapper")
public class XStreamImplicitWrapper extends XStreamImplicitWrapperImpl {

  @XStreamImplicit(itemFieldName = "marshalled-string")
  private List<String> marshalledStrings = new ArrayList<String>();

  public XStreamImplicitWrapper() {
  }

  public List<String> getMarshalledStrings() {
    return marshalledStrings;
  }

  public void setMarshalledStrings(List<String> marshalledStrings) {
    this.marshalledStrings = marshalledStrings;
  }

  public void addMarshalledString(String s) {
    marshalledStrings.add(s);
  }
}
