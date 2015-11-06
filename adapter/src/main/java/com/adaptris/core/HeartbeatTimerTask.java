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

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Implementation of <code>TimerTask</code> which emits <code>Adapter</code>
 * heartbeat  <code>Event</code>s.
 * </p>
 */
class HeartbeatTimerTask extends TimerTask {
  private transient Logger log = LoggerFactory.getLogger(TimerTask.class);

  private transient Class<HeartbeatEvent> heartbeatEventImp;
  private transient Adapter adapter;
  private transient boolean threadRenamed;

  public HeartbeatTimerTask(Class<HeartbeatEvent> eventImpl, Adapter a) {
    heartbeatEventImp = eventImpl;
    this.adapter = a;
  }

  @Override
  public void run() {
    renameThread();
    try {
      HeartbeatEvent heartbeat = EventFactory.create(heartbeatEventImp);
      heartbeat.setHeartbeatTime(System.currentTimeMillis());
      heartbeat.extractState(adapter);
      log.trace("Heartbeat created=" + heartbeatEventImp);
      adapter.getEventHandler().send(heartbeat);
    }
    catch (Exception e) {
      log.trace("exception sending heartbeat Event; logging only", e);
    }
  }

  private void renameThread() {
    if (!threadRenamed) {
      Thread.currentThread().setName("heartbeat");
      threadRenamed = true;
    }
  }
}
