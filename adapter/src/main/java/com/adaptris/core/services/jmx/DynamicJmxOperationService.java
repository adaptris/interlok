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

package com.adaptris.core.services.jmx;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.validation.Valid;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jmx.JmxConnection;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Allows you to dynamically connect to different {@link JMXServiceURL}s and execute JMX Operations.
 * <p>
 * Note that because of the dynamic nature of this service, it does not support username/password/jmx-environment that are supported
 * by {@link JmxConnection}. If it can be defined in the URL, then it should be (e.g. jmx+jms rather than jmxmp). A small
 * (configurable) cache of connections is kept for performance reasons. This is emptied whenever the service is stopped.
 * </p>
 * <p>
 * Note that parameters are not configurable in the same way, so this is really designed for calling the same {@code operation} for
 * different {@link ObjectName} instances on different {@link JMXServiceURL}s.
 * </p>
 * 
 * @since 3.6.5
 */
@XStreamAlias("jmx-dynamic-operation-service")
@AdapterComponent
@ComponentProfile(summary = "Execute a JMX operation", tag = "service,jmx", recommended = {NullConnection.class})
@DisplayOrder(order = {"jmxServiceUrl", "objectName", "operationName", "maxJmxConnectionCache", "operationParameters", "resultValueTranslator"})
public class DynamicJmxOperationService extends JmxOperationImpl {
  public static final int DEFAULT_MAX_CACHE_SIZE = 16;

  private static final TimeInterval DEFAULT_RETRY_INTERVAL = new TimeInterval(10L, TimeUnit.SECONDS);
  private static final Integer MAX_RETRIES = -1;

  @InputFieldHint(expression = true)
  private String jmxServiceUrl;
  @Valid
  private ValueTranslator resultValueTranslator;
  @AdvancedConfig
  @InputFieldDefault(value = "16")
  private Integer maxJmxConnectionCache;


  private transient JmxOperationInvoker invoker;
  private transient MemoryCache jmxConnectionCache = new MemoryCache();

  public DynamicJmxOperationService() {
    setInvoker(new JmxOperationInvoker<Object>());
    setOperationParameters(new ArrayList<ValueTranslator>());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      MBeanServerConnection mbeanConn = cachedGet(msg.resolve(getJmxServiceUrl())).mbeanServerConnection();
      Object result = getInvoker().invoke(mbeanConn, msg.resolve(getObjectName()), msg.resolve(getOperationName()),
          parametersToArray(msg),
          parametersToTypeArray(msg));
      if (getResultValueTranslator() != null) getResultValueTranslator().setValue(msg, result);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void initService() throws CoreException {
    super.initService();
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void stop() {
    super.stop();
    clearCache();
  }

  @Override
  public void start() throws CoreException {
    super.start();
    clearCache();
  }

  /**
   * @return the invoker
   */
  private JmxOperationInvoker<Object> getInvoker() {
    return invoker;
  }

  /**
   * @param invoker the invoker to set
   */
  void setInvoker(JmxOperationInvoker<Object> invoker) {
    this.invoker = invoker;
  }

  public String getJmxServiceUrl() {
    return jmxServiceUrl;
  }

  /**
   * The JMX Service URL to target.
   * 
   * @param s the jmxservice url.
   */
  public void setJmxServiceUrl(String s) {
    this.jmxServiceUrl = s;
  }

  public ValueTranslator getResultValueTranslator() {
    return resultValueTranslator;
  }

  public void setResultValueTranslator(ValueTranslator t) {
    this.resultValueTranslator = t;
  }

  /**
   * Get the max number of entries in the cache.
   *
   * @return the maximum number of entries.
   */
  public Integer getMaxJmxConnectionCache() {
    return maxJmxConnectionCache;
  }

  /**
   * Set the max number of entries in the cache.
   * <p>
   * Entries will be removed on a least recently accessed basis.
   * </p>
   * 
   * @param maxSize the maximum number of entries, default is {@value #DEFAULT_MAX_CACHE_SIZE}
   */
  public void setMaxJmxConnectionCache(Integer maxSize) {
    maxJmxConnectionCache = maxSize;
  }

  public DynamicJmxOperationService withResultValueTranslator(ValueTranslator t) {
    setResultValueTranslator(t);
    return this;
  }

  public DynamicJmxOperationService withJmxServiceUrl(String s) {
    setJmxServiceUrl(s);
    return this;
  }

  public DynamicJmxOperationService withMaxJmxConnectionCache(Integer i) {
    setMaxJmxConnectionCache(i);
    return this;
  }

  public DynamicJmxOperationService withOperationName(String s) {
    setOperationName(s);
    return this;
  }

  public DynamicJmxOperationService withObjectName(String s) {
    setObjectName(s);
    return this;
  }

  public DynamicJmxOperationService withOperationParameters(List<ValueTranslator> l) {
    setOperationParameters(l);
    return this;
  }

  int maxCache() {
    return getMaxJmxConnectionCache() != null ? getMaxJmxConnectionCache().intValue() : DEFAULT_MAX_CACHE_SIZE;
  }

  private void clearCache() {
    for (Map.Entry<String, JmxConnection> entry : jmxConnectionCache.entrySet()) {
      LifecycleHelper.stopAndClose(entry.getValue());
    }
    jmxConnectionCache.clear();
  }


  private JmxConnection cachedGet(String jmxServiceURL) throws CoreException {
    JmxConnection jmx = jmxConnectionCache.get(jmxServiceURL);
    if (jmx == null) {
      jmx = LifecycleHelper
          .initAndStart(new JmxConnection().withJmxServiceUrl(jmxServiceURL).withRetries(MAX_RETRIES, DEFAULT_RETRY_INTERVAL));
      jmxConnectionCache.put(jmxServiceURL, jmx);
    }
    return jmx;
  }

  private class MemoryCache extends LinkedHashMap<String, JmxConnection> {

    private static final long serialVersionUID = 2017090801L;

    public MemoryCache() {
      super(maxCache(), 0.75f, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, JmxConnection> eldest) {
      boolean result = size() > maxCache();
      if (result) {
        LifecycleHelper.stopAndClose(eldest.getValue());
      }
      return result;
    }
  }
}
