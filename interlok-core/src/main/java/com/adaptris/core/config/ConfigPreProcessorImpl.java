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

package com.adaptris.core.config;

import java.util.Properties;

import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;

/**
 * <p>
 * An abstract implementation of the {@link ConfigPreProcessor}
 * </p>
 * <p>
 * All concrete classes will have access to the bootstrap.properties should their pre-processing
 * need extra configuration.
 * </p>
 * @author amcgrath
 *
 */
public abstract class ConfigPreProcessorImpl implements ConfigPreProcessor {

  private KeyValuePairSet configuration;
  
  public ConfigPreProcessorImpl(BootstrapProperties properties) {
    this.setBootstrapProperties(properties);
  }

  public ConfigPreProcessorImpl(KeyValuePairSet kvp) {
    setConfiguration(kvp);
  }


  /**
   * @deprecated use {@link #getProperties()} instead.
   */
  @Deprecated
  public BootstrapProperties getBootstrapProperties() {
    return new BootstrapProperties(KeyValuePairBag.asProperties(getConfiguration()));
  }

  /**
   * @deprecated use {@link #setProperties(Properties)} instead.
   */
  @Deprecated
  public void setBootstrapProperties(BootstrapProperties bp) {
    setConfiguration(new KeyValuePairSet(bp));
  }


  public Properties getProperties() {
    return KeyValuePairBag.asProperties(getConfiguration());
  }

  public void setProperties(Properties bp) {
    setConfiguration(new KeyValuePairSet(bp));
  }


  public KeyValuePairSet getConfiguration() {
    return configuration;
  }

  public void setConfiguration(KeyValuePairSet kvps) {
    configuration = Args.notNull(kvps, "Configuration");
  }

}
