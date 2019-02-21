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

package com.adaptris.core.lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultChannelLifecycleStrategy;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Blocking strategy for starting channels.
 * <p>
 * Functionally it is equivalent to DefaultChannelLifecycleStrategy; however each channel operation is performed in its own Thread
 * which is named after the Channel's unique id. It may be of use for logging purposes when attempting to start (or stop) many
 * channels.
 * </p>
 * 
 * @config blocking-channel-lifecycle-strategy
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("blocking-channel-lifecycle-strategy")
public class BlockingChannelLifecycleStrategy extends DefaultChannelLifecycleStrategy {
  private static enum ChannelAction {
    INIT {
      @Override
      void invoke(Channel c) throws CoreException {
        LifecycleHelper.init(c);
      }
    },
    START {
      @Override
      void invoke(Channel c) throws CoreException {
        LifecycleHelper.start(c);
      }
    },
    STOP {
      @Override
      void invoke(Channel c) throws CoreException {
        LifecycleHelper.stop(c);
      }
    },
    CLOSE {
      @Override
      void invoke(Channel c) throws CoreException {
        LifecycleHelper.close(c);
      }
    };
    abstract void invoke(Channel c) throws CoreException;
  }

  private static final TimeInterval DEFAULT_TIMEOUT_INTERVAL = new TimeInterval(2L, TimeUnit.MINUTES.name());

  private TimeInterval timeout;

  public BlockingChannelLifecycleStrategy() {

  }

  public BlockingChannelLifecycleStrategy(TimeInterval t) {
    this();
    setTimeout(t);
  }

  @Override
  public void init(List<Channel> channels) throws CoreException {
    handleStartup(channels, ChannelAction.INIT);
  }

  @Override
  public void start(List<Channel> channels) throws CoreException {
    handleStartup(channels, ChannelAction.START);
  }


  @Override
  public void stop(List<Channel> channels) {
    handleShutdown(channels, ChannelAction.STOP);
  }

  @Override
  public void close(List<Channel> channels) {
    handleShutdown(channels, ChannelAction.CLOSE);
  }

  private int toBeStarted(List<Channel> channels) {
    int result = 0;
    for (Channel c : channels) {
      if (c.shouldStart()) {
        result++;
      }
    }
    return result;
  }


  private void handleShutdown(List<Channel> channels, ChannelAction op) {
    final CyclicBarrier gate = new CyclicBarrier(channels.size() + 1);
    try {
      handleOperation(channels, op, gate);
    }
    catch (CoreException ignored) {
      // log.trace("Ignoring Exception during " + op.name(), ignored);
    }

  }

  private void handleStartup(List<Channel> channels, ChannelAction op) throws CoreException {
    final CyclicBarrier gate = new CyclicBarrier(toBeStarted(channels) + 1);
    List<Channel> eligible = new ArrayList<Channel>();
    for (Channel c : channels) {
      if (c.shouldStart()) {
        eligible.add(c);
      }
    }
    handleOperation(eligible, op, gate);
  }

  private void handleOperation(List<Channel> channels, ChannelAction op, CyclicBarrier gate) throws CoreException {
    ManagedThreadFactory factory = new ManagedThreadFactory(getClass().getSimpleName());
    Set<Thread> myThreads = Collections.newSetFromMap(new WeakHashMap<Thread, Boolean>());
    final CoreExceptionHandler exceptions = new CoreExceptionHandler();
    for (int i = 0; i < channels.size(); i++) {
      final Channel c = channels.get(i);
      final String name = c.hasUniqueId() ? c.getUniqueId() : "Channel(" + i + ")";
      Thread t = factory.newThread(new ChannelInvocationThread(name + "." + op.name(), exceptions, gate, op, c));
      t.setUncaughtExceptionHandler(exceptions);
      myThreads.add(t);
      t.start();
    }
    try {
      gate.await(timeoutMs(), TimeUnit.MILLISECONDS);
    }
    catch (Exception gateException) {
      CoreException e = new CoreException("Exception waiting for all channel." + op.name() + " operations to complete",
          gateException);
      exceptions.uncaughtException(Thread.currentThread(), e);
      // I've been interrupted; so interrupt children!
      interrupt(myThreads);
    }
    CoreException e = exceptions.getFirstThrowableException();
    if (e != null) {
      throw e;
    }
  }

  private synchronized static void prune(Set<Thread> threads) {
    synchronized (threads) {
      Set<Thread> pruneList = new HashSet<Thread>();
      for (Thread t : threads) {
        if (!t.isAlive()) {
          pruneList.add(t);
        }
      }
      threads.removeAll(pruneList);
    }
  }

  private static void interrupt(Set<Thread> threads) {
    while (threads.size() > 0) {
      for (Thread t : threads) {
        if (t.isAlive() && !t.isInterrupted()) {
          t.interrupt();
        }
      }
      try {
        Thread.sleep(1000);
      }
      catch (Exception e) {
        ;
      }
      prune(threads);
    }
  }

  private long timeoutMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getTimeout(), DEFAULT_TIMEOUT_INTERVAL);
  }

  public TimeInterval getTimeout() {
    return timeout;
  }

  /**
   * Set the timeout for a channel operation.
   *
   * @param timeout the timeout.
   */
  public void setTimeout(TimeInterval timeout) {
    this.timeout = timeout;
  }

  private class CoreExceptionHandler implements Thread.UncaughtExceptionHandler {
    private List<Throwable> exceptionList = Collections.synchronizedList(new ArrayList<Throwable>());

    /**
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
      log.error("uncaughtException from " + t.getName(), e);
      exceptionList.add(e);
    }

    public CoreException getFirstThrowableException() {
      CoreException result = null;
      for (Throwable t : exceptionList) {
        if (t instanceof CoreException) {
          result = (CoreException) t;
          break;
        }
      }
      if (result == null && exceptionList.size() > 0) {
        Throwable t = exceptionList.get(0);
        if (t instanceof RuntimeException) {
          throw (RuntimeException) t;
        }
        else if (t instanceof Error) {
          throw (Error) t;
        }
        else {
          throw new RuntimeException(t);
        }
      }
      return result;
    }
  }

  private class ChannelInvocationThread implements Runnable {
    private CoreExceptionHandler exceptions;
    private CyclicBarrier gate;
    private ChannelAction op;
    private Channel channel;
    private final String threadName;

    ChannelInvocationThread(String name, CoreExceptionHandler ceh, CyclicBarrier cb, ChannelAction ca, Channel ch) {
      threadName = name;
      exceptions = ceh;
      gate = cb;
      op = ca;
      channel = ch;
    }

    @Override
    public void run() {
      Thread.currentThread().setName(threadName);
      try {
        op.invoke(channel);
      }
      catch (CoreException e) {
        exceptions.uncaughtException(Thread.currentThread(), e);
      }
      catch (RuntimeException e) {
        exceptions.uncaughtException(Thread.currentThread(), e);
      }
      try {
        gate.await(timeoutMs(), TimeUnit.MILLISECONDS);
      }
      catch (Exception gateException) {
        CoreException e = new CoreException("Exception waiting for all channel." + op.name() + " operations to complete",
            gateException);
        exceptions.uncaughtException(Thread.currentThread(), e);
      }
    }
  }

}
