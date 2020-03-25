/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.management;

import static com.adaptris.core.management.Constants.CFG_KEY_MANAGEMENT_COMPONENT;
import static com.adaptris.core.util.PropertyHelper.getPropertyIgnoringCase;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.ClosedState;
import com.adaptris.core.management.classloader.ClassLoaderFactory;

/**
 * Simple factory that creates management components.
 *
 * @author lchan
 */
public class ManagementComponentFactory {

  private static transient Logger log = LoggerFactory.getLogger(ManagementComponentFactory.class.getName());

  private static final String COMPONENT_SEPARATOR = ":";
  private static final String PROPERTY_KEY = "class";
  private static final String RESOURCE_PATH = "META-INF/com/adaptris/core/management/components/";
  private static final String CLASSLOADER_KEY = "classloader";
  private static final ManagementComponentFactory INSTANCE = new ManagementComponentFactory();

  private final Map<Properties, List<ManagementComponentInfo>> managementComponents = new HashMap<>();
  
  private ManagementComponentFactory() {
  }

  public static List<ManagementComponentInfo> create(final BootstrapProperties p) throws Exception {
    if (!INSTANCE.getManagementComponents().containsKey(p)) {
      List<ManagementComponentInfo> manCompInfo = INSTANCE.createComponents(p);
      INSTANCE.getManagementComponents().put(p,manCompInfo );
    }
    return INSTANCE.getManagementComponents().get(p);
  }

  public static void initCreated(BootstrapProperties bootstrapProperties) {
    for(ManagementComponentInfo manCompInfo : INSTANCE.getManagementComponents().get(bootstrapProperties)) {
      try {
        manCompInfo.getInstance().init(bootstrapProperties);
      } catch (Exception e) {
        log.error("Failed to initialize management component []", manCompInfo.getName(), e);
      }
    }
  }

  public static void startCreated(BootstrapProperties bootstrapProperties) {
    for(ManagementComponentInfo manCompInfo : INSTANCE.getManagementComponents().get(bootstrapProperties)) {
      try {
        manCompInfo.getInstance().start();
      } catch (Exception e) {
        log.error("Failed to start management component []", manCompInfo.getName(), e);
      }
    }
  }

  public static void stopCreated(BootstrapProperties bootstrapProperties, boolean reverseOrder) {
    for(ManagementComponentInfo manCompInfo : reverseOrder(INSTANCE.getManagementComponents().get(bootstrapProperties), reverseOrder)) {
      try {
        manCompInfo.getInstance().stop();
      } catch (Exception e) {
        log.error("Failed to stop management component []", manCompInfo.getName(), e);
      }
    }
  }

  public static void closeCreated(BootstrapProperties bootstrapProperties, boolean reverseOrder) {
    for(ManagementComponentInfo manCompInfo : reverseOrder(INSTANCE.getManagementComponents().get(bootstrapProperties), reverseOrder)) {
      try {
        manCompInfo.getInstance().destroy();
      } catch (Exception e) {
        log.error("Failed to destroy management component []", manCompInfo.getName(), e);
      }
    }
  }

  private static List<ManagementComponentInfo> reverseOrder(List<ManagementComponentInfo> list, boolean reverseOrder) {
    List<ManagementComponentInfo> newList = new ArrayList<>(list);
    if (reverseOrder) {
      Collections.reverse(newList);
    }
    return newList;
  }

  private Map<Properties, List<ManagementComponentInfo>> getManagementComponents() {
    return managementComponents;
  }

  private List<ManagementComponentInfo> createComponents(final BootstrapProperties bootstropProperties) throws Exception {
    List<ManagementComponentInfo> result = new ArrayList<>();
    final String componentList = getPropertyIgnoringCase(bootstropProperties, CFG_KEY_MANAGEMENT_COMPONENT, "");
    if (!isEmpty(componentList)) {
      final String components[] = componentList.split(COMPONENT_SEPARATOR);
      for (final String componentName : components) {
        ManagementComponent resolvedComponent = resolve(componentName, bootstropProperties);
        
        ManagementComponentInfo mcInfo = new ManagementComponentInfo();
        mcInfo.setClassName(resolvedComponent.getClass().getName());
        mcInfo.setState(ClosedState.getInstance());
        mcInfo.setName(componentName);
        mcInfo.setInstance(resolvedComponent);
        
        result.add(mcInfo);
      }
    }
    return result;
  }

  private ManagementComponent resolve(final String name, BootstrapProperties bootstrapProperties) throws Exception {
    final ClassLoader originalContectClassLoader = Thread.currentThread().getContextClassLoader();
    ClassLoader classLoader = getClass().getClassLoader();
    try (final InputStream in = classLoader.getResourceAsStream(RESOURCE_PATH + name)) {
      if (in != null) {
        final Properties p = new Properties();
        p.load(in);

        final String classloaderProperty = p.getProperty(CLASSLOADER_KEY);
        if (classloaderProperty != null) {
          log.debug("Using custom class loader " + classloaderProperty);
          final Class<ClassLoaderFactory> classLoaderFactoryClass = (Class<ClassLoaderFactory>)Class.forName(classloaderProperty);
          final Constructor<ClassLoaderFactory> constructor = classLoaderFactoryClass.getConstructor(BootstrapProperties.class);
          final ClassLoaderFactory classLoaderFactory = constructor.newInstance(bootstrapProperties);
          classLoader = classLoaderFactory.create(classLoader);
          Thread.currentThread().setContextClassLoader(classLoader);
        }
        final ManagementComponent component = (ManagementComponent) Class.forName(p.getProperty(PROPERTY_KEY), true, classLoader).newInstance();
        if (classloaderProperty != null) {
          component.setClassLoader(classLoader);
        }
        return component;
      }
      return (ManagementComponent) Class.forName(name).newInstance();
    } finally {
      Thread.currentThread().setContextClassLoader(originalContectClassLoader);
    }
  }

}
