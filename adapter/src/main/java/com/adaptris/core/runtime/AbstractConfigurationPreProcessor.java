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

package com.adaptris.core.runtime;

import com.adaptris.core.config.ConfigPreProcessorImpl;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.util.KeyValuePairSet;

/**
 * <p>
 * An abstract implementation of the {@link ConfigurationPreProcessor}
 * </p>
 * <p>
 * All concrete classes will have access to the bootstrap.properties should their pre-processing
 * need extra configuration.
 * </p>
 * @author amcgrath
 * @deprecated since 3.1.1 switch to using {@link ConfigPreProcessorImpl} instead.
 */
@Deprecated
public abstract class AbstractConfigurationPreProcessor extends ConfigPreProcessorImpl implements ConfigurationPreProcessor {

  public AbstractConfigurationPreProcessor(BootstrapProperties properties) {
    super(properties);
  }

  public AbstractConfigurationPreProcessor(KeyValuePairSet properties) {
    super(properties);
  }
}
