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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.adaptris.core.CoreException;
import com.adaptris.core.management.ManagementComponentInfo;
import com.adaptris.util.URLString;

/**
 * A registry of Adapters that are available for management.
 *
 * @author lchan
 *
 */
public interface AdapterRegistryMBean extends BaseComponentMBean {

  /**
   * The type for the adapter registry.
   * 
   */
  String JMX_REGISTRY_TYPE = AdapterComponentMBean.JMX_DOMAIN_NAME + ":type=Registry";

  String REGISTRY_PREFIX = AdapterComponentMBean.PROPERTY_SEPARATOR + AdapterComponentMBean.KEY_ID + AdapterComponentMBean.EQUALS;

  /**
   * The Standard Name for the Adapter Registry {@value}
   * 
   */
  String STANDARD_REGISTRY_JMX_NAME = JMX_REGISTRY_TYPE + REGISTRY_PREFIX + "AdapterRegistry";


  /**
   * Get all the adapters that have been registered.
   *
   * @return set of ObjectNames that can be looked up in JMX.
   */
  public Set<ObjectName> getAdapters();

  // Effectively does something like...
  // OutputStream out = url.openConnection().getOutputStream();
  // out.println(adapter.getConfiguration());
  /**
   * Persist the adapter to the given URL.
   * 
   * @param adapter the adapter Manager.
   * @param url the url.
   * @throws IOException
   * @throws CoreException
   */
  void persistAdapter(AdapterManagerMBean adapter, URLString url) throws IOException, CoreException;

  /**
   * Persist the adapter to the given URL.
   * 
   * @param adapter the adapter Manager.
   * @param url the url.
   * @throws IOException
   * @throws CoreException
   */
  void persistAdapter(AdapterManagerMBean adapter, String url) throws IOException, CoreException;

  /**
   * Persist the adapter to the given URL.
   * 
   * @param adapter the {@link AdapterManagerMBean} ObjectName.
   * @param url the url.
   * @throws IOException
   * @throws CoreException
   */
  void persistAdapter(ObjectName adapter, URLString url) throws IOException, CoreException;

  /**
   * Persist the adapter to the given URL.
   * 
   * @param adapter the {@link AdapterManagerMBean} ObjectName.
   * @param url the url.
   * @throws IOException
   * @throws CoreException
   */
  void persistAdapter(ObjectName adapter, String url) throws IOException, CoreException;

  /**
   * Persist some data the given URL.
   * 
   * @param data the data.
   * @param url the url.
   * @throws IOException
   * @throws CoreException
   * @since 3.0.3
   */
  void persist(String data, URLString url) throws IOException, CoreException;

  // Create an Adapter object, and an associated AdapterManagerMBean from the given URL.
  // Register the MBean and return its object name.
  /**
   * Create an adapter from the given URL.
   * <p>
   * Create an adapter from the given URL; the license associated with this adapter is one that was derived from
   * <code>bootstrap.properties</code>.
   * </p>
   *
   * @param url the url
   * @return the object name representing for the associated {@link AdapterManagerMBean}
   * @throws IOException an error accessing the URL.
   * @throws CoreException an error creating the underlying adapter.
   * @throws MalformedObjectNameException if there was a problem
   * @deprecated since 3.6.1 get it via {@link #getBuilder(Properties)} instead.
   */
  @Deprecated
  ObjectName createAdapter(URLString url) throws IOException, MalformedObjectNameException, CoreException;

  /**
   * Create an adapter from the given URL.
   * <p>
   * Create an adapter from the given URL; the license associated with this adapter is one that was derived from
   * <code>bootstrap.properties</code>.
   * </p>
   * 
   * @param url the url
   * @return the object name representing for the associated {@link AdapterManagerMBean}
   * @throws IOException an error accessing the URL.
   * @throws CoreException an error creating the underlying adapter.
   * @throws MalformedObjectNameException if there was a problem
   * @deprecated since 3.6.1
   */
  @Deprecated
  ObjectName createAdapterFromUrl(String url) throws IOException, MalformedObjectNameException, CoreException;

