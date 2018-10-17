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

package com.adaptris.core;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Holder for example <code>Service</code>s so that xsi:type is output in XML.
 * </p>
 */
@XStreamAlias("dummy-placeholder-service-element")
public class ExampleServiceConfig {

  // Note that this is a list so that it comes out nice in XSTream.
  // so we get
  // <dummy-placeholder-service-element>
  // <add-metadata-service>
  // </add-metadata-service>
  // </dummy-placeholder-service-element>
  // rather than
  // <dummy-placeholder-service-element>
  // <service class="add-metadata-service">
  // </service>
  // </dummy-placeholder-service-element>
  private List<Service> services = new ArrayList<Service>();

  public List<Service> getServices() {
    return services;
  }

  public void setServices(List<Service> s) {
    services = s;

  }

  public void addService(Service s) {
    services.add(s);
  }
}
