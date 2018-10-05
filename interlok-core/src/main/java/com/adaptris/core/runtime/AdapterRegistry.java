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
import java.beans.Transient;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.Adapter;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.EventFactory;
import com.adaptris.core.XStreamJsonMarshaller;
import com.adaptris.core.event.AdapterShutdownEvent;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.util.URLString;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

@SuppressWarnings("deprecation")
public class AdapterRegistry implements AdapterRegistryMBean {

  // Packages for fast class path we never want to scan
  // stick a - in front of it.
  private static final String[] FCS_BLACKLIST = {
      "-javax", "-java", "-org.apache", "-org.codehaus", "-org.hibernate", "-org.springframework", "-com.mchange", "-com.sun",
      "-org.bouncycastle", "-org.eclipse", "-org.jboss", "-org.slf4j", "-net.sf.saxon", "-com.google.guava", "-com.fasterxml",
      "-io.github.classgraph", "-com.jcraft", "-com.thoughtworks", "-org.quartz"
  };
  private static final String EXCEPTION_MSG_XML_NULL = "XML String is null";
  private static final String EXCEPTION_MSG_URL_NULL = "URL is null";
  private static final String EXCEPTION_MSG_MBEAN_NULL = "AdapterManagerMBean is null";
  private static final String EXCEPTION_MSG_NO_VCR = "No Runtime Version Control";

  private final Set<ObjectName> registeredAdapters = new HashSet<ObjectName>();
  private transient ObjectName myObjectName;
  private transient MBeanServer mBeanServer;
  private static transient Logger log = LoggerFactory.getLogger(AdapterRegistry.class);
  private transient Map<ObjectName, URLString> configurationURLs = new HashMap<>();
  private transient Map<ObjectName, AdapterBuilder> builderByObjectName = new HashMap<>();
  private transient Map<Properties, AdapterBuilder> builderByProps = new HashMap<>();
  private transient AdapterBuilder defaultBuilder;

  private AdapterRegistry() throws MalformedObjectNameException {
    mBeanServer = JmxHelper.findMBeanServer();
    myObjectName = ObjectName.getInstance(STANDARD_REGISTRY_JMX_NAME);
  }