  /**
   * Get the URL that was used to create the adapter associated with that ObjectName.
   * <p>
   * When attempting to persist an adapter instance, you can use this method to query for the URL
   * that was used to create the associated adapter. This will be the URL used to create the adapter
   * initially (i.e. the {@code adapterConfigURL} that was resolved from
   * {@code bootstrap.properties}) unless the adapter was created with
   * {@linkplain #createAdapter(String xml)} in the first instance (in which case, you need to
   * figure out where you want to persist the adapter in a different way).
   * </p>
   * <p>
   * Internally, the URLs are keyed against the ObjectName, which means that if create adapters that
   * have the same logical object name, then a URLString will be returned regardless of "how" you
   * created the adapter. So the code below will return {@code http://my/url} if the two ObjectName
   * instances are considered the same.
   * 
   * <pre>
   * {
   *   &#064;code
   *   ObjectName on = createAdapter(new URLString(&quot;http://my/url&quot;));
   *   ObjectName on2 = createAdapter(&quot;&lt;adapter&gt;...&lt;/adapter&gt;&quot;);
   *   URLString url = getConfigurationURL(on2);
   * }
   * </pre>
   * 
   * @param adapterName object name representation for the associated {@link AdapterManagerMBean}
   * @return the URL that was used to create the object, or null, if
   *         {@linkplain #createAdapter(String xml)} was used to create a new adapter.
   * @since 3.0.2
   */
  URLString getConfigurationURL(ObjectName adapterName);

  /**
   * Get the URL that was used to create the adapter associated with that ObjectName.
   * <p>
   * When attempting to persist an adapter instance, you can use this method to query for the URL that was used to create the
   * associated adapter. This will be the URL used to create the adapter initially (i.e. the {@code adapterConfigURL} that was
   * resolved from {@code bootstrap.properties}) unless the adapter was created with {@linkplain #createAdapter(String xml)} in the
   * first instance (in which case, you need to figure out where you want to persist the adapter in a different way).
   * </p>
   * <p>
   * Internally, the URLs are keyed against the ObjectName, which means that if create adapters that have the same logical object
   * name, then a URL will be returned regardless of "how" you created the adapter. So the code below will return
   * {@code http://my/url} if the two ObjectName instances are considered the same.
   * 
   * <pre>
   * {@code
   * ObjectName on = createAdapter(new URL("http://my/url"));
   * ObjectName on2 = createAdapter("<adapter>...</adapter>");
   * URL url = getConfigurationURL(on2);
   * }
   * </pre>
   * 
   * @param adapterName object name representation for the associated {@link AdapterManagerMBean}
   * @return the URL that was used to create the object, or null, if {@linkplain #createAdapter(String xml)} was used to create a
   * new adapter.
   * @since 3.0.2
   */
  String getConfigurationURLString(ObjectName adapterName);

  /**
   * Remove the URL that was used to create the adapter associated with the ObjectName.
   * <p>
   * Note that calling either {@linkplain #destroyAdapter(AdapterManagerMBean)} or {@linkplain #destroyAdapter(ObjectName)} will
   * <strong>NOT</strong remove the URL from the internal map (as you are likely to invoke those methods to stop/close the adapter
   * prior to creating a new one), so this method is available for you to manually clean up if desired.
   * </p>
   * 
   * @param adapterName object name representation for the associated {@link AdapterManagerMBean}
   * @return true if the URL was removed.
   * @since 3.0.3
   */
  boolean removeConfigurationURL(ObjectName adapterName);

  /**
   * Register a URL against a given ObjectName.
   * <p>
   * Where you have used {@link #createAdapter(String)}; there will not be an URL associated with the newly created adapter. Use
   * this to register a URL against the given ObjectName, which means that {@linkplain #getConfigurationURL(ObjectName)} will not
   * return a null object.
   * </p>
   * 
   * @param adapterName object name representation for the associated {@link AdapterManagerMBean}
   * @param url the URL to be stored against the objectname
   */
  void putConfigurationURL(ObjectName adapterName, URLString url);

  /**
   * Register a URL against a given ObjectName.
   * <p>
   * Where you have used {@link #createAdapter(String)}; there will not be an URL associated with the newly created adapter. Use
   * this to register a URL against the given ObjectName, which means that {@linkplain #getConfigurationURL(ObjectName)} will not
   * return a null object.
   * </p>
   * 
   * @param adapterName object name representation for the associated {@link AdapterManagerMBean}
   * @param url the URL to be stored against the objectname
   */
  void putConfigurationURL(ObjectName adapterName, String url) throws IOException;

