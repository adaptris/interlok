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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Simple factory that creates management components.
 * 
 * @author lchan
 */
public class ManagementComponentFactory {

  private static final String COMPONENT_SEPARATOR = ":";
  private static final String PROPERTY_KEY = "class";
  private static final String RESOURCE_PATH = "META-INF/com/adaptris/core/management/components/";
  private static final ManagementComponentFactory INSTANCE = new ManagementComponentFactory();

  private ManagementComponentFactory() {
  }

  public static List<ManagementComponent> create(BootstrapProperties p) throws Exception {
    return INSTANCE.createComponents(p);
  }

  private List<ManagementComponent> createComponents(BootstrapProperties p) throws Exception {
    List<ManagementComponent> result = new ArrayList<ManagementComponent>();
    String componentList = getPropertyIgnoringCase(p, CFG_KEY_MANAGEMENT_COMPONENT, "");
    if (!isEmpty(componentList)) {
      String components[] = componentList.split(COMPONENT_SEPARATOR);
      for (String c : components) {
        result.add(resolve(c));
      }
    }
    return result;
  }

  private ManagementComponent resolve(String name) throws Exception {
    ManagementComponent result = null;
    InputStream in = this.getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH + name);
    if (in != null) {
      Properties p = new Properties();
      p.load(in);
      result = (ManagementComponent) Class.forName(p.getProperty(PROPERTY_KEY)).newInstance();
    }
    else {
      // If we can't find it, then let's assume it's just a class.
      result = (ManagementComponent) Class.forName(name).newInstance();
    }
    return result;
  }
}
