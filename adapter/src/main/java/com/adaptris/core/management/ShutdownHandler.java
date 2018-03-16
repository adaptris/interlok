/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core.management;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.JMX;
import javax.management.MBeanServer;
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
public class ShutdownHandler extends Thread {

  private transient static final TimeInterval DEFAULT_WAIT_TIME = new TimeInterval(30L, TimeUnit.SECONDS);

  private transient Logger log = LoggerFactory.getLogger(Thread.class.getName());

  private transient Thread mainThread;
  private transient ManagedThreadFactory threadFactory = new ManagedThreadFactory(ShutdownHandler.class.getSimpleName());
  private transient BootstrapProperties bootProperties;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param bp the adapter to shut down
   */
  public ShutdownHandler(BootstrapProperties bp) throws Exception {
    mainThread = Thread.currentThread();
    this.bootProperties = bp;
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
      AdapterRegistryMBean adapterRegistry = bootProperties.getConfigManager().getAdapterRegistry();
      log.trace("Sending Shutdown events for all adapters");
      AdapterRegistry.sendShutdownEvent(adapterRegistry.getAdapters());
      log.info("Shutting down Adapter(s)");
      shutdown(adapterRegistry.getAdapters());
      while (mainThread.isAlive() && !mainThread.isInterrupted()) {
        mainThread.interrupt();
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          mainThread.interrupt();
          break;
        }
      }
      // log.info("Unregistering Adapter(s)");
      // AdapterRegistry.unregister(adapterRegistry.getAdapters());
    } catch (Exception e) {
      log.debug(e.getMessage(), e);
    }
    log.info("Adapter(s) closed");
  }

  private void shutdown(Set<ObjectName> adapterManagers) {
    int count = adapterManagers.size();
    final CountDownLatch barrier = new CountDownLatch(count + 1);
    final long barrierWait = DEFAULT_WAIT_TIME.toMilliseconds() + TimeUnit.SECONDS.toMillis(2);

    for (ObjectName obj : adapterManagers) {
      Thread t = threadFactory.newThread(new ShutdownAdapter(obj, barrier, false));
      t.setUncaughtExceptionHandler(new IgnoreExceptions());
      t.start();
    }
    boolean shutdownOk = false;
    try {
      barrier.countDown();
      shutdownOk = barrier.await(barrierWait, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
    }
    if (!shutdownOk) {
      log.warn("Shutdown taking too long; initiating forced shutdown");
      forceShutdown(adapterManagers);
    }
    log.trace("Stopping Management Components.");
    try {
      ManagementComponentFactory.stopCreated(bootProperties);
      ManagementComponentFactory.closeCreated(bootProperties);
    } catch (Exception ex) {
      log.warn("Could not stop management components, logging for informational purposes only.", ex);
    }
  }

  private void forceShutdown(Set<ObjectName> adapterManagers) {
    int count = adapterManagers.size();
    final CountDownLatch barrier = new CountDownLatch(count + 1);

    for (ObjectName obj : adapterManagers) {
      Thread t = new Thread(new ShutdownAdapter(obj, barrier, true));
      t.setUncaughtExceptionHandler(new IgnoreExceptions());
      t.start();
    }
    try {
      barrier.countDown();
      barrier.await(DEFAULT_WAIT_TIME.toMilliseconds(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
    }
  }

  private class IgnoreExceptions implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {}
  }

  private class ShutdownAdapter implements Runnable {

    private boolean forceShutdown;
    private ObjectName objName;
    private CountDownLatch barrier;

    ShutdownAdapter(ObjectName name, CountDownLatch barrier, boolean force) {
      this.forceShutdown = force;
      this.objName = name;
      this.barrier = barrier;
    }

    @Override
    public void run() {
      try {
        Thread.currentThread().setName(((forceShutdown) ? "Forced " : "") + "Shutdown: " + objName.getKeyProperty("id"));
        MBeanServer server = JmxHelper.findMBeanServer();
        if (server.isRegistered(objName)) {
          AdapterManagerMBean mgr = JMX.newMBeanProxy(server, objName, AdapterManagerMBean.class);
          doShutdown(mgr);
        }
      } catch (Throwable e) {
      } finally {
        waitQuietly();
      }
    }

    private void doShutdown(AdapterManagerMBean mgr) throws CoreException, TimeoutException {
      if (forceShutdown) {
        mgr.forceClose();
      } else {
        mgr.requestClose(DEFAULT_WAIT_TIME.toMilliseconds());
      }
      mgr.unregisterMBean();
    }

    private void waitQuietly() {
      try {
        barrier.countDown();
        barrier.await(DEFAULT_WAIT_TIME.toMilliseconds(), TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
      }
    }
  }

}
