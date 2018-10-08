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

package com.adaptris.core.services;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This RestartStrategy monitors the last usage of the service and if the last usage
 * passes an inactivity period, then this strategy will return true upon requiresRestart().
 * </p>
 * @author amcgrath
 *
 * @config timed-inactivity-restart-strategy
 */
@XStreamAlias("timed-inactivity-restart-strategy")
public class TimedInactivityRestartStrategy implements RestartStrategy {

  private static final TimeInterval DEFAULT_INACTIVITY_PERIOD = new TimeInterval(2L, TimeUnit.MINUTES);
  
  private transient long lastMessageProcessedTime;
  
  @NotNull
  @Valid
  @AutoPopulated
  private TimeInterval inactivityPeriod;
  
  public TimedInactivityRestartStrategy() {
    lastMessageProcessedTime = new Date().getTime();
    inactivityPeriod = DEFAULT_INACTIVITY_PERIOD;
  }
  
  @Override
  public void messageProcessed(AdaptrisMessage msg) {
    lastMessageProcessedTime = new Date().getTime();
  }

  @Override
  public boolean requiresRestart() {
    long currentTime = new Date().getTime();
    if((currentTime - lastMessageProcessedTime) > inactivityPeriod.toMilliseconds()) { // we have expired our session, lets close it all down and recreate later when we need it.
      return true;
    }
    return false;
  }

  public TimeInterval getInactivityPeriod() {
    return inactivityPeriod;
  }

  public void setInactivityPeriod(TimeInterval inactivityPeriod) {
    this.inactivityPeriod = inactivityPeriod;
  }

}
