/*
 * Copyright 2017 Adaptris Ltd.
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

import javax.management.ObjectName;

import com.adaptris.core.util.JmxHelper;

public class HelloWorld implements HelloWorldMBean {

  private ObjectName myName;

  public HelloWorld(ObjectName objectName) {
    myName = objectName;
  }

  public void register() throws Exception {
    JmxHelper.register(myName, this);
  }

  public void unregister() throws Exception {
    JmxHelper.unregister(myName);
  }

  @Override
  public String hello() {
    return "world";
  }

}