  // Create an Adapter object, and an associated AdapterManagerMBean from the given XML.
  // Register the MBean and return its object name.
  /**
   * Create an adapter from the given string representation.
   * <p>
   * Create an adapter from the given string; the license associated with this adapter is one that was derived from
   * <code>bootstrap.properties</code>.
   * </p>
   *
   * @param xml the string representation (generally XML).
   * @return the object name representing for the associated {@link AdapterManagerMBean}
   * @throws IOException an error accessing the URL.
   * @throws CoreException an error creating the underlying adapter.
   * @throws MalformedObjectNameException if there was a problem with the objectname.
   * @deprecated since 3.6.1 get it via {@link #getBuilder(Properties)} instead.
   * 
   */
  @Deprecated
  ObjectName createAdapter(String xml) throws IOException, MalformedObjectNameException, CoreException;

  /**
   * Add an {@link AdapterManagerMBean} instance to the registry.
   * 
   * @param adapter the {@link AdapterManagerMBean} instance.
   * @throws MalformedObjectNameException if there was a problem with the objectname
   * @throws CoreException if the objectname already exists in the registry.
   */
  void addAdapter(AdapterManagerMBean adapter) throws MalformedObjectNameException, CoreException;

  /**
   * Close the underlying adapter and unregister from JMX.
   * <p>
   * Note that this uses {@link AdapterManagerMBean#requestClose()} which will block until the adapter is closed; if a timeout is
   * required, then make sure to use {@link AdapterManagerMBean#requestClose(long)} and possibly
   * {@link AdapterManagerMBean#forceClose()} before invoking this method.
   * </p>
   * 
   * @param adapter the {@link AdapterManagerMBean}.
   * @throws CoreException an error accessing the underlying adapter.
   * @throws MalformedObjectNameException if there was a problem with the objectname.
   */
  void destroyAdapter(AdapterManagerMBean adapter) throws CoreException, MalformedObjectNameException;

  /**
   * Close the underlying adapter and unregister from JMX.
   * 
   * <p>
   * Note that this uses {@link AdapterManagerMBean#requestClose()} which will block until the adapter is closed; if a timeout is
   * required, then make sure to use {@link AdapterManagerMBean#requestClose(long)} and possibly
   * {@link AdapterManagerMBean#forceClose()} before invoking this method.
   * </p>
   * 
   * @param adapter the {@link AdapterManagerMBean}.
   * @see #destroyAdapter(AdapterManagerMBean)
   * @throws MalformedObjectNameException if there was a problem with the objectname.
   * @throws CoreException an error accessing the underlying adapter.
   */
  void destroyAdapter(ObjectName adapter) throws MalformedObjectNameException, CoreException;

  /**
   * Return the {@link com.adaptris.core.management.BootstrapProperties} instance that was used to create this registry.
   * 
   * @return a set of properties.
   * @since 3.0.3
   * @deprecated since 3.6.1 get it via {@link #getBuilder(ObjectName)} instead.
   */
  @Deprecated
  Properties getConfiguration();

  /**
   * Return the version control system that is currently in use based on
   * {@linkplain com.adaptris.core.management.vcs.RuntimeVersionControl#getImplementationName()}
   * 
   * @return the version control system that is currently configured for adapter runtime (null if none is available).
   * @since 3.0.3
   */
  String getVersionControl();

  /**
   * Recreate all adapters associated with this AdapterRegistry from version control.
   * <p>
   * Note that the adapters will not be started as part of this process; this needs to be done separately.
   * </p>
   * 
   * @throws MalformedObjectNameException if there was a problem with any objectname.
   * @throws CoreException an error creating the underlying adapter(s).
   * @throws IOException if there was a with reading the URL where the adapter is hosted.
   * @throws MalformedURLException if there was a problem with the URL found from config.
   * @since 3.0.3
   */
  void reloadFromVersionControl() throws MalformedObjectNameException, CoreException, MalformedURLException, IOException;

  /**
   * Recreate all adapters associated with this AdapterRegistry from their associated bootstrap file.
   * <p>
   * Note that the adapters will not be started as part of this process; this needs to be done separately.
   * </p>
   * 
   * @throws MalformedObjectNameException if there was a problem with any objectname.
   * @throws CoreException an error creating the underlying adapter(s).
   * @throws IOException if there was a with reading the URL where the adapter is hosted.
   * @throws MalformedURLException if there was a problem with the URL found from config.
   * @return a set of object name representing for the associated {@link AdapterManagerMBean} instances created.
   * @since 3.4.1
   */
  Set<ObjectName> reloadFromConfig() throws MalformedObjectNameException, CoreException, MalformedURLException, IOException;


