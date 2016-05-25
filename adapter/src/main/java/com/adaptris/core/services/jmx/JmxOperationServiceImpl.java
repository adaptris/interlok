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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.jmx.JmxConnection;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;

/**
 * Base abstract implementation for all JMX services.
 * 
 * @since 3.3.0
 */
public abstract class JmxOperationServiceImpl extends ServiceImp {
  
  @Valid
  @NotNull
  @AutoPopulated
  private AdaptrisConnection connection;

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
  

  public JmxOperationServiceImpl() {
    this.setOperationParameters(new ArrayList<ValueTranslator>());
    setConnection(new JmxConnection());
  }

  protected Object[] parametersToArray(AdaptrisMessage message) throws CoreException {
    Object[] returnArray = new Object[this.getOperationParameters().size()];
    for(int count = 0; count < this.getOperationParameters().size(); count ++)
      returnArray[count] = this.getOperationParameters().get(count).getValue(message);
    
    return returnArray;
  }
  
  protected String[] parametersToTypeArray(AdaptrisMessage message) {
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

  /**
   * Set the object name to issue the operation against.
   * 
   * @param s the object name.
   */
  public void setObjectName(String s) {
    this.objectName = Args.notNull(s, "objectName");
  }

  public String getOperationName() {
    return operationName;
  }

  /**
   * Set the operation to call.
   * 
   * @param s the operation.
   */
  public void setOperationName(String s) {
    this.operationName = Args.notNull(s, "operationName");
  }

  public List<ValueTranslator> getOperationParameters() {
    return operationParameters;
  }

  /**
   * Set the list of {@link ValueTranslator} that will create the various parameters.
   * 
   * @param parameters the operation parameters.
   */
  public void setOperationParameters(List<ValueTranslator> parameters) {
    this.operationParameters = Args.notNull(parameters, "operationParameters");
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
}
