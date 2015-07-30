/*
 * $RCSfile: MockMessageProducer.java,v $
 * $Revision: 1.12 $
 * $Date: 2008/10/28 13:34:30 $
 * $Author: lchan $
 */
package com.adaptris.core.stubs;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.StartedState;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.StoppedState;
import com.adaptris.util.license.License;

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
  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public MockMessageProducer() {
    producedMessages = new ArrayList<AdaptrisMessage>();
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageProducer#produce
   *      (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    if (msg == null) {
      throw new ProduceException("msg is null");
    }
    log.trace("Produced [" + msg.getUniqueId() + "]");
    producedMessages.add(msg);
  }

  /**
   * <p>
   * Returns the internal store of produced messages.
   * </p>
   *
   * @return the internal store of produced messages
   */
  public List<AdaptrisMessage> getMessages() {
    return producedMessages;
  }

  public int messageCount() {
    return producedMessages.size();
  }
  // nothing to see below here...

  /**
   * @see com.adaptris.core.AdaptrisMessageProducer#produce
   *      (com.adaptris.core.AdaptrisMessage,
   *      com.adaptris.core.ProduceDestination)
   */
  public void produce(AdaptrisMessage msg, ProduceDestination destination)
      throws ProduceException {

    if (msg == null) {
      throw new ProduceException("msg is null");
    }
    if (destination == null) {
      throw new ProduceException("Destination is null");
    }
    produce(msg);
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() throws CoreException {
    state = InitialisedState.getInstance();
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  public void start() throws CoreException {
    state = StartedState.getInstance();
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  public void stop() {
    state = StoppedState.getInstance();
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    state = ClosedState.getInstance();
  }
  
  public void changeState(ComponentState newState) {
    state = newState;
  }

  @Override
  public String getUniqueId() {
    // TODO Auto-generated method stub
    return null;
  }

  public void requestClose() {
    state.requestClose(this);
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

  public ComponentState retrieveComponentState() {
    return state;
  }
}
