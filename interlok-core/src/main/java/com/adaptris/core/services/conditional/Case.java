/*
 * Copyright 2019 Adaptris Ltd.
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
package com.adaptris.core.services.conditional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ComponentLifecycleExtension;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A 'case' for {@link Switch}.
 *
 */
@XStreamAlias("case")
@ComponentProfile(summary = "a case for a configured switch service", since = "3.9.0")
@DisplayOrder(order = {"condition", "service"})
public class Case implements ComponentLifecycle, ComponentLifecycleExtension {

  @NotNull
  @Valid
  private Condition condition;

  @NotNull
  @AutoPopulated
  @Valid
  private Service service;

  public Case() {
    setService(new ServiceList());
  }

  @Override
  public void prepare() throws CoreException {
    Args.notNull(getCondition(), "condition");
    Args.notNull(getService(), "service");
    LifecycleHelper.prepare(getCondition(), getService());
  }

  @Override
  public void init() throws CoreException {
    LifecycleHelper.init(getCondition(), getService());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getCondition(), getService());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getCondition(), getService());
  }

  @Override
  public void close() {
    LifecycleHelper.close(getCondition(), getService());
  }

  public boolean evaluate(AdaptrisMessage msg) throws CoreException {
    return getCondition().evaluate(msg);
  }

  public void execute(AdaptrisMessage msg) throws ServiceException {
    getService().doService(msg);
  }

  public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition condition) {
    this.condition = Args.notNull(condition, "condition");
  }

  public Case withCondition(Condition condition) {
    setCondition(condition);
    return this;
  }

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = Args.notNull(service, "service");
  }

  public Case withService(Service service) {
    setService(service);
    return this;
  }

}
