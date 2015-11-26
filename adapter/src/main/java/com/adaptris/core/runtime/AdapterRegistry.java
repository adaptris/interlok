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
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.JMX;
import javax.management.MBeanServer;
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
import com.adaptris.core.EventFactory;
import com.adaptris.core.event.AdapterShutdownEvent;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.management.BootstrapProperties;
import com.adaptris.core.management.Constants;
import com.adaptris.core.management.vcs.RuntimeVersionControl;
import com.adaptris.core.management.vcs.RuntimeVersionControlLoader;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.util.URLString;

import net.sf.saxon.trans.LicenseException;

@SuppressWarnings("deprecation")
public class AdapterRegistry implements AdapterRegistryMBean {

  private static final String EXCEPTION_MSG_XML_NULL = "XML String is null";
  private static final String EXCEPTION_MSG_URL_NULL = "URL is null";
  private static final String EXCEPTION_MSG_MBEAN_NULL = "AdapterManagerMBean is null";
  private static final String EXCEPTION_MSG_NO_VCR = "No Runtime Version Control";

  private Set<ObjectName> registeredAdapters;
  private transient ObjectName myObjectName;
  private transient MBeanServer mBeanServer;
  private static transient Logger log = LoggerFactory.getLogger(AdapterRegistry.class);
  private transient BootstrapProperties config = new BootstrapProperties();
  private transient ConfigurationPreProcessorLoader configurationPreProcessorLoader;
  private transient Map<ObjectName, URL> configurationURLs;
  private transient RuntimeVersionControl runtimeVCS;
  private transient ValidatorFactory validatorFactory = null;

  private AdapterRegistry() throws MalformedObjectNameException {
    registeredAdapters = new HashSet<ObjectName>();
    mBeanServer = JmxHelper.findMBeanServer();
    configurationPreProcessorLoader = new ConfigurationPreProcessorFactory();
    configurationURLs = new HashMap<ObjectName, URL>();
  }

  public AdapterRegistry(BootstrapProperties config) throws MalformedObjectNameException {
    this();
    this.config = config;
    generateObjectName();
    runtimeVCS = RuntimeVersionControlLoader.getInstance().load();
    if (runtimeVCS != null) {
      runtimeVCS.setBootstrapProperties(config);
    }
    boolean enableValidation =
        Boolean.valueOf(getPropertyIgnoringCase(config, Constants.CFG_KEY_VALIDATE_CONFIG, Constants.DEFAULT_VALIDATE_CONFIG))
            .booleanValue();
    if (enableValidation) {
      validatorFactory = Validation.buildDefaultValidatorFactory();
    }
  }

