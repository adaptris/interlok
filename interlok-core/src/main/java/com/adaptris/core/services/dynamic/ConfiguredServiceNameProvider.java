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

package com.adaptris.core.services.dynamic;

import java.util.HashSet;
import java.util.Set;

import com.adaptris.core.TradingRelationship;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ServiceNameProvider} that has static mappings for trading relationships and service names
 * <p>
 * This is primiarily a basic implementation of <code>ServiceNameProvider</code> for testing. We do not recommend it for production
 * use as additional dynamic services will require you to restart the adapter every time you add one.
 * </p>
 * 
 * @config configured-service-name-provider
 */
@XStreamAlias("configured-service-name-provider")
public class ConfiguredServiceNameProvider extends ServiceNameProviderImp {

  private Set<ServiceNameMapper> serviceNameMappers;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ConfiguredServiceNameProvider() {
    serviceNameMappers = new HashSet<ServiceNameMapper>();
  }

  public ConfiguredServiceNameProvider(Set<ServiceNameMapper> set) {
    this();
    setServiceNameMappers(set);
  }

  @Override
  protected String retrieveName(TradingRelationship t) {
    Args.notNull(t, "relationship");
    String result = null;
    for (ServiceNameMapper candidate : serviceNameMappers) {
      if (candidate.getTradingRelationship().equals(t)) {
        result = candidate.getServiceName();
        break;
      }
    }

    return result;
  }


  // properties

  /**
   * <p>
   * Returns the <code>Set</code> of <code>ServiceNameMapper</code>s.
   * </p>
   *
   * @return the <code>Set</code> of <code>ServiceNameMapper</code>s
   */
  public Set<ServiceNameMapper> getServiceNameMappers() {
    return serviceNameMappers;
  }

  /**
   * <p>
   * Adds a <code>ServiceNameMapper</code> to the underlying store.
   * </p>
   *
   * @param s the <code>ServiceNameMapper</code> to add to the underlying store
   * @return true if <code>s</code> is added, otherwise false
   */
  public boolean addServiceNameMapper(ServiceNameMapper s) {
    return serviceNameMappers.add(s);
  }

  /**
   *
   * @param s the set of <code>ServiceNameMapper</code>s
   */
  public void setServiceNameMappers(Set<ServiceNameMapper> s) {
    serviceNameMappers = s;
  }
}
