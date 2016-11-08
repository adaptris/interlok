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

package com.adaptris.core.management;

import static com.adaptris.core.management.Constants.CFG_KEY_MANAGEMENT_COMPONENT;
import static com.adaptris.core.util.PropertyHelper.getPropertyIgnoringCase;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.management.classloader.ClassLoaderFactory;

/**
 * Simple factory that creates management components.
 * 
 * @author lchan
 */
public class ManagementComponentFactory {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  private static final String COMPONENT_SEPARATOR = ":";
  private static final String PROPERTY_KEY = "class";
  private static final String RESOURCE_PATH = "META-INF/com/adaptris/core/management/components/";
  private static final String CLASSLOADER_KEY = "classloader";
  private static final ManagementComponentFactory INSTANCE = new ManagementComponentFactory();

  private transient BootstrapProperties bootstrapProperties;
  
  private List<Object> managementComponents = new ArrayList<>();
  
  private ManagementComponentFactory() {
  }

  public static void create(BootstrapProperties p) throws Exception {
    INSTANCE.createComponents(p);
  }
  
  public static void initCreated() {
    INSTANCE.invokeInit();
  }
  
  public static void startCreated() {
    INSTANCE.invokeCreated("start");
  }

  public static void stopCreated() {
    INSTANCE.invokeCreated("stop");
  }

  public static void closeCreated() {
    INSTANCE.invokeCreated("close");
  }

  private void createComponents(BootstrapProperties p) throws Exception {
    bootstrapProperties = p;
    String componentList = getPropertyIgnoringCase(p, CFG_KEY_MANAGEMENT_COMPONENT, "");
    if (!isEmpty(componentList)) {
      String components[] = componentList.split(COMPONENT_SEPARATOR);
      for (String c : components) {
        managementComponents.add(resolve(c));
      }
    }
  }
  
  private void invokeInit() {
    for (Object o : managementComponents) {
      try {
        Method init = o.getClass().getDeclaredMethod("init", new Class[] { Properties.class });
        init.invoke(o, new Object[] { bootstrapProperties });
        log.debug("Called init on " + o.getClass());
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }
  
  private void invokeCreated(String methodName) {
    for (Object o : managementComponents) {
      try {
        Method init = o.getClass().getDeclaredMethod(methodName, new Class[0]);
        init.invoke(o, new Object[0]);
        log.debug("Called " + methodName + " on " + o.getClass());
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  private Object resolve(String name) throws Exception {
    Object result = null;
    InputStream in = this.getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH + name);
    if (in != null) {
      Properties p = new Properties();
      p.load(in);
      
      ClassLoader classLoader = getClass().getClassLoader();
      
      String classloaderProperty = p.getProperty(CLASSLOADER_KEY);
      if (classloaderProperty != null) {
        log.debug("Using custom class loader " + classloaderProperty);
        Class<ClassLoaderFactory> clazz = (Class<ClassLoaderFactory>) Class.forName(classloaderProperty);
        Constructor<ClassLoaderFactory> c = clazz.getConstructor(BootstrapProperties.class);
        ClassLoaderFactory clf = c.newInstance(bootstrapProperties);
        classLoader = clf.create(classLoader);
      }
      
      result = Class.forName(p.getProperty(PROPERTY_KEY), true, classLoader).newInstance();
    }
    else {
      // If we can't find it, then let's assume it's just a class.
      result = Class.forName(name).newInstance();
    }
    return result;
  }
}
