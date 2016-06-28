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

import javax.management.MBeanServerConnection;
import javax.validation.Valid;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.jmx.JmxConnection;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Allows you to make a remote call on a JMX operation.
 * </p>
 * <p>
 * You can set parameters for the call using {@link ValueTranslator}'s and also a single
 * {@link ValueTranslator} to help translate the result back into the Message.
 * </p>
 * <p>
 * If you do not wish to translate the result of the operation, simply omit the
 * "result-value-translator".
 * </p>
 * 
 * 
 * @config jmx-operation-call-service
 * @since 3.0.3
 */
@XStreamAlias("jmx-operation-call-service")
@AdapterComponent
@ComponentProfile(summary = "Execute a JMX operation", tag = "service,jmx", recommended = {JmxConnection.class})
@DisplayOrder(order = {"objectName", "operationName", "operationParameters", "resultValueTranslator"})
public class JmxOperationCallService extends JmxOperationServiceImpl {
  

  /**
   * Should you want to translate the result of the operation back into the message, configure a single {@link ValueTranslator}.
   */
  @Valid
  private ValueTranslator resultValueTranslator;

  private transient JmxOperationInvoker invoker;

  public JmxOperationCallService() {
    super();
    this.setResultValueTranslator(null);
    setInvoker(new JmxOperationInvoker<Object>());
  }

  @Override
  public void doService(AdaptrisMessage message) throws ServiceException {
    try {
      MBeanServerConnection mbeanConn = getConnection().retrieveConnection(JmxConnection.class).mbeanServerConnection();
      Object result = getInvoker().invoke(mbeanConn, getObjectName(), getOperationName(), parametersToArray(message),
          parametersToTypeArray(message));
      if(this.getResultValueTranslator() != null)
        this.getResultValueTranslator().setValue(message, result);
      
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }
  

  public ValueTranslator getResultValueTranslator() {
    return resultValueTranslator;
  }

  public void setResultValueTranslator(ValueTranslator resultValueTranslator) {
    this.resultValueTranslator = resultValueTranslator;
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

}
