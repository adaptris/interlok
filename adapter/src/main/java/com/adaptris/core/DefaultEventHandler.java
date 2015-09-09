package com.adaptris.core;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Basic implementation of <code>EventHandler</code>.
 * </p>
 * 
 * @config default-event-handler
 * @license dependent on underlying connection and producer.
 */
@XStreamAlias("default-event-handler")
public class DefaultEventHandler extends EventHandlerBase {

  private AdaptrisConnection connection; // used for consume and produce
  private AdaptrisMessageProducer producer;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   * 
   * @throws CoreException if any occurs
   */
  public DefaultEventHandler() throws CoreException {
    this(new NullConnection(), new NullMessageProducer());
  }

  public DefaultEventHandler(AdaptrisMessageProducer producer) throws CoreException {
    this(new NullConnection(), producer);
  }

  public DefaultEventHandler(AdaptrisConnection connection, AdaptrisMessageProducer producer) throws CoreException {
    setConnection(connection);
    setProducer(producer);
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#isEnabled (com.adaptris.util.license.License)
   */
  public boolean isEnabled(License license) throws CoreException {
    return getConnection().isEnabled(license) && getProducer().isEnabled(license);
  }

  @Override
  protected AdaptrisMessageSender retrieveProducer() {
    return producer;
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  protected void eventHandlerInit() throws CoreException {
    connection.addExceptionListener(this);
    connection.addMessageProducer(producer);
    LifecycleHelper.init(connection);
    LifecycleHelper.init(producer);
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  protected void eventHandlerStart() throws CoreException {
    LifecycleHelper.start(producer);
    LifecycleHelper.start(connection);
  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  protected void eventHandlerStop() {
    LifecycleHelper.stop(connection);
    LifecycleHelper.stop(producer);
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  protected void eventHandlerClose() {
    LifecycleHelper.close(connection);
    LifecycleHelper.close(producer);
  }

  /**
   * <p>
   * Sets the <code>AdaptrisConnection</code> to use. May not be null.
   * </p>
   * 
   * @param c the <code>AdaptrisConnection</code> to use
   */
  public void setConnection(AdaptrisConnection c) {
    if (c == null) {
      throw new IllegalArgumentException("null param");
    }
    if (!retrieveComponentState().equals(ClosedState.getInstance())) {
      throw new IllegalStateException("Attempt to set the connection when already initialised");
    }
    connection = c;
  }

  /**
   * <p>
   * Returns the <code>AdaptrisConnection</code> to use.
   * </p>
   * 
   * @return the <code>AdaptrisConnection</code> to use
   */
  public AdaptrisConnection getConnection() {
    return connection;
  }

  /**
   * <p>
   * Sets the <code>AdaptrisMessageProducer</code> to use. May not be null.
   * </p>
   * 
   * @param p the <code>AdaptrisMessageProducer</code> to use
   */
  public void setProducer(AdaptrisMessageProducer p) {
    if (p == null) {
      throw new IllegalArgumentException("null param");
    }
    if (!retrieveComponentState().equals(ClosedState.getInstance())) {
      throw new IllegalStateException("Attempt to set the producer when already initialised");
    }
    producer = p;
  }

  /**
   * <p>
   * Returns the <code>AdaptrisMessageProducer</code> to use.
   * </p>
   * 
   * @return the <code>AdaptrisMessageProducer</code> to use
   */
  public AdaptrisMessageProducer getProducer() {
    return producer;
  }

}
