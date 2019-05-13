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
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@link Service} allows you to test boolean (true or false) {@link Condition}'s, which if evaluate to "true" will run a configured set of services, otherwise run a different set of services.
 * </p>
 * <p>
 * Note, that although you must specify a service or list of services should the configured conditions pass, you do not have to configure services to run should the conditions fail.
 * </p>
 * <p>
 * Typically your {@link Condition} will test for equality, in-line expressions or whether values exist or not.  The values to test will generally come from the payload or message metadata. <br/>
 * Also note that some conditions can be nested, such that you can test that a value is equal to another AND / OR a value is equal/not to another value.
 * </p>
 * @author aaron
 *
 */
@XStreamAlias("if-then-otherwise")
@AdapterComponent
@ComponentProfile(summary = "Runs the configured service/list 'IF' the configured condition is met, otherwise will run the 'else' service/list.", tag = "service, conditional", since="3.7.3")
@DisplayOrder(order = {"condition", "then","otherwise"})
public class IfElse extends ServiceImp {

  @NotNull
  @Valid
  private Condition condition;

  @NotNull
  @Valid
  private ThenService then;

  @Valid
  private ElseService otherwise;

  public IfElse() {
    this.setThen(new ThenService());
    this.setOtherwise(new ElseService());
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      log.trace("Running logical 'IF', with condition class {}", this.condition().getClass().getSimpleName());
      if (this.condition().evaluate(msg)) {
        log.trace("Logical 'IF' evaluated to true, running service.");
        this.getThen().getService().doService(msg);
      } else {
        log.trace("Logical 'IF' evaluated to false, running 'otherwise' service.");
        this.getOtherwise().getService().doService(msg);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }

  }

  @Override
  public void prepare() throws CoreException {
    try {
      Args.notNull(condition(), "condition");
      LifecycleHelper.prepare(condition());
      LifecycleHelper.prepare(getThen());
      LifecycleHelper.prepare(getOtherwise());
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(condition());
    LifecycleHelper.init(getThen());
    LifecycleHelper.init(getOtherwise());
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(condition());
    LifecycleHelper.close(getThen());
    LifecycleHelper.close(getOtherwise());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(condition());
    LifecycleHelper.start(getThen());
    LifecycleHelper.start(getOtherwise());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(condition());
    LifecycleHelper.stop(getThen());
    LifecycleHelper.stop(getOtherwise());
  }


  public Condition getCondition() {
    return condition;
  }

  /**
   * Set the conditions to apply.
   * 
   * 
   * @param condition
   */
  public void setCondition(Condition condition) {
    this.condition = Args.notNull(condition, "condition");
  }

  protected Condition condition() {
    return Args.notNull(getCondition(), "condition");
  }

  public ThenService getThen() {
    return then;
  }

  public void setThen(ThenService thenService) {
    this.then = thenService;
  }

  public ElseService getOtherwise() {
    return otherwise;
  }

  public void setOtherwise(ElseService elseService) {
    this.otherwise = elseService;
  }


}
