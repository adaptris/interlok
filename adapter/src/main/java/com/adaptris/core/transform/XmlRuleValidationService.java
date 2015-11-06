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
import java.util.List;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.transform.validate.ValidationStage;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * XML rule validation service.
 * </p>
 * 
 * @config xml-rule-validation-service
 * @license BASIC
 * @deprecated use {@link XmlValidationService} with an {@link XmlRuleValidator} instead.
 */
@XStreamAlias("xml-rule-validation-service")
@Deprecated
public class XmlRuleValidationService extends ServiceImp {

  private static transient boolean warningLogged;

  @XStreamImplicit(itemFieldName = "validation-stage")
  @NotNull
  @AutoPopulated
  private List<ValidationStage> validationStages = new ArrayList<ValidationStage>();
  private KeyValuePairSet namespaceContext;
  private transient XmlRuleValidator validator;

  public XmlRuleValidationService() {
    if (!warningLogged) {
      log.warn("{} is deprecated use {} with a {} instead", this.getClass().getSimpleName(),
        XmlValidationService.class.getCanonicalName(), XmlRuleValidator.class.getSimpleName());
      warningLogged = true;
    }
  }

  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      validator.validate(msg);
    }
    catch (ServiceException e) {
      throw e;
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {}


  @Override
  public void init() throws CoreException {
    validator = new XmlRuleValidator();
    validator.setNamespaceContext(getNamespaceContext());
    validator.setValidationStages(getValidationStages());
    validator.prepare();
    LifecycleHelper.init(validator);
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(validator);
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(validator);
  }

  public void close() {
    LifecycleHelper.close(validator);
  }

  public void addValidationStage(ValidationStage vs) {
    validationStages.add(vs);
  }

  public List<ValidationStage> getValidationStages() {
    return validationStages;
  }

  public void setValidationStages(List<ValidationStage> l) {
    validationStages = l;
  }

  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  /**
   * Set the namespace context for resolving namespaces.
   * <ul>
   * <li>The key is the namespace prefix</li>
   * <li>The value is the namespace uri</li>
   * </ul>
   * 
   * @param set the mapping for the namespace context.
   */
  public void setNamespaceContext(KeyValuePairSet set) {
    this.namespaceContext = set;
  }
}
