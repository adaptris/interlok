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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * A switch statement in configuration.
 * 
 * @config switch
 */
@XStreamAlias("switch")
@AdapterComponent
@ComponentProfile(
    summary = "A 'switch' statement in configuration; replaces some usecases for branching-service-collection",
    tag = "service, conditional", since = "3.9.0")
@DisplayOrder(order = {"case"})
public class Switch extends ServiceImp {

  @XStreamImplicit(itemFieldName = "case")
  @NotNull
  @Valid
  @AutoPopulated
  private List<Case> cases;

  public Switch() {
    setCases(new ArrayList<>());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      for (Case c : getCases()) {
        if (c.evaluate(msg)) {
          c.execute(msg);
          break;
        }
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    Args.notNull(getCases(), "cases");
    LifecycleHelper.prepare(getCases().toArray(new ComponentLifecycle[0]));
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(getCases().toArray(new ComponentLifecycle[0]));
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(getCases().toArray(new ComponentLifecycle[0]));
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(getCases().toArray(new ComponentLifecycle[0]));
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getCases().toArray(new ComponentLifecycle[0]));
  }

  public List<Case> getCases() {
    return cases;
  }

  public void setCases(List<Case> cases) {
    this.cases = Args.notNull(cases, "cases");
  }

  public Switch withCases(List<Case> cases) {
    setCases(cases);
    return this;
  }

  public Switch withCases(Case... cases) {
    return withCases(new ArrayList<>(Arrays.asList(cases)));
  }
}
