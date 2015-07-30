package com.adaptris.core.management;

import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.JMX;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.AdapterRegistry;
import com.adaptris.core.runtime.AdapterRegistryMBean;
import com.adaptris.core.util.JmxHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;

/**
 * <p>
 * Runnable implementation to be used as a Shutdown Hook.
 * </p>
 */
class ShutdownHandler extends Thread {

  private transient static final TimeInterval DEFAULT_WAIT_TIME = new TimeInterval(30L, TimeUnit.SECONDS);

  private transient Logger log = LoggerFactory.getLogger(Thread.class.getName());

  private transient AdapterRegistryMBean adapterRegistry;
  private transient Thread mainThread;
  private transient ManagedThreadFactory threadFactory = new ManagedThreadFactory();

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param param the adapter to shut down
   */
  public ShutdownHandler(AdapterRegistryMBean controller) {
    mainThread = Thread.currentThread();
    adapterRegistry = controller;
  }

  /**
   * <p>
   * Attempts to stop then close <code>adapter</code>. May be unsuccessful as
   * state of Adapter is unknown. Issues an <code>AdapterShutdownEvent</code>.
   * Logs any Exceptions.
   * </p>
   *
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    Thread.currentThread().setName("Shutdown Handler");
    // useful when not logging to screen...
    System.out.println("Running ShutdownHandler, please wait...");
    log.info("Running ShutdownHandler...");
    try {
      log.trace("Sending Shutdown events for all adapters");
      AdapterRegistry.sendShutdownEvent(adapterRegistry.getAdapters());
      log.info("Shutting down Adapter(s)");
      shutdown(adapterRegistry.getAdapters());
      while (mainThread.isAlive() && !mainThread.isInterrupted()) {
        mainThread.interrupt();
        try {
          Thread.sleep(1000);
        }
        catch (InterruptedException e) {
          mainThread.interrupt();
          break;
        }
      }
      // log.info("Unregistering Adapter(s)");
      // AdapterRegistry.unregister(adapterRegistry.getAdapters());
    }
    catch (Exception e) {
      log.debug(e.getMessage(), e);
    }
    log.info("Adapter(s) closed");
  }

  private void shutdown(Set<ObjectName> adapterManagers) {
    int count = adapterManagers.size();
    final CyclicBarrier barrier = new CyclicBarrier(count + 1);
    long barrierWait = DEFAULT_WAIT_TIME.toMilliseconds() + TimeUnit.SECONDS.toMillis(2);

    for (ObjectName obj : adapterManagers) {
      final AdapterManagerMBean mgr = JMX.newMBeanProxy(JmxHelper.findMBeanServer(), obj, AdapterManagerMBean.class);
      final ObjectName name = obj;
      threadFactory.newThread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.currentThread().setName("Shutdown " + name);
            mgr.requestClose(DEFAULT_WAIT_TIME.toMilliseconds());
            log.info("Unregistering Adapter");
            mgr.unregisterMBean();
            barrier.await(TimeUnit.SECONDS.toMillis(2), TimeUnit.MILLISECONDS);
          }
          catch (CoreException | InterruptedException | BrokenBarrierException | TimeoutException e) {
          }
        }
      }).start();
    }
    try {
      barrier.await(barrierWait, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
      log.warn("Shutdown taking too long; initiating forced shutdown");
      forceShutdown(adapterManagers);
    }
  }

  private void forceShutdown(Set<ObjectName> adapterManagers) {
    int count = adapterManagers.size();
    final CountDownLatch barrier = new CountDownLatch(count + 1);

    for (ObjectName obj : adapterManagers) {
      final AdapterManagerMBean mgr = JMX.newMBeanProxy(JmxHelper.findMBeanServer(), obj, AdapterManagerMBean.class);
      final ObjectName name = obj;
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.currentThread().setName("Forced Shutdown " + name);
            mgr.forceClose();
            log.info("Unregistering Adapter");
            mgr.unregisterMBean();
            barrier.countDown();
            barrier.await(DEFAULT_WAIT_TIME.toMilliseconds(), TimeUnit.MILLISECONDS);
          }
          catch (CoreException | InterruptedException e) {
          }
        }
      }).start();
    }
    try {
      barrier.countDown();
      barrier.await(DEFAULT_WAIT_TIME.toMilliseconds(), TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e) {
    }
  }
}
