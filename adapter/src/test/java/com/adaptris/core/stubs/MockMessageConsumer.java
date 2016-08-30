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

package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StartedState;
import com.adaptris.core.StoppedState;

/**
 * <p>
 * Mock implementation of <code>AdaptrisMessageConsumer</code> which allows
 * e.g. test cases to create and submit messages to the registered
 * <code>AdaptrisMessageListener</code>.
 * </p>
 */
public class MockMessageConsumer extends AdaptrisMessageConsumerImp {

  public MockMessageConsumer() {
    super();
  }

  public MockMessageConsumer(ConsumeDestination d, AdaptrisMessageListener m) {
    super();
    setDestination(d);
    registerAdaptrisMessageListener(m);
  }

  public MockMessageConsumer(ConsumeDestination d) {
    super();
    setDestination(d);
  }

  public MockMessageConsumer(AdaptrisMessageListener aml) {
    this();
    registerAdaptrisMessageListener(aml);
  }

  /**
   * <p>
   * Submit a message you've just created.
   * </p>
   */
  public void submitMessage(AdaptrisMessage msg) {
    retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
  }

  @Override
  public void prepare() throws CoreException {
  }

  public void init() throws CoreException {
    changeState(InitialisedState.getInstance());
  }

  public void start() throws CoreException {
    changeState(StartedState.getInstance());
  }

  public void stop() {
    changeState(StoppedState.getInstance());
  }

  public void close() {
    changeState(ClosedState.getInstance());
  }

  @Override
  public String getUniqueId() {
    return null;
  }
}
