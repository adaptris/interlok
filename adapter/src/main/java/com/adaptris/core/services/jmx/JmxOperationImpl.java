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
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;

/**
 * Base abstract implementation for all JMX services.
 * 
 * @since 3.6.5
 */
public abstract class JmxOperationImpl extends ServiceImp {
  
  @NotBlank
  @InputFieldHint(expression = true)
  private String objectName;
  
  @NotBlank
  @InputFieldHint(expression = true)
  private String operationName;
  
  @Valid
  @NotNull
  @AutoPopulated
  private List<ValueTranslator> operationParameters;
  

  public JmxOperationImpl() {
    this.setOperationParameters(new ArrayList<ValueTranslator>());
  }

  protected Object[] parametersToArray(AdaptrisMessage message) throws CoreException {
    List<Object> result = new ArrayList<>();
    for (ValueTranslator t : getOperationParameters()) {
      result.add(t.getValue(message));
    }
    return result.toArray();
  }

  protected String[] parametersToTypeArray(AdaptrisMessage message) {
    List<String> result = new ArrayList<>();
    for (ValueTranslator t : getOperationParameters()) {
      result.add(t.getType());
    }
    return result.toArray(new String[0]);
  }

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notBlank(getObjectName(), "objectName");
      Args.notBlank(getOperationName(), "operationName");
      Args.notNull(getOperationParameters(), "operationParameters");
    }
    catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
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
}
