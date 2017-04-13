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

import static com.adaptris.core.util.PropertyHelper.getPropertyIgnoringCase;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.Adapter;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.config.ConfigPreProcessorLoader;
import com.adaptris.core.config.ConfigPreProcessors;
import com.adaptris.core.config.DefaultPreProcessorLoader;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.Constants;
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
  private transient ValidatorFactory validatorFactory;
  private transient ObjectName myObjectName;

  private AdapterBuilder() {
  }

  public AdapterBuilder(AdapterRegistry owner, Properties cfg) throws MalformedObjectNameException {
    this();
    parent = owner;
    this.config = new BootstrapProperties(cfg);
    runtimeVCS = loadVCS();
    boolean enableValidation = Boolean
        .valueOf(getPropertyIgnoringCase(cfg, Constants.CFG_KEY_VALIDATE_CONFIG, Constants.DEFAULT_VALIDATE_CONFIG))
        .booleanValue();
    if (enableValidation) {
      validatorFactory = Validation.buildDefaultValidatorFactory();
    }
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


  public ObjectName createAdapter() throws IOException, MalformedObjectNameException, CoreException {
    return createAdapter(new URLString(config.findAdapterResource()));
  }

  public ObjectName createAdapter(URLString url) throws IOException, MalformedObjectNameException, CoreException {
    String xml = loadPreProcessors().process(url.getURL());
    return parent.register(this, validate((Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(xml)), url);
  }

  public ObjectName createAdapterFromUrl(String url) throws IOException, MalformedObjectNameException, CoreException {
    return createAdapter(new URLString(url));
  }

  public ObjectName createAdapter(String xml) throws IOException, MalformedObjectNameException, CoreException {
    xml = loadPreProcessors().process(xml);
    return parent.register(this, validate((Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(xml)), null);
  }

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
    if (validatorFactory == null) {
      return adapter;
    }
    Validator validator = validatorFactory.getValidator();
    checkViolations(validator.validate(adapter));
    return adapter;
  }

  private void checkViolations(Set<ConstraintViolation<Adapter>> violations) throws CoreException {
    StringWriter writer = new StringWriter();
    if (violations.size() == 0) {
      return;
    }
    try (PrintWriter p = new PrintWriter(writer)) {
      p.println();
      for (ConstraintViolation v : violations) {
        String logString = String.format("Adapter Validation Error: [%1$s]=[%2$s]", v.getPropertyPath(), v.getMessage());
        p.println(logString);
        log.warn(logString);
      }
    }
    finally {
      IOUtils.closeQuietly(writer);
    }
    throw new CoreException(writer.toString());
  }

  static void assertNotNull(Object o, String msg) throws CoreException {
    if (o == null) {
      throw new CoreException(msg);
    }
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
