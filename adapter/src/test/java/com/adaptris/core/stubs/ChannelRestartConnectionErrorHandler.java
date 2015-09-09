package com.adaptris.core.stubs;

import javax.jms.JMSException;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.Channel;
import com.adaptris.core.ConnectionErrorHandlerImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;

/**
 * Attempt to restart the channel where there's an error. This is purely for
 * testing purposes to illustrate a bug.
 */
public class ChannelRestartConnectionErrorHandler extends
    ConnectionErrorHandlerImp {

  /** @see com.adaptris.core.ConnectionErrorHandler#init() */
  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  /** @see javax.jms.ExceptionListener#onException(javax.jms.JMSException) */
  public void onException(JMSException e) {
    try {
      handleConnectionException();
    }
    catch (Exception x) {
      log.error("Unexpected Exception thrown back to onException", x);
    }
  }

  @Override
  public void handleConnectionException() {
    super.restartAffectedComponents();
  }

  /**
   * <p>
   * Calls init then start on the owning <code>AdaptrisComponent</code>. If
   * an <code>Exception</code> is encountered, <code>onFail</code> is
   * called, otherwise <code>onSuccess</code>.
   * </p>
   */
  private void doRestart(AdaptrisComponent owner) {
    try {
      log.trace("Attempting to initialise the channel");
      LifecycleHelper.init(owner);
      log.trace("Attempting to start the channel");
      LifecycleHelper.start(owner);

      onSuccess(owner);
    }
    catch (Exception e) {
      onFail(owner, e);
    }
  }

  /**
   * <p>
   * Failure is logged and if <code>owner</code> is a <code>Channel</code>
   * an event is sent.
   * </p>
   */
  private void onFail(AdaptrisComponent owner, Exception e) {
    log.error("exception restarting component [" + owner + "]", e);
    log.error("this component has been permanently stopped");
  }

  /**
   * <p>
   * Success is logged and if <code>owner</code> is a <code>Channel</code>
   * an event is sent.
   * </p>
   */
  private void onSuccess(AdaptrisComponent channel) {
    log.info("component restart complete");
    if (channel instanceof Channel) {
      ((Channel) channel).toggleAvailability(true);
    }
  }


}