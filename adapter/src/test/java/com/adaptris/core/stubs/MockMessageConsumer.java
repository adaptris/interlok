/*
 * $RCSfile: MockMessageConsumer.java,v $
 * $Revision: 1.8 $
 * $Date: 2008/05/22 13:17:42 $
 * $Author: lchan $
 */
package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StartedState;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.StoppedState;
import com.adaptris.util.license.License;

/**
 * <p>
 * Mock implementation of <code>AdaptrisMessageConsumer</code> which allows
 * e.g. test cases to create and submit messages to the registered
 * <code>AdaptrisMessageListener</code>.
 * </p>
 */
public class MockMessageConsumer extends AdaptrisMessageConsumerImp implements StateManagedComponent {

  private ComponentState state = ClosedState.getInstance();

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

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  public void requestInit() throws CoreException {
    state.requestInit(this);
  }

  public void requestStart() throws CoreException {
    state.requestStart(this);
  }

  public void requestStop() {
    state.requestStop(this);
  }

  public void requestClose() {
    state.requestClose(this);
  }

  public ComponentState retrieveComponentState() {
    return state;
  }

  public void init() throws CoreException {
    state = InitialisedState.getInstance();
  }

  public void start() throws CoreException {
    state = StartedState.getInstance();
  }

  public void stop() {
    state = StoppedState.getInstance();
  }

  public void close() {
    state = ClosedState.getInstance();
  }
  
  public void changeState(ComponentState newState) {
    state = newState;
  }

  @Override
  public String getUniqueId() {
    return null;
  }
}
