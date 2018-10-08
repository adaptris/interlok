/*
 * Copyright 2018 Adaptris Ltd.
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

import com.adaptris.core.services.BranchingServiceEnabler;
import com.adaptris.core.services.StatelessServiceWrapper;

/**
 * A marker interface for Services that wrap other services.
 * 
 * <p>
 * There are some services that wrap services and yet aren't {@link ServiceCollection} instances (for instance :
 * {@link StatelessServiceWrapper} or {@link BranchingServiceEnabler}). This marker interface allows us to introduce some custom
 * handling for those services. The primary use here is to manage testing via the UI and usage of
 * {@link AdaptrisConnection#cloneForTesting()}.
 * </p>
 * <p>
 * Implementing this interface is optional and isn't required for normal Interlok runtime.
 * </p>
 *
 */
public interface ServiceWrapper extends Service {

  /**
   * Return all the services that are wrapped by this service.
   * 
   * @return an array of wrapped services.
   */
  Service[] wrappedServices();
}
