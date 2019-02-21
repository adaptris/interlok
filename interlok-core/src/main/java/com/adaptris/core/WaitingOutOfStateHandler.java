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

package com.adaptris.core;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of the {@link OutOfStateHandler} will simply wait on the current Thread for the {@link StateManagedComponent}
 * to be in the correct/expected state.
 * </p>
 * <p>
 * While waiting, we will periodically check the {@link StateManagedComponent}s state. This periodic check can be configured by
 * setting the interval-to-check which is a {@link TimeInterval}.
 * </p>
 * <p>
 * We will also only wait for the {@link StateManagedComponent} to be in the right state for a maximum time, configured by setting
 * the maximum-wait-time, which is also a {@link TimeInterval}. If the maximum time should pass while waiting, we will stop waiting
 * and throw an {@link OutOfStateException}.
 * </p>
 * <p>
 * If you do not configure interval-to-check and/or maximum-wait-time, the following defaults will be applied;
 * <ul>
 * <li>maximum-wait-time = 2 minutes.</li>
 * <li>interval-to-check = 5 seconds.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Example configuration;<br/>
 * 
 * <pre>
 * {@code 
 * <waiting-out-of-state-handler>
 *    <correct-state>STARTED</correct-state>
 *    <maximum-wait-time>
 *        <unit>MINUTES</UNIT>
 *        <interval>2</interval>
 *    </maximum-wait-time>
 *    <interval-to-check>
 *        <unit>SECONDS<<unit>
 *        <interval>5</interval>
 *    </interval-to-check>
 * </waiting-out-of-state-handler>
 * }
 * </pre>
 * </p>
 * 
 * @config waiting-out-of-state-handler
 * 
 * 
 */
@XStreamAlias("waiting-out-of-state-handler")
public class WaitingOutOfStateHandler extends OutOfStateHandlerImp {
  
  private static final TimeInterval DEFAULT_MAX_WAIT = new TimeInterval(2L, TimeUnit.MINUTES);
  
  private static final TimeInterval DEFAULT_INTERVAL_TO_CHECK = new TimeInterval(5L, TimeUnit.SECONDS);

  @Valid
  @InputFieldDefault(value = "2 Minutes")
  private TimeInterval maximumWaitTime;

  @Valid
  @InputFieldDefault(value = "5 Seconds")
  private TimeInterval intervalToCheck;
  
  public WaitingOutOfStateHandler() {
    super();
  }
  
  @Override
  public void handleOutOfState(StateManagedComponent component) throws OutOfStateException {
    long start = new Date().getTime();
    
    while(!component.retrieveComponentState().equals(this.getCorrectState().getComponentState())) {
      long now = new Date().getTime();
      if (now - start > maxWaitTimeMs())
        break;
      else {
        LifecycleHelper.waitQuietly(intervalToCheckMs());
      }
    }
    if(!component.retrieveComponentState().equals(this.getCorrectState().getComponentState()))
      throw new OutOfStateException("Expected state: " + this.getCorrectState().getClass().getSimpleName() + " but got " + component.retrieveComponentState().getClass().getSimpleName());
  }

  public TimeInterval getMaximumWaitTime() {
    return maximumWaitTime;
  }

  public void setMaximumWaitTime(TimeInterval max) {
    this.maximumWaitTime = Args.notNull(max, "Max Wait Time");
  }

  public TimeInterval getIntervalToCheck() {
    return intervalToCheck;
  }

  public void setIntervalToCheck(TimeInterval interval) {
    this.intervalToCheck = Args.notNull(interval, "Check interval");
  }

  protected long intervalToCheckMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getIntervalToCheck(),
        DEFAULT_INTERVAL_TO_CHECK);
  }

  protected long maxWaitTimeMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getMaximumWaitTime(), DEFAULT_MAX_WAIT);
  }

}
