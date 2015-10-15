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

import com.adaptris.util.GuidGenerator;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

//For testing the @@XStreamImplicit annotation being parsed by adp-core-apt for for XStreamMarshaller
public abstract class XStreamImplicitWrapperImpl {

  private String marshalledIdentity;

  @XStreamImplicit(itemFieldName = "parent-string")
  private List<String> parentStrings = new ArrayList<String>();

  public XStreamImplicitWrapperImpl() {
    marshalledIdentity = new GuidGenerator().getUUID();
  }

  public String getMarshalledIdentity() {
    return marshalledIdentity;
  }

  public void setMarshalledIdentity(String marshalledIdentity) {
    this.marshalledIdentity = marshalledIdentity;
  }

  public List<String> getParentStrings() {
    return parentStrings;
  }

  public void setParentStrings(List<String> list) {
    parentStrings = list;
  }

  public void addParentString(String s) {
    parentStrings.add(s);
  }
}
