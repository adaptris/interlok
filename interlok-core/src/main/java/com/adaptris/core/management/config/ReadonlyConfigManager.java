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

package com.adaptris.core.management.config;

import com.adaptris.core.runtime.AdapterManagerMBean;

abstract class ReadonlyConfigManager extends ConfigManagerImpl {

  @Override
  public void syncAdapterConfiguration(AdapterManagerMBean adapter) throws Exception {
    log.warn("Method [syncAdapterConfiguration(Adapter adapter)] is not available for this implementation!");
  }
}
