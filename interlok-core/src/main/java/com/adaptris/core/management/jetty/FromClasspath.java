/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.management.jetty;

import java.net.URL;
import java.util.Map;
import java.util.Properties;
import org.eclipse.jetty.util.resource.Resource;
import com.adaptris.core.management.webserver.JettyServerManager;

/**
 * Build a jetty server from a failsafe XML configuration
 * 
 */
public final class FromClasspath extends FromXmlConfig {

  private static final String DEFAULT_DESCRIPTORS_KEY = "jetty.deploy.defaultsDescriptorPath";

  public FromClasspath(Properties initialConfig) {
    super(initialConfig);
  }

  @Override
  protected Resource getJettyConfigResource() throws Exception {
    URL jettyFailsafe = getClass().getClassLoader().getResource(JettyServerManager.DEFAULT_JETTY_XML);
    return Resource.newResource(jettyFailsafe);
  }

  @Override
  protected Map<String, String> mergeWithSystemProperties() {
    Map<String, String> result = super.mergeWithSystemProperties();
    if (!result.containsKey(DEFAULT_DESCRIPTORS_KEY)) {
      URL descriptorURL = getClass().getClassLoader().getResource(JettyServerManager.DEFAULT_DESCRIPTOR_XML);
      result.put(DEFAULT_DESCRIPTORS_KEY, descriptorURL.toString());
    }
    return result;
  }

}
