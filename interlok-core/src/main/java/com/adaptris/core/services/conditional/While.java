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

import static com.adaptris.core.CoreConstants.shouldStopProcessing;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.NumberUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This {@link Service} allows you to test boolean (true or false) {@link Condition}'s, which if
 * evaluate to "true" will run a configured set of services continuously until the configured
 * conditions do not evaluate to true.
 * </p>
 * <p>
 * You can also set a value for the maximum amount of times your services will run regardless of
 * whether your conditions continue to evaluate to true. <br/>
 * 
 * <pre>
 *  <max-loops>5</max-loops>
 * </pre>
 * 
 * The default value for the max-loops is 10. Setting this value to 0, will loop forever until your
 * configured conditions evaluate to false.
 * </p>
 * <p>
 * Typically your {@link Condition} will test for equality, in-line expressions or whether values
 * exist or not. The values to test will generally come from the payload or message metadata. <br/>
 * Also note that some conditions can be nested, such that you can test that a value is equal to
 * another AND / OR a value is equal/not to another value.
 * </p>
 * 
 * @author aaron
 * @config while
 *
 */
@XStreamAlias("while")
@AdapterComponent
@ComponentProfile(
    summary = "Runs the configured service/list repeatedly 'WHILE' the configured condition is met.",
    tag = "service,conditional,loop")
@DisplayOrder(order = {"condition", "then", "maxLoops"})
public class While extends ServiceImp {
  
  private static final int DEFAULT_MAX_LOOPS = 10;
  
  @NotNull
  @Valid
  private Condition condition;
  
  @NotNull
  @Valid
  @AutoPopulated
  private ThenService then;

  @InputFieldDefault("10")
  private Integer maxLoops;
  
  @AdvancedConfig
  @InputFieldDefault(value = "continue with no error")
  private MaxLoopBehaviour onMaxLoops;

  public While() {
    this.setMaxLoops(DEFAULT_MAX_LOOPS);
    setThen(new ThenService());
  }
  
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    int loopCount = 0;
    try {
      log.trace("Running logical test on 'WHILE', with condition class {}",
          this.getCondition().getClass().getSimpleName());
      while((!shouldStopProcessing.apply(msg)) && (this.getCondition().evaluate(msg))) {
        log.trace("Logical 'IF' evaluated to true on WHILE test, running service.");
        getThen().getService().doService(msg);
        loopCount ++;
        if (!continueLooping(loopCount, msg)) {
          break;
        }
      }
      log.trace("Logical 'WHILE' completed, exiting.");
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    
  }

  @Override
  public void prepare() throws CoreException {
    try {
      Args.notNull(getCondition(), "condition");
      LifecycleHelper.prepare(getThen());
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(getThen());
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getThen());
  }
  
  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getThen());
  }
  
  @Override
  public void stop() {
    LifecycleHelper.stop(getThen());
  }
  

  public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition condition) {
    this.condition = condition;
  }

  public ThenService getThen() {
    return then;
  }

  public void setThen(ThenService ifTrueService) {
    this.then = ifTrueService;
  }
  
  public Integer getMaxLoops() {
    return maxLoops;
  }

  /**
   * Set the maximum number of loops.
   * <p>
   * Note that you can set the max-loops to be {@code <0} to get infinite loops; in that situation
   * if your condition is never met, the service will loop indefinitely.
   * </p>
   * 
   * @param maxLoops the max loops; if not specified 10.
   */
  public void setMaxLoops(Integer maxLoops) {
    this.maxLoops = maxLoops;
  }

  public MaxLoopBehaviour getOnMaxLoops() {
    return onMaxLoops;
  }

  /**
   * Set the behaviour desired when the max-loop condition is hit.
   * 
   * @param onMaxLoops the desired behaviour; the default which is to "continue" (effectively {@link OnMaxNoOp} if not explicitly
   *        configured
   */
  public void setOnMaxLoops(MaxLoopBehaviour onMaxLoops) {
    this.onMaxLoops = onMaxLoops;
  }


  @SuppressWarnings("unchecked")
  public <T extends While> T withMaxLoops(Integer maxLoops) {
    setMaxLoops(maxLoops);
    return (T) this;
  }


  @SuppressWarnings("unchecked")
  public <T extends While> T withThen(ThenService t) {
    setThen(t);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public <T extends While> T withCondition(Condition c) {
    setCondition(c);
    return (T) this;
  }


  @SuppressWarnings("unchecked")
  public <T extends While> T withOnMaxLoops(MaxLoopBehaviour c) {
    setOnMaxLoops(c);
    return (T) this;
  }

  protected MaxLoopBehaviour onMaxLoops() {
    return ObjectUtils.defaultIfNull(getOnMaxLoops(), (e) -> {
      return;
    });
  }

  private int maxLoops() {
    return NumberUtils.toIntDefaultIfNull(getMaxLoops(), DEFAULT_MAX_LOOPS);
  }

  private boolean exceedsMax(int loopCount) {
    return BooleanUtils.and(new boolean[] {maxLoops() > 0, loopCount >= maxLoops()});
  }

  protected boolean continueLooping(int loopCount, AdaptrisMessage msg) throws Exception {
    if (exceedsMax(loopCount)) {
      log.trace("Reached maximum loops({}), triggering on-max behaviour", maxLoops());
      onMaxLoops().onMax(msg);
      return false;
    }
    return true;
  }
}
