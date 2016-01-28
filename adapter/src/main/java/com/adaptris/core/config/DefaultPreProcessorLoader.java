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
import static com.adaptris.core.util.PropertyHelper.getPropertyIgnoringCase;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.management.AdapterConfigManager;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @config default-pre-processor-loader
 * @author lchan
 *
 */
@XStreamAlias("default-pre-processor-loader")
public class DefaultPreProcessorLoader implements ConfigPreProcessorLoader {

  private transient Logger log = LoggerFactory.getLogger(DefaultPreProcessorLoader.class);

  private static final String PRE_PROCESSOR_SEPARATOR = ":";
  private static final String NAME_PROPERTY_KEY = "name";
  private static final String CLASS_PROPERTY_KEY = "class";
  private static final transient String PRE_PROCESSOR_RESOURCE = "META-INF/com/adaptris/core/preprocessor/";

  private transient PropertyLoader propertyLoader;

  public DefaultPreProcessorLoader() {
    this.setPropertyLoader(new PropertyLoader());
  }

  @Override
  public ConfigPreProcessors load(BootstrapProperties bootstrapProperties) throws CoreException {
    ConfigPreProcessors preProcessorsList = new ConfigPreProcessors();
    String prePrecessorList = getPropertyIgnoringCase(bootstrapProperties,
        AdapterConfigManager.CONFIGURATION_PRE_PROCESSORS);
    if (!isEmpty(prePrecessorList)) {
      String[] configuredPreProcessors = prePrecessorList.split(PRE_PROCESSOR_SEPARATOR);
      for (String preProcessor : configuredPreProcessors) {
        try {
          preProcessorsList.add(resolve(preProcessor, bootstrapProperties));
        }
        catch (Exception e) {
          log.warn("Unable to find pre-processor with name: ]{}].  Ignoring.", preProcessor);
        }
      }
    }
    return preProcessorsList;
  }

  @Override
  public ConfigPreProcessors load(String preProcessors, KeyValuePairSet config) throws CoreException {
    ConfigPreProcessors preProcessorsList = new ConfigPreProcessors();
    if (!isEmpty(preProcessors)) {
      String[] configuredPreProcessors = preProcessors.split(PRE_PROCESSOR_SEPARATOR);
      for (String preProcessor : configuredPreProcessors) {
        try {
          preProcessorsList.add(resolve(preProcessor, config));
        } catch (Exception e) {
          log.warn("Unable to find pre-processor with name: ]{}].  Ignoring.", preProcessor);
        }
      }
    }
    return preProcessorsList;
  }

  private ConfigPreProcessor resolve(String name, BootstrapProperties bootstrapProperties) throws CoreException {
    ConfigPreProcessor result = null;
    Properties p = getPropertyLoader().loadPropertyFile(name);
    String classname = p.getProperty(CLASS_PROPERTY_KEY);
    if (isBlank(classname)) {
      result = this.createInstance(name, bootstrapProperties);
    }
    else {
      result = this.createInstance(classname, bootstrapProperties);
    }
    return result;
  }

  private ConfigPreProcessor resolve(String name, KeyValuePairSet config) throws CoreException {
    ConfigPreProcessor result = null;
    Properties p = getPropertyLoader().loadPropertyFile(name);
    String classname = p.getProperty(CLASS_PROPERTY_KEY);
    if (isBlank(classname)) {
      result = this.createInstance(name, config);
    } else {
      result = this.createInstance(classname, config);
    }
    return result;
  }

  private ConfigPreProcessor createInstance(String classname, BootstrapProperties bootstrapProperties) throws CoreException {
    ConfigPreProcessor preProcessor = null;
    log.trace("Loading pre-processor: " + classname);
    Class<?>[] paramTypes = {BootstrapProperties.class};
    Object[] args = {bootstrapProperties};
    try {
      Class<?> clazz = Class.forName(classname);
      Constructor<?> cnst = clazz.getDeclaredConstructor(paramTypes);
      preProcessor = ((ConfigPreProcessor) cnst.newInstance(args));
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }

    return preProcessor;
  }

  private ConfigPreProcessor createInstance(String classname, KeyValuePairSet config) throws CoreException {
    ConfigPreProcessor preProcessor = null;
    log.trace("Loading pre-processor: " + classname);
    Class<?>[] paramTypes = {KeyValuePairSet.class};
    Object[] args = {config};
    try {
      Class<?> clazz = Class.forName(classname);
      Constructor<?> cnst = clazz.getDeclaredConstructor(paramTypes);
      preProcessor = ((ConfigPreProcessor) cnst.newInstance(args));
    } catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }

    return preProcessor;
  }


  private PropertyLoader getPropertyLoader() {
    return propertyLoader;
  }

  void setPropertyLoader(PropertyLoader loader) {
    this.propertyLoader = Args.notNull(loader, "property loader");
  }

  class PropertyLoader {

    public Properties loadPropertyFile(String name) {
      Properties p = new Properties();
      try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(PRE_PROCESSOR_RESOURCE + name)) {
        if (in != null) {
          p.load(in);
        }
      }
      catch (IOException e) {
        // Just return an empty properties is fine.
      }
      return p;
    }
  }

}