  /**
   * Attempts to unmarshal the component within this target runtime unit.
   * <p>
   * This will verify that the component that was configured can be unmarshalled within the target runtime system. It is largely
   * similar to the {@link #createAdapter(String)} method, however it does not try to register a new {@link AdapterManagerMBean} as
   * part of the operation.
   * </p>
   * 
   * @param config the String representation of a component.
   * @throws CoreException if there was an exception unmarshalling.
   * @since 3.0.5
   * @see #createAdapter(String)
   */
  void validateConfig(String config) throws CoreException;

  /**
   * In JSON format return the full class description, including subclasses and annotation detail.
   * 
   * @param className - fully qualified
   * @return JSON class definition.
   * @throws CoreException
   * @since 3.6.0
   */
  String getClassDefinition(String className) throws CoreException;

  /**
   * Recreate the adapters associated with the specified objectname from their associated bootstrap file.
   * <p>
   * Note that the adapter will not be started as part of this process; this needs to be done separately.
   * </p>
   * 
   * @param obj the ObjectName to reload from config.
   * @throws MalformedObjectNameException if there was a problem with any objectname.
   * @throws CoreException an error creating the underlying adapter(s).
   * @throws IOException if there was a with reading the URL where the adapter is hosted.
   * @throws MalformedURLException if there was a problem with the URL found from config.
   * @return The ObjectName representing for the associated {@link AdapterManagerMBean} instances created.
   * @since 3.6.1
   */
  ObjectName reloadFromConfig(ObjectName obj)
      throws MalformedObjectNameException, CoreException, IOException, MalformedURLException;

  /**
   * Recreate the adapter associated with the specified objectname from their associated bootstrap file.
   * <p>
   * Note that the adapter will not be started as part of this process; this needs to be done separately.
   * </p>
   * 
   * @param obj the ObjectName to restart.
   * @throws MalformedObjectNameException if there was a problem with any objectname.
   * @throws CoreException an error creating the underlying adapter(s).
   * @throws IOException if there was a with reading the URL where the adapter is hosted.
   * @throws MalformedURLException if there was a problem with the URL found from config.
   * @return The ObjectName representing for the associated {@link AdapterManagerMBean} instances created.
   * @since 3.6.1
   */
  ObjectName reloadFromVersionControl(ObjectName obj)
      throws MalformedObjectNameException, CoreException, IOException, MalformedURLException;

  void addConfiguration(Properties p) throws MalformedObjectNameException, CoreException;

  /**
   * Get a reference to the {@link AdapterBuilderMBean} instance that built the corresponding {@link AdapterManagerMBean} instance.
   * 
   * @param p a reference to the {@link AdapterManagerMBean}
   * @return a reference to the {@link AdapterBuilderMBean}
   * @throws InstanceNotFoundException if there was no associated builder.
   */
  ObjectName getBuilder(ObjectName p) throws InstanceNotFoundException;

  /**
   * Get a reference to the {@link AdapterBuilderMBean} instance that built the corresponding {@link AdapterManagerMBean} instance.
   * 
   * @param p a reference to the {@link AdapterManagerMBean}
   * @return a reference to the {@link AdapterBuilderMBean}
   * @throws InstanceNotFoundException if there was no associated builder.
   */
  AdapterBuilderMBean getBuilderMBean(ObjectName p) throws InstanceNotFoundException;

  /**
   * Get the {@link AdapterBuilderMBean} associated with given {@code Properties} object.
   * 
   */
  AdapterBuilderMBean getBuilder(Properties p) throws InstanceNotFoundException;

  /**
   * Get All the builders.
   * 
   * @return all the available builders.
   */
  Set<ObjectName> getBuilders();
  
  /**
   * Will return a Map of {@link ManagementComponentInfo}'s.  The key to the map
   * is the name of the configured management component.  
   * 
   * @return Basic information on configured management components
   */
  List<ManagementComponentInfo> getManagementComponentInfo();
  
  /**
   * Set the list of configured management components.  
   */
  void setManagementComponentInfo(List<ManagementComponentInfo> manComps);

}
