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
import java.util.Arrays;
import java.util.Collection;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@code ServiceCollection} with an ordered list of {@link Service}s.
 * 
 * @config service-list
 */
@XStreamAlias("service-list")
@AdapterComponent
@ComponentProfile(summary = "A collection of services", tag = "service,base")
@DisplayOrder(order = {"restartAffectedServiceOnException"})
public class ServiceList extends ServiceListBase {

  @AdvancedConfig
  private Boolean allowForwardSearch;

  public ServiceList() {
    super();
  }

  public ServiceList(Collection<Service> serviceList) {
    this();
    setServices(new ArrayList<>(serviceList));
  }

  public ServiceList(Service... serviceList) {
    this(Arrays.asList(serviceList));
  }

}
