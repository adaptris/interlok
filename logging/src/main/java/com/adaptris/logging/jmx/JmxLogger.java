package com.adaptris.logging.jmx;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;


public class JmxLogger {

  private JmxLoggingNotification notifier;
  private PriorityBlockingQueue<JmxLoggingEvent> queue = new PriorityBlockingQueue<JmxLoggingEvent>(128);
  private ExecutorService queueConsumer;
  private ExecutorService queueProducer;
  private ObjectName loggerObjectName;
  private volatile boolean started;

  public JmxLogger(ObjectName objName) {
    loggerObjectName = objName;
    notifier = new JmxLoggingNotification();
  }

  public synchronized void start() throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
    queueProducer = Executors.newCachedThreadPool();
    this.setupQueueConsumer();
    ManagementFactory.getPlatformMBeanServer().registerMBean(notifier, loggerObjectName);
    started = true;
  }

  public synchronized void stop() throws MBeanRegistrationException, InstanceNotFoundException {
    queueProducer.shutdownNow();
    queueConsumer.shutdownNow();
    ManagementFactory.getPlatformMBeanServer().unregisterMBean(loggerObjectName);
    started = false;
  }

  public boolean isStarted() {
    return started;
  }

  public void log(final JmxLoggingEvent event) {
    if (!isStarted()) {
      return;
    }
    queueProducer.execute(new Runnable() {
      public void run() {
        queue.put(event);
      }
    });
  }

  private void setupQueueConsumer() {
    queueConsumer = Executors.newSingleThreadExecutor();
    queueConsumer.execute(new Runnable() {
      public void run() {
        try {
          while (true) {
            JmxLoggingEvent event = queue.take();
            notifier.sendNotification(event);
          }
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
      }
    });
  }
}

