package com.adaptris.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.core.util.ManagedThreadFactory;

/**
 * <p>
 * Implementation of behaviour common to {@link ProduceExceptionHandler} instances
 * </p>
 */
public abstract class ProduceExceptionHandlerImp implements ProduceExceptionHandler {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  protected void restart(final StateManagedComponent s) {
    // spin off exception handler Thread
    Thread t = new ManagedThreadFactory().newThread(new Runnable() {
      @Override
      public void run() {
        try {
          LifecycleHelper.stop(s);
          LifecycleHelper.close(s);
          LifecycleHelper.init(s);
          LifecycleHelper.start(s);
        }
        catch (Exception e) {
          log.error("Failed to restart " + LoggingHelper.friendlyName(s));
        }
      }
    });
    t.setName("Restart " + Thread.currentThread().getName());
    log.trace("Handling Produce Exception");
    t.start();
  }
}