  public static AdapterRegistryMBean findInstance(Properties cfg) throws MalformedObjectNameException, CoreException {
    MBeanServer mbs = JmxHelper.findMBeanServer();
    AdapterRegistryMBean result = null;
    ObjectName objName = ObjectName.getInstance(STANDARD_REGISTRY_JMX_NAME);
    if (mbs.isRegistered(objName)) {
      result = JMX.newMBeanProxy(mbs, objName, AdapterRegistryMBean.class);
      result.addConfiguration(cfg);
    }
    else {
      result = new AdapterRegistry();
      result.registerMBean();
      result.addConfiguration(cfg);
    }
    return result;
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
      for (AdapterBuilder builder : builderByProps.values()) {
        builder.unregisterMBean();
      }
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
  public URLString getConfigurationURL(ObjectName adapterName) {
    return configurationURLs.get(adapterName);
  }

  @Override
  public String getConfigurationURLString(ObjectName adapterName) {
    URLString url = getConfigurationURL(adapterName);
    if (url != null) {
      return url.toString();
    }
    return null;
  }

  @Override
  public boolean removeConfigurationURL(ObjectName adapterName) {
    URLString removed = null;
    synchronized (configurationURLs) {
      removed = configurationURLs.remove(adapterName);
    }
    return removed != null;
  }

  @Override
  public void putConfigurationURL(ObjectName adapterName, URLString configUrl) {
    if (configUrl != null) {
      synchronized (configurationURLs) {
        configurationURLs.put(adapterName, configUrl);
      }
    }
  }

  @Override
  public void putConfigurationURL(ObjectName adapterName, String configUrl) throws IOException {
    putConfigurationURL(adapterName, new URLString(configUrl));
  }

  @Override
  public void persistAdapter(AdapterManagerMBean adapter, URLString url) throws CoreException, IOException {
    assertNotNull(adapter, EXCEPTION_MSG_MBEAN_NULL);
    persist(adapter.getConfiguration(), url);
  }

  @Override
  public void persistAdapter(AdapterManagerMBean adapter, String url) throws CoreException, IOException {
    assertNotNull(url, EXCEPTION_MSG_URL_NULL);
    persistAdapter(adapter, new URLString(url));
  }

  @Override
  public void persistAdapter(ObjectName adapter, URLString url) throws CoreException, IOException {
    persistAdapter(JMX.newMBeanProxy(mBeanServer, adapter, AdapterManagerMBean.class), url);
  }

  @Override
  public void persistAdapter(ObjectName adapter, String url) throws CoreException, IOException {
    assertNotNull(url, EXCEPTION_MSG_URL_NULL);
    persistAdapter(adapter, new URLString(url));
  }

  @Override
  public void persist(String data, URLString configUrl) throws CoreException, IOException {
    assertNotNull(configUrl, EXCEPTION_MSG_URL_NULL);
    URL url = configUrl.getURL();
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
  public ObjectName createAdapter(URLString url) throws IOException, MalformedObjectNameException, CoreException {
    assertNotNull(url, EXCEPTION_MSG_URL_NULL);
    return defaultBuilder.createAdapter(url);
  }

  @Override
  public ObjectName createAdapterFromUrl(String url) throws IOException, MalformedObjectNameException, CoreException {
    assertNotNull(url, EXCEPTION_MSG_URL_NULL);
    return createAdapter(new URLString(url));
  }

  @Override
  public ObjectName createAdapter(String xml) throws IOException, MalformedObjectNameException, CoreException {
    assertNotNull(xml, EXCEPTION_MSG_XML_NULL);
    return defaultBuilder.createAdapter(xml);
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

  ObjectName register(AdapterBuilder builder, Adapter adapter, URLString configUrl)
      throws CoreException, MalformedObjectNameException {
    AdapterManager manager = new AdapterManager(adapter);
    ObjectName adapterName = manager.createObjectName();
    addRegisteredAdapter(adapterName);
    manager.registerMBean();
    putConfigurationURL(adapterName, configUrl);
    synchronized (builderByObjectName) {
      builderByObjectName.put(adapterName, builder);
    }
    return adapterName;
  }



  @Override
  public void destroyAdapter(AdapterManagerMBean adapter) throws CoreException, MalformedObjectNameException {
    assertNotNull(adapter, EXCEPTION_MSG_MBEAN_NULL);
    ObjectName name = adapter.createObjectName();
    adapter.requestClose();
    adapter.unregisterMBean();
    removeRegisteredAdapter(name);
    synchronized (builderByObjectName) {
      builderByObjectName.remove(name);
    }
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

  private static void assertNotNull(Object o, String msg) throws CoreException {
    if (o == null) {
      throw new CoreException(msg);
    }
  }

  @Override
  public Properties getConfiguration() {
    return defaultBuilder.getConfiguration();
  }

  @Override
  public String getVersionControl() {
    String name = null;
    for (AdapterBuilder builder : builderByProps.values()) {
      name = builder.getVersionControl();
      if (name != null) break;
    }
    return name;
  }

  @Override
  public void reloadFromVersionControl()
      throws MalformedObjectNameException, CoreException, MalformedURLException, IOException {

    assertNotNull(getVersionControl(), EXCEPTION_MSG_NO_VCR);
    for (ObjectName o : getAdapters()) {
      destroyAdapter(o);
    }
    for (AdapterBuilder builder : builderByProps.values()) {
      builder.updateVCS();
      builder.createAdapter();
    }
  }

  @Override
  public Set<ObjectName> reloadFromConfig() throws MalformedObjectNameException, CoreException, MalformedURLException, IOException {
    for (ObjectName o : getAdapters()) {
      destroyAdapter(o);
    }
    Set<ObjectName> result = new HashSet<>();
    for (AdapterBuilder builder : builderByProps.values()) {
      result.add(builder.createAdapter());
    }
    return result;
  }

  @Override
  public ObjectName reloadFromConfig(ObjectName obj) throws MalformedObjectNameException, CoreException, IOException {
    return reload(obj, false);
  }

  @Override
  public ObjectName reloadFromVersionControl(ObjectName obj) throws MalformedObjectNameException, CoreException, IOException {
    return reload(obj, true);
  }

  private ObjectName reload(ObjectName obj, boolean vcsUpdate) throws MalformedObjectNameException, CoreException, IOException {
    destroyAdapter(obj);
    ObjectName newAdapter = null;
    synchronized (builderByObjectName) {
      AdapterBuilder builder = builderByObjectName.containsKey(obj) ? builderByObjectName.get(obj) : defaultBuilder;
      if (vcsUpdate) builder.updateVCS();
      newAdapter = builder.createAdapter();
      builderByObjectName.put(newAdapter, builder);
    }
    return newAdapter;
  }

  @Override
  public void addConfiguration(Properties cfg) throws MalformedObjectNameException, CoreException {
    synchronized (builderByProps) {
      AdapterBuilder builder = builderByProps.containsKey(cfg) ? builderByProps.get(cfg) : new AdapterBuilder(this, cfg);
      if (defaultBuilder == null) {
        defaultBuilder = builder;
      }
      if (!mBeanServer.isRegistered(builder.createObjectName())) {
        builder.registerMBean();
      }
      builderByProps.put(cfg, builder);
    }
  }

  @Override
  public void validateConfig(String config) throws CoreException {
    try {
      assertNotNull(config, EXCEPTION_MSG_XML_NULL);
      String xml = defaultBuilder.loadPreProcessors().process(config);
      DefaultMarshaller.getDefaultMarshaller().unmarshal(xml);
    } catch (CoreException e) {
      // We do this so that we don't have nested causes as it's possible that
      // some exceptions may not be serializable for the UI.
      throw new CoreException(e.getMessage());
    }
  }

  @Override
  public String getClassDefinition(String className) throws CoreException {
    final ClassDescriptor classDescriptor = new ClassDescriptor(className);
    try {
      Class<?> clazz = Class.forName(className);
      
      classDescriptor.setClassType(ClassDescriptor.ClassType.getTypeForClass(clazz).name().toLowerCase());
      
      List<String> displayOrder = new ArrayList<>();
      for(Annotation annotation : clazz.getAnnotations()) {
        if(XStreamAlias.class.isAssignableFrom(annotation.annotationType())) {
          classDescriptor.setAlias(((XStreamAlias) annotation).value());
        } else if(ComponentProfile.class.isAssignableFrom(annotation.annotationType())) {
          classDescriptor.setTags(((ComponentProfile) annotation).tag());
          classDescriptor.setSummary(((ComponentProfile) annotation).summary());
        } else if(DisplayOrder.class.isAssignableFrom(annotation.annotationType())) {
          displayOrder = Arrays.asList(((DisplayOrder) annotation).order());
        }
      }
      
      for(Field field : clazz.getDeclaredFields()) {
        if((!Modifier.isStatic(field.getModifiers())) && (field.getDeclaredAnnotation(Transient.class) == null)) { // if we're not transient
          ClassDescriptorProperty fieldProperty = new ClassDescriptorProperty();
          fieldProperty.setOrder(displayOrder.contains(field.getName()) ? displayOrder.indexOf(field.getName()) + 1 : 999);
          fieldProperty.setAdvanced(false);
          fieldProperty.setClassName(field.getType().getName());
          fieldProperty.setType(field.getType().getSimpleName());
          fieldProperty.setName(field.getName());
          fieldProperty.setAutoPopulated(field.getDeclaredAnnotation(AutoPopulated.class) != null);
          fieldProperty.setNullAllowed(field.getDeclaredAnnotation(NotNull.class) != null);
          
          for(Annotation annotation : field.getDeclaredAnnotations()) {
            if(AdvancedConfig.class.isAssignableFrom(annotation.annotationType())) {
              fieldProperty.setAdvanced(true);
            } else if(InputFieldDefault.class.isAssignableFrom(annotation.annotationType())) {
              fieldProperty.setDefaultValue(((InputFieldDefault) annotation).value());
            }
          }
          classDescriptor.getClassDescriptorProperties().add(fieldProperty);
        }
      }

      ScanResult result = new ClassGraph()
        .enableAllInfo()
        .blacklistPackages(FCS_BLACKLIST)
        .scan();

      List<String> subclassNames = result.getSubclasses(className).getNames();

      for (String subclassName : subclassNames) {
          classDescriptor.getSubTypes().add(subclassName);
      }

    } catch (ClassNotFoundException e) {
      throw new CoreException(e);
    }
    return new XStreamJsonMarshaller().marshal(classDescriptor);
  }

  @Override
  public Set<ObjectName> getBuilders() {
    Set<ObjectName> result = new HashSet<>();
    for (AdapterBuilder b : builderByProps.values()) {
      result.add(b.createObjectName());
    }
    return result;
  }

  @Override
  public ObjectName getBuilder(ObjectName p) throws InstanceNotFoundException {
    AdapterBuilder b = builderByObjectName.get(p);
    if (b == null) {
      throw new InstanceNotFoundException("No Builder for " + p);
    }
    return b.createObjectName();
  }

  @Override
  public AdapterBuilderMBean getBuilderMBean(ObjectName p) throws InstanceNotFoundException {
    AdapterBuilderMBean b = builderByObjectName.get(p);
    if (b == null) {
      throw new InstanceNotFoundException("No Builder for " + p);
    }
    return b;
  }

  @Override
  public AdapterBuilderMBean getBuilder(Properties p) throws InstanceNotFoundException {
    AdapterBuilder b = builderByProps.get(p);
    if (b == null) {
      throw new InstanceNotFoundException("No Builder for " + p);
    }
    return b;
  }


  Set<AdapterBuilder> builders() {
    return new HashSet<AdapterBuilder>(builderByProps.values());
  }
}
