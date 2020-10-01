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

import java.util.ArrayList;
import java.util.List;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.StartedState;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.StoppedState;
import com.adaptris.interlok.util.Args;

/**
 * <p>
 * Mock implementation of <code>AdaptrisMessageProducer</code> for testing.
 * Produces messages to a List which can be retrieved, thus allowing messages to
 * be verified as split, etc., etc.
 * </p>
 */
public class MockMessageProducer extends ProduceOnlyProducerImp implements
 StateManagedComponent, MessageCounter {

  private transient List<AdaptrisMessage> producedMessages;
  private transient ComponentState state = ClosedState.getInstance();
  public MockMessageProducer() {
    producedMessages = new ArrayList<AdaptrisMessage>();
  }


  @Override
  protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
    Args.notNull(msg, "message");
    producedMessages.add(msg);
  }


  @Override
  public void prepare() throws CoreException {
  }

  /**
   * <p>
   * Returns the internal store of produced messages.
   * </p>
   *
   * @return the internal store of produced messages
   */
  @Override
  public List<AdaptrisMessage> getMessages() {
    return producedMessages;
  }

  @Override
  public int messageCount() {
    return producedMessages.size();
  }

  @Override
  public void init() throws CoreException {
    state = InitialisedState.getInstance();
  }

  @Override
  public void start() throws CoreException {
    state = StartedState.getInstance();
  }

  @Override
  public void stop() {
    state = StoppedState.getInstance();
  }

  @Override
  public void close() {
    state = ClosedState.getInstance();
  }

  @Override
  public void changeState(ComponentState newState) {
    state = newState;
  }

  @Override
  public String getUniqueId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void requestClose() {
    state.requestClose(this);
  }

  @Override
  public void requestInit() throws CoreException {
    state.requestInit(this);
  }

  @Override
  public void requestStart() throws CoreException {
    state.requestStart(this);
  }

  @Override
  public void requestStop() {
    state.requestStop(this);
  }

  @Override
  public ComponentState retrieveComponentState() {
    return state;
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return null;
  }
}
