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
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This service holder is used to hold the service or list of services that will be executed by logical expressions, such as {@link IfElse} and {@link While}, should configured {@link Condition}'s pass.
 * </p>>
 * @author amcgrath
 *
 */
@XStreamAlias("then")
@AdapterComponent
@ComponentProfile(summary = "A service/list that should be executed after conditions have been met. ", tag = "service, conditional")
public class ThenService implements ComponentLifecycle, ComponentLifecycleExtension {

  @NotNull
  @Valid
  @AutoPopulated
  private Service service;

  public ThenService() {
    this.setService(new ServiceList());
  }

  @Override
  public void prepare() throws CoreException {
      service.prepare();
  }

  @Override
  public void init() throws CoreException {
      service.init();
  }

  @Override
  public void start() throws CoreException {
      service.start();
  }

  @Override
  public void stop() {
      service.stop();
  }

  @Override
  public void close() {
      service.close();
  }

  public Service getService() {
    return service;
  }

  public void setService(Service thenService) {
    this.service = Args.notNull(thenService, "service");
  }

}
