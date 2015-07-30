package com.adaptris.core;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
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
  
  @NotNull
  @Valid
  @AutoPopulated
  private TimeInterval maximumWaitTime;
  
  @NotNull
  @Valid
  @AutoPopulated
  private TimeInterval intervalToCheck;
  
  public WaitingOutOfStateHandler() {
    super();
    this.setMaximumWaitTime(DEFAULT_MAX_WAIT);
    this.setIntervalToCheck(DEFAULT_INTERVAL_TO_CHECK);
  }
  
  @Override
  public void handleOutOfState(StateManagedComponent component) throws OutOfStateException {
    long start = new Date().getTime();
    
    while(!component.retrieveComponentState().equals(this.getCorrectState().getComponentState())) {
      long now = new Date().getTime();
      if((now - start) > this.getMaximumWaitTime().toMilliseconds())
        break;
      else {
        try {
          Thread.sleep(this.getIntervalToCheck().toMilliseconds());
        } catch (InterruptedException ex) {
          // do nothing
        }
      }
    }
    
    if(!component.retrieveComponentState().equals(this.getCorrectState().getComponentState()))
      throw new OutOfStateException("Expected state: " + this.getCorrectState().getClass().getSimpleName() + " but got " + component.retrieveComponentState().getClass().getSimpleName());
  }

  public TimeInterval getMaximumWaitTime() {
    return maximumWaitTime;
  }

  public void setMaximumWaitTime(TimeInterval maximumWaitTime) {
    this.maximumWaitTime = maximumWaitTime;
  }

  public TimeInterval getIntervalToCheck() {
    return intervalToCheck;
  }

  public void setIntervalToCheck(TimeInterval intervalToCheck) {
    this.intervalToCheck = intervalToCheck;
  }

}