  private void generateObjectName() throws MalformedObjectNameException {
    String name = config.getProperty(CFG_KEY_REGISTRY_JMX_ID);
    if (!isEmpty(name)) {
      myObjectName = ObjectName.getInstance(JMX_REGISTRY_TYPE + REGISTRY_PREFIX + name);
    }
    else {
      myObjectName = ObjectName.getInstance(STANDARD_REGISTRY_JMX_NAME);
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

  @Override
  public Set<ObjectName> getAdapters() {
    return new HashSet<ObjectName>(registeredAdapters);
  }

  @Override
  public URL getConfigurationURL(ObjectName adapterName) {
    return configurationURLs.get(adapterName);
  }

  @Override
  public boolean removeConfigurationURL(ObjectName adapterName) {
    URL removed = null;
    synchronized (configurationURLs) {
      removed = configurationURLs.remove(adapterName);
    }
    return removed != null;
  }

  @Override
  public void putConfigurationURL(ObjectName adapterName, URL configUrl) {
    if (configUrl != null) {
      synchronized (configurationURLs) {
        configurationURLs.put(adapterName, configUrl);
      }
    }
  }

  // For testing so we don't really care
  void setConfigurationPreProcessorLoader(ConfigurationPreProcessorLoader cppl) {
    this.configurationPreProcessorLoader = cppl;
  }

  @Override
  public void persistAdapter(AdapterManagerMBean adapter, URL url) throws CoreException, IOException {
    assertNotNull(adapter, EXCEPTION_MSG_MBEAN_NULL);
    persist(adapter.getConfiguration(), url);
  }

  @Override
  public void persistAdapter(ObjectName adapter, URL url) throws CoreException, IOException {
    persistAdapter(JMX.newMBeanProxy(mBeanServer, adapter, AdapterManagerMBean.class), url);
  }

  @Override
  public void persist(String data, URL url) throws CoreException, IOException {
    assertNotNull(url, EXCEPTION_MSG_URL_NULL);
    OutputStream out = null;
    try {
      if ("file".equalsIgnoreCase(url.getProtocol())) {
        out = new FileOutputStream(FsHelper.createFileReference(url));
        persist(data, out);
      }
      else {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setDoOutput(true);
        out = urlConnection.getOutputStream();
        persist(data, out);
      }
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }

  private void persist(String data, OutputStream out) throws IOException {
    try (PrintStream ps = new PrintStream(out)) {
      ps.println(data);
    }
  }

  @Override
  public ObjectName createAdapter(URL url) throws IOException, MalformedObjectNameException, CoreException {
    assertNotNull(url, EXCEPTION_MSG_URL_NULL);
    String xml = this.loadPreProcessors().process(url);
    return register((Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(xml), url);
  }

  @Override
  public ObjectName createAdapter(String xml) throws IOException, MalformedObjectNameException, CoreException {
    assertNotNull(xml, EXCEPTION_MSG_XML_NULL);
    xml = this.loadPreProcessors().process(xml);
    return register((Adapter) DefaultMarshaller.getDefaultMarshaller().unmarshal(xml), null);
  }

  @Override
  public void addAdapter(AdapterManagerMBean adapterManager) throws MalformedObjectNameException, CoreException {
    assertNotNull(adapterManager, EXCEPTION_MSG_MBEAN_NULL);
    addRegisteredAdapter(adapterManager.createObjectName());
  }

  private void addRegisteredAdapter(ObjectName adapterName) throws CoreException {
    synchronized (registeredAdapters) {
      if (registeredAdapters.contains(adapterName)) {
        throw new CoreException("[" + adapterName + "] already exists in the registry, remove it first");
      }
      registeredAdapters.remove(adapterName);
      registeredAdapters.add(adapterName);
    }
  }

  private void removeRegisteredAdapter(ObjectName adapterName) throws CoreException {
    synchronized (registeredAdapters) {
      registeredAdapters.remove(adapterName);
    }
  }

  private ObjectName register(Adapter adapter, URL configUrl) throws CoreException, MalformedObjectNameException {
    Adapter adapterToRegister = validate(adapter);
    AdapterManager manager = new AdapterManager(adapterToRegister);
    ObjectName adapterName = manager.createObjectName();
    addRegisteredAdapter(adapterName);
    manager.registerMBean();
    putConfigurationURL(adapterName, configUrl);
    return adapterName;
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
    } finally {
      IOUtils.closeQuietly(writer);
    }
    throw new CoreException(writer.toString());
  }
  
  @Override
  public void destroyAdapter(AdapterManagerMBean adapter) throws CoreException, MalformedObjectNameException {
    assertNotNull(adapter, EXCEPTION_MSG_MBEAN_NULL);
    ObjectName name = adapter.createObjectName();
    adapter.requestClose();
    adapter.unregisterMBean();
    removeRegisteredAdapter(name);
  }

  @Override
  public void destroyAdapter(ObjectName adapterName) throws MalformedObjectNameException, CoreException {
    destroyAdapter(JMX.newMBeanProxy(mBeanServer, adapterName, AdapterManagerMBean.class));
  }

  /**
   * Convenience method for sending a shutdown event for every adapter.
   * 
   * @param adapterManagers set of managers.
   */
  public static void sendShutdownEvent(Set<ObjectName> adapterManagers) {
    for (ObjectName obj : adapterManagers) {
      AdapterManagerMBean mgr = JMX.newMBeanProxy(JmxHelper.findMBeanServer(), obj, AdapterManagerMBean.class);
      try {
        mgr.sendLifecycleEvent(EventFactory.create(AdapterShutdownEvent.class));
      }
      catch (CoreException e) {
        log.warn("Failed to send ShutdownEvent for " + mgr.getUniqueId());
      }
    }
  }

  /**
   * Convenience method to start a set of adapter managers that uses {@link AdapterManagerMBean#requestStart()}
   *
   * @param adapterManagers set of managers.
   */
  public static void start(Set<ObjectName> adapterManagers) throws CoreException {
    for (ObjectName obj : adapterManagers) {
      AdapterManagerMBean mgr = JMX.newMBeanProxy(JmxHelper.findMBeanServer(), obj, AdapterManagerMBean.class);
      mgr.requestStart();
    }
  }

  /**
   * Convenience method to close a set of adapter managers that uses
   * {@link AdapterManagerMBean#requestClose()}
   *
   * @param adapterManagers set of managers.
   * @throws CoreException wrapping other exceptions
   */
  public static void close(Set<ObjectName> adapterManagers) throws CoreException {
    for (ObjectName obj : adapterManagers) {
      AdapterManagerMBean mgr = JMX.newMBeanProxy(JmxHelper.findMBeanServer(), obj, AdapterManagerMBean.class);
      mgr.requestClose();
    }
  }

  /**
   * Convenience method to close a set of adapter managers that uses
   * {@link AdapterManagerMBean#requestStop()}
   *
   * @param adapterManagers set of managers.
   * @throws CoreException wrapping other exceptions
   */
  public static void stop(Set<ObjectName> adapterManagers) throws CoreException {
    for (ObjectName obj : adapterManagers) {
      AdapterManagerMBean mgr = JMX.newMBeanProxy(JmxHelper.findMBeanServer(), obj, AdapterManagerMBean.class);
      mgr.requestStop();
    }
  }

  /**
   * Convenience method to close a set of adapter managers that uses
   * {@link AdapterManagerMBean#unregisterMBean()}
   *
   * @param adapterManagers set of managers.
   * @throws CoreException wrapping other exceptions
   */
  public static void unregister(Set<ObjectName> adapterManagers) throws CoreException {
    for (ObjectName obj : adapterManagers) {
      AdapterManagerMBean mgr = JMX.newMBeanProxy(JmxHelper.findMBeanServer(), obj, AdapterManagerMBean.class);
      mgr.unregisterMBean();
    }
  }

  private PreProcessorsList loadPreProcessors() throws CoreException {
    return configurationPreProcessorLoader.load(config);
  }

  private static void assertNotNull(Object o, String msg) throws CoreException {
    if (o == null) {
      throw new CoreException(msg);
    }
  }

  @Override
  public Properties getConfiguration() {
    return new Properties(config);
  }

  @Override
  public String getVersionControl() {
    return runtimeVCS != null ? runtimeVCS.getImplementationName() : null;
  }

  @Override
  public void reloadFromVersionControl() throws MalformedObjectNameException, CoreException, MalformedURLException, IOException,
      LicenseException {
    assertNotNull(runtimeVCS, EXCEPTION_MSG_NO_VCR);
    // first of all destroy all adapters.
    for (ObjectName o : getAdapters()) {
      destroyAdapter(o);
    }
    // Next update runtimeVCS
    runtimeVCS.update();
    // Reconfigure Logging; likely to not be required because we create and watch...
    // config.reconfigureLogging();

    createAdapter(new URLString(config.findAdapterResource()).getURL());
  }

  // For testing.
  void overrideRuntimeVCS(RuntimeVersionControl newVcs) {
    runtimeVCS = newVcs;
  }

  @Override
  public void validateConfig(String config) throws CoreException {
    try {
      assertNotNull(config, EXCEPTION_MSG_XML_NULL);
      String xml = this.loadPreProcessors().process(config);
      DefaultMarshaller.getDefaultMarshaller().unmarshal(xml);
    } catch (CoreException e) {
      // We do this so that we don't have nested causes as it's possible that
      // some exceptions may not be serializable for the UI.
      throw new CoreException(e.getMessage());
    }
  }

}
