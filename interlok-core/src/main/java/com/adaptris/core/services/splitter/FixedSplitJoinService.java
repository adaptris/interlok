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

package com.adaptris.core.services.splitter;

import static com.adaptris.core.util.ServiceUtil.discardNulls;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.ServiceWrapper;
import com.adaptris.core.services.aggregator.MessageAggregator;
import com.adaptris.core.services.splitter.ServiceWorkerPool.Worker;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.interlok.util.Closer;
import com.adaptris.util.NumberUtils;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Implementation of the Splitter and Aggregator enterprise integration pattern.
 *
 *
 * @config fixed-split-join-service
 */
@XStreamAlias("fixed-split-join-service")
@AdapterComponent
@ComponentProfile(summary = "Split a message and then execute the associated services on the split items, aggregating the split messages afterwards", tag = "service,splitjoin")
@DisplayOrder(order =
{
    "splitter", "service", "aggregator", "timeout", "poolsize"
})
@NoArgsConstructor
public class FixedSplitJoinService extends ServiceImp implements EventHandlerAware, ServiceWrapper {

  private static TimeInterval DEFAULT_TTL = new TimeInterval(600L, TimeUnit.SECONDS);
  private static final int DEFAULT_POOLSIZE = 10;


  /**
   * The {@link com.adaptris.core.Service} to execute over all the split messages.
   *
   */
  @NotNull
  @Valid
  @Getter
  @Setter
  @NonNull
  private Service service;

  /**
   * The {@link MessageSplitter} implementation to use to split the incoming message.
   *
   */
  @NotNull
  @Valid
  @Getter
  @Setter
  @NonNull
  private MessageSplitter splitter;
  /**
   * The {@link MessageAggregator} implementation to use to join messages together.
   *
   */
  @NotNull
  @Valid
  @Getter
  @Setter
  @NonNull
  private MessageAggregator aggregator;

  /**
   * The max amount of time to wait for all the operations to complete.
   * <p>
   * If not explicitly specified then is set to be 10 minutes; in the event that the timeout is
   * exceeded, then an exception will be thrown eventually.
   * </p>
   */
  @Valid
  @Getter
  @Setter
  private TimeInterval timeout;
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean sendEvents;
  @Getter
  @Setter
  @AdvancedConfig
  @InputFieldDefault(value = "10")
  private Integer poolsize;

