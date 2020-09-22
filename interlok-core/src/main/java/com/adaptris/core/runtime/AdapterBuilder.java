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

import static org.apache.commons.lang3.StringUtils.isBlank;
import java.io.IOException;
import java.util.Properties;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.core.Adapter;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.config.ConfigPreProcessorLoader;
import com.adaptris.core.config.ConfigPreProcessors;
import com.adaptris.core.config.DefaultPreProcessorLoader;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.vcs.RuntimeVersionControl;
import com.adaptris.core.management.vcs.RuntimeVersionControlLoader;
import com.adaptris.core.management.vcs.VcsConstants;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.util.URLString;

class AdapterBuilder implements AdapterBuilderMBean {
  private static final String JMX_REGISTRY_TYPE = AdapterComponentMBean.JMX_DOMAIN_NAME + ":type=Builder";

  private static final String JMX_OBJECT_PREFIX =
      JMX_REGISTRY_TYPE +
      AdapterComponentMBean.PROPERTY_SEPARATOR + AdapterComponentMBean.KEY_ID + AdapterComponentMBean.EQUALS;


  private static transient Logger log = LoggerFactory.getLogger(AdapterRegistry.class);
  private transient BootstrapProperties config;
  private transient ConfigPreProcessorLoader configurationPreProcessorLoader = new DefaultPreProcessorLoader();
  private transient RuntimeVersionControl runtimeVCS;
  private transient AdapterRegistry parent;
  private transient ObjectName myObjectName;

  private AdapterBuilder() {
  }

  public AdapterBuilder(AdapterRegistry owner, Properties cfg) throws MalformedObjectNameException {
    this();
    parent = owner;
    config = new BootstrapProperties(cfg);
    runtimeVCS = loadVCS();
    myObjectName = ObjectName.getInstance(JMX_OBJECT_PREFIX + Integer.toHexString(System.identityHashCode(cfg)));
  }

  private RuntimeVersionControl loadVCS() {
    RuntimeVersionControlLoader loader = RuntimeVersionControlLoader.getInstance();
    RuntimeVersionControl result = loader.load(config.getProperty(VcsConstants.VSC_IMPLEMENTATION));
    if (result == null) {
      result = loader.load();
    }
    if (result != null) {
      result.setBootstrapProperties(config);
    }
    return result;
  }

  @Override
  public ObjectName createAdapter() throws IOException, MalformedObjectNameException, CoreException {
    return createAdapter(new URLString(config.findAdapterResource()));
  }

  @Override
  public ObjectName createAdapter(URLString url) throws IOException, MalformedObjectNameException, CoreException {
    String xml = loadPreProcessors().process(url.getURL());
    return parent.register(this, validate((Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(xml)), url);
  }

  @Override
  public ObjectName createAdapterFromUrl(String url) throws IOException, MalformedObjectNameException, CoreException {
    return createAdapter(new URLString(url));
  }

  @Override
  public ObjectName createAdapter(final String xml) throws IOException, MalformedObjectNameException, CoreException {
    return parent.register(this, validate((Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(loadPreProcessors().process(xml))), null);
  }

  @Override
  public void updateVCS() throws CoreException {
    if (runtimeVCS != null) {
      log.error("runtimeVCS {}", runtimeVCS);
      runtimeVCS.update();
    }
  }

  ConfigPreProcessors loadPreProcessors() throws CoreException {
    return configurationPreProcessorLoader.load(config);
  }

  void setConfigurationPreProcessorLoader(ConfigPreProcessorLoader p) {
    configurationPreProcessorLoader = p;
  }

  public Properties getConfiguration() {
    Properties result = new Properties();
    result.putAll(config);
    return result;
  }

  public String getVersionControl() {
    return runtimeVCS != null ? runtimeVCS.getImplementationName() : null;
  }

  // For testing.
  void overrideRuntimeVCS(RuntimeVersionControl newVcs) {
    runtimeVCS = newVcs;
  }

  private Adapter validate(Adapter adapter) throws CoreException {
    if (isBlank(adapter.getUniqueId())) {
      throw new CoreException("Adapter Unique ID is null/empty");
    }
    return adapter;
  }

  @Override
  public ObjectName createObjectName() {
    return myObjectName;
  }

  @Override
  public void registerMBean() throws CoreException {
    try {
      JmxHelper.register(createObjectName(), this);
    }
    catch (Exception e) {
      throw new CoreException(e);
    }

  }

  @Override
  public void unregisterMBean() throws CoreException {
    try {
      JmxHelper.unregister(createObjectName());
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }
}
