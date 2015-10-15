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

import com.adaptris.annotation.GenerateBeanInfo;
import com.adaptris.util.GuidGenerator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

// For testing the @GenerateBeanInfo annotation is doing it's job for XStreamMarshaller
@XStreamAlias("xstream-bean-info-wrapper")
@GenerateBeanInfo
public class XStreamBeanInfoWrapper {

  private String marshalledIdentity;
  private transient boolean setterCalled = false;

  public XStreamBeanInfoWrapper() {
    marshalledIdentity = new GuidGenerator().getUUID();
  }

  public String getMarshalledIdentity() {
    return marshalledIdentity;
  }

  public void setMarshalledIdentity(String s) {
    setterCalled = true;
    marshalledIdentity = s;
  }

  public boolean getSetterCalled() {
    return setterCalled;
  }

}
