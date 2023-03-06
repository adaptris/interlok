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

package com.adaptris.core.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service which validates an input XML document.
 * 
 * @config xml-validation-service
 * 
 */
@JacksonXmlRootElement(localName = "xml-validation-service")
@XStreamAlias("xml-validation-service")
@AdapterComponent
@ComponentProfile(summary = "Validate the XML document", tag = "service,xml")
public class XmlValidationService extends ServiceImp {

  @NotNull
  @Valid
  @AutoPopulated
  private List<MessageValidator> validators;

  public XmlValidationService() {
    super();
    setValidators(new ArrayList<MessageValidator>());
  }

  public XmlValidationService(MessageValidator... validators) {
    this();
    setValidators(new ArrayList(Arrays.asList(validators)));
  }

  @Override
  protected void initService() throws CoreException {
    for (MessageValidator v : getValidators()) {
      LifecycleHelper.init(v);
    }
  }

  @Override
  protected void closeService() {
    for (MessageValidator v : getValidators()) {
      LifecycleHelper.close(v);
    }
  }

  @Override
  public void start() throws CoreException {
    super.start();
    for (MessageValidator v : getValidators()) {
      LifecycleHelper.start(v);
    }
  }

  @Override
  public void stop() {
    for (MessageValidator v : getValidators()) {
      LifecycleHelper.stop(v);
    }
    super.stop();
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      for (MessageValidator v : getValidators()) {
        v.validate(msg);
      }
    }
    catch (Exception e) {
      ExceptionHelper.rethrowServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    for (MessageValidator v : getValidators()) {
      LifecycleHelper.prepare(v);
    }
  }

  public List<MessageValidator> getValidators() {
    return validators;
  }

  public void setValidators(List<MessageValidator> validators) {
    this.validators = Args.notNull(validators, "validators");
  }
}
