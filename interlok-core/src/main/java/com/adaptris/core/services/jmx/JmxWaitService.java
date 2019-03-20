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

import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.validation.Valid;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.interceptor.InFlightWorkflowInterceptor;
import com.adaptris.core.jmx.JmxConnection;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Allows you to make a remote call on a JMX operation and wait until the result from the JMX operation is "true"
 * </p>
 * <p>
 * You can set parameters for the call using {@link ValueTranslator} implements. The result of the JMX operation is always
 * expected to be a {@link Boolean}. When used in conjunction with {@link InFlightWorkflowInterceptor} you can use this service
 * to pause processing until the workflow in question does not have any in flight messages.
 * </p>
 * 
 * @config jmx-wait-service
 * @since 3.3.0
 */
@XStreamAlias("jmx-wait-service")
@AdapterComponent
@ComponentProfile(summary = "Execute a JMX operation", tag = "service,jmx", recommended = {JmxConnection.class})
@DisplayOrder(order = {"objectName", "operationName", "operationParameters", "negate"})
public class JmxWaitService extends JmxOperationServiceImpl {
  
  private static final TimeInterval DEFAULT_INTERVAL = new TimeInterval(10L, TimeUnit.SECONDS);

  @InputFieldDefault(value = "false")
  private Boolean negate;
  
  @Valid
  @AdvancedConfig
  private TimeInterval retryInterval;

  private transient JmxOperationInvoker invoker;

  public JmxWaitService() {
    super();
    setInvoker(new JmxOperationInvoker<Boolean>());
  }

  @Override
  public void doService(AdaptrisMessage message) throws ServiceException {
    try {
      MBeanServerConnection mbeanConn = getConnection().retrieveConnection(JmxConnection.class).mbeanServerConnection();
      Object[] params = parametersToArray(message);
      String[] paramTypes = parametersToTypeArray(message);
      ObjectName objectNameInst = ObjectName.getInstance(message.resolve(getObjectName()));
      String operation = message.resolve(getOperationName());
      long sleepTime = retryInterval();
      boolean conditionReached = evaluate(getInvoker().invoke(mbeanConn, objectNameInst, operation, params, paramTypes));
      while (!conditionReached) {
        Thread.sleep(sleepTime);
        conditionReached = evaluate(getInvoker().invoke(mbeanConn, objectNameInst, operation, params, paramTypes));
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }
  
  private boolean evaluate(Boolean result) {
    if (negate()) {
      return !result;
    }
    return result;
  }

  /**
   * @return the invoker
   */
  private JmxOperationInvoker<Boolean> getInvoker() {
    return invoker;
  }

  /**
   * @param invoker the invoker to set
   */
  void setInvoker(JmxOperationInvoker<Boolean> invoker) {
    this.invoker = invoker;
  }

  /**
   * @return the retryInterval
   */
  public TimeInterval getRetryInterval() {
    return retryInterval;
  }

  /**
   * @param retryInterval the retryInterval to set
   */
  public void setRetryInterval(TimeInterval retryInterval) {
    this.retryInterval = retryInterval;
  }

  long retryInterval() {
    return TimeInterval.toMillisecondsDefaultIfNull(getRetryInterval(), DEFAULT_INTERVAL);
  }

  /**
   * @return the negate
   */
  public Boolean getNegate() {
    return negate;
  }

  /**
   * Switches the success criteria to {@code !operation} if set to true.
   * 
   * @param n the negate to set, defaults to false.
   */
  public void setNegate(Boolean n) {
    this.negate = n;
  }

  boolean negate() {
    return BooleanUtils.toBooleanDefaultIfNull(getNegate(), false);
  }
}