  private transient ExecutorService executor;
  private transient EventHandler eventHandler;
  private transient ServiceWorkerPool workerFactory;
  private transient GenericObjectPool<ServiceWorkerPool.Worker> objectPool;


  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getService());
    workerFactory = new ServiceWorkerPool(getService(), eventHandler, poolsize());
  }

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notNull(getSplitter(), "splitter");
      Args.notNull(getAggregator(), "aggregator");
      Args.notNull(getService(), "service");
      objectPool = workerFactory.createCommonsObjectPool();
      executor = workerFactory.createExecutor(this.getClass().getSimpleName());
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public void start() throws CoreException {
    workerFactory.warmup(objectPool);
  }

  @Override
  public void stop() {
    super.stop();
  }

  @Override
  protected void closeService() {
    ManagedThreadFactory.shutdownQuietly(executor, new TimeInterval());
    Closer.closeQuietly(objectPool);
  }



  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      Iterable<AdaptrisMessage> splits = getSplitter().splitMessage(msg);
      ServiceExceptionHandler handler = new ServiceExceptionHandler();
      Iterable<AdaptrisMessage> toAggregate = doSplitService(splits, handler);
      getAggregator().aggregate(msg, toAggregate);
      // TODO: how to handle the pooling-future-exception-strategy behaviour?
      handler.throwFirstException();
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  protected Iterable<AdaptrisMessage> doSplitService(final Iterable<AdaptrisMessage> msgs,
      final ServiceExceptionHandler handler) {
    final LinkedBlockingQueue<AdaptrisMessage> splitQueue = new LinkedBlockingQueue<>();
    final long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs());
    new Thread(() -> {
      int max = poolsize();
      for (AdaptrisMessage m : msgs) {
        try {
          while (objectPool.getNumActive() >= max) {
            waitQuietly(handler);
            checkTimeout(deadline);
          }
          executor.submit(new MyServiceExecutor(m, handler, splitQueue));
        } catch (Exception e) {
          // probably a timeout at this point.
          handler.uncaughtException(Thread.currentThread(), e);
        }
      }
    }).start();
    return new MessageAggregatorIterator(splitQueue, deadline);
  }

  // Take this + PoolingSplitter -> needs to be abstracted into a helper...
  protected void waitQuietly(Object monitor) {
    try {
      Args.notNull(monitor, "monitor");
      synchronized (monitor) {
        monitor.wait();
      }
    } catch (InterruptedException | IllegalArgumentException e) {
    }
  }


  private long timeoutMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getTimeout(), DEFAULT_TTL);
  }

  private int poolsize() {
    return NumberUtils.toIntDefaultIfNull(getPoolsize(), DEFAULT_POOLSIZE);
  }


  private void checkTimeout(long deadlineNanos) throws Exception {
    long nanos = deadlineNanos - System.nanoTime();
    if (nanos <= 0L) {
      throw new TimeoutException();
    }
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }

  @Override
  public Service[] wrappedServices() {
    return discardNulls(getService());
  }


  private boolean sendEvents() {
    return BooleanUtils.toBooleanDefaultIfNull(getSendEvents(), false);
  }

  protected AdaptrisMessage sendEvents(AdaptrisMessage msg) throws CoreException {
    if (eventHandler != null && sendEvents()) {
      eventHandler.send(msg.getMessageLifecycleEvent(), msg.getMessageHeaders());
    }
    return msg;
  }

  private class MessageAggregatorIterator
      implements Iterable<AdaptrisMessage>, Iterator<AdaptrisMessage> {
    private boolean iteratorAvailable = true;
    private long deadlineNanos = -1;
    private AdaptrisMessage nextMessage;
    private LinkedBlockingQueue<AdaptrisMessage> myQueue;

    public MessageAggregatorIterator(LinkedBlockingQueue<AdaptrisMessage> queue, long deadline) {
      deadlineNanos = deadline;
      myQueue = queue;
    }

    @Override
    public Iterator<AdaptrisMessage> iterator() {
      if (!iteratorAvailable) {
        throw new IllegalStateException("iterator() no longer available");
      }
      iteratorAvailable = false;
      return this;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    // How do we know if there's a next, since we can't just peek the queue?
    // Do we just wait until the timeout has expired?
    protected AdaptrisMessage constructAdaptrisMessage() throws Exception {
      AdaptrisMessage next = null;
      do {
        checkTimeout(deadlineNanos);
        next = myQueue.peek();
      } while (next == null);
      return myQueue.remove();
    }

    @Override
    public boolean hasNext() {
      if (nextMessage == null) {
        try {
          nextMessage = constructAdaptrisMessage();
        } catch (Exception e) {
          throw new RuntimeException("Could not construct next AdaptrisMessage", e);
        }
      }
      return nextMessage != null;
    }

    @Override
    public AdaptrisMessage next() {
      AdaptrisMessage ret = nextMessage;
      nextMessage = null;
      return ret;
    }

  }


  private class MyServiceExecutor implements Callable<AdaptrisMessage> {

    private ServiceExceptionHandler handler;
    private AdaptrisMessage myMessage;
    private LinkedBlockingQueue<AdaptrisMessage> myQueue;

    MyServiceExecutor(AdaptrisMessage msg, ServiceExceptionHandler excHandler,
        LinkedBlockingQueue<AdaptrisMessage> queue) {
      handler = excHandler;
      myMessage = msg;
      myQueue = queue;
    }

    @Override
    public AdaptrisMessage call() throws Exception {
      Worker w = objectPool.borrowObject();
      try {
        w.doService(myMessage);
      } catch (Exception e) {
        handler.uncaughtException(Thread.currentThread(), e);
      } finally {
        // put the message onto the queue, so that the aggregator can work on it.
        myQueue.put(myMessage);
        objectPool.returnObject(w);
        synchronized (handler) {
          handler.notifyAll();
        }
      }
      return sendEvents(myMessage);
    }

  }
}
