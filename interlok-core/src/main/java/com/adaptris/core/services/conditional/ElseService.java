/*
    Copyright Adaptris

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.adaptris.core.services.conditional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ComponentLifecycleExtension;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceList;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This service holder is used to hold the service or list of services that will be executed by logical expressions, such as {@link IfElse} should configured {@link Condition}'s NOT pass.
 * </p>
 * @author amcgrath
 *
 */
@XStreamAlias("else")
@AdapterComponent
@ComponentProfile(summary = "A service/list that should be executed after conditions have NOT been met. ", tag = "service, conditional")
public class ElseService implements ComponentLifecycle, ComponentLifecycleExtension {

  @NotNull
  @Valid
  @AutoPopulated
  private Service service;

  public ElseService() {
    this.setService(new ServiceList());
  }

  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getService());
  }

  @Override
  public void init() throws CoreException {
    LifecycleHelper.init(getService());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getService());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getService());
  }

  @Override
  public void close() {
    LifecycleHelper.close(getService());
  }

  public Service getService() {
    return service;
  }

  public void setService(Service elseService) {
    this.service = Args.notNull(elseService, "service");
  }

}
