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
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.interceptor.InFlightWorkflowInterceptor;
import com.adaptris.core.jmx.JmxConnection;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
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
public class JmxWaitService extends ServiceImp {
  
  private static final TimeInterval DEFAULT_INTERVAL = new TimeInterval(10L, TimeUnit.SECONDS);

  @Valid
  @NotNull
  @AutoPopulated
  private AdaptrisConnection connection;

  @InputFieldDefault(value = "false")
  private Boolean negate;

  /**
   * The fully qualified object name string, pointing to the object containing your chosen operation.
   */
  @NotBlank
  private String objectName;
  
  /**
   * The name of the operation that belongs to the ObjectName specified by the jmx-service-url.
   */
  @NotBlank
  private String operationName;
  
  @Valid
  @NotNull
  @AutoPopulated
  private List<ValueTranslator> operationParameters;
  
  @Valid
  @AdvancedConfig
  private TimeInterval retryInterval;

  private transient JmxOperationInvoker invoker;

  public JmxWaitService() {
    this.setOperationParameters(new ArrayList<ValueTranslator>());
    setConnection(new JmxConnection());
    setInvoker(new JmxOperationInvoker<Boolean>());
  }

  @Override
  public void doService(AdaptrisMessage message) throws ServiceException {
    try {
      MBeanServerConnection mbeanConn = getConnection().retrieveConnection(JmxConnection.class).mbeanServerConnection();
      Boolean result = getInvoker().invoke(mbeanConn, getObjectName(), getOperationName(), parametersToArray(message),
          parametersToTypeArray(message));
      boolean conditionReached = evaluate(result);
      while (!conditionReached) {
        Thread.sleep(retryInterval());
        result = getInvoker().invoke(mbeanConn, getObjectName(), getOperationName(), parametersToArray(message),
            parametersToTypeArray(message));
        conditionReached = evaluate(result);
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

  private Object[] parametersToArray(AdaptrisMessage message) throws CoreException {
    Object[] returnArray = new Object[this.getOperationParameters().size()];
    for(int count = 0; count < this.getOperationParameters().size(); count ++)
      returnArray[count] = this.getOperationParameters().get(count).getValue(message);
    
    return returnArray;
  }
  
  private String[] parametersToTypeArray(AdaptrisMessage message) {
    String[] returnArray = new String[this.getOperationParameters().size()];
    for(int count = 0; count < this.getOperationParameters().size(); count ++)
      returnArray[count] = this.getOperationParameters().get(count).getType();
    
    return returnArray;
  }

  @Override
  public void prepare() throws CoreException {
    getConnection().prepare();
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(getConnection());
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getConnection());
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(getConnection());
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(getConnection());
  }


  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = Args.notNull(objectName, "objectName");
  }

  public String getOperationName() {
    return operationName;
  }

  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }

  public List<ValueTranslator> getOperationParameters() {
    return operationParameters;
  }

  public void setOperationParameters(List<ValueTranslator> parameters) {
    this.operationParameters = parameters;
  }

  /**
   * @return the connection
   */
  public AdaptrisConnection getConnection() {
    return connection;
  }

  /**
   * @param c the connection to set
   */
  public void setConnection(AdaptrisConnection c) {
    this.connection = Args.notNull(c, "connection");
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
    return getRetryInterval() != null ? getRetryInterval().toMilliseconds() : DEFAULT_INTERVAL.toMilliseconds();
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
    return getNegate() != null ? getNegate().booleanValue() : false;
  }
}
