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

package com.adaptris.core;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.apache.commons.lang3.Range;
import org.apache.commons.pool2.ObjectPool;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.FifoMutexLock;
import com.adaptris.util.NumberUtils;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * A Workflow that pools ServiceCollections.
 * <p>
 * Pooling of <code>ServiceCollection</code>s is useful in situations where the services are considered the bottleneck for the
 * throughput of messages (e.g. local FS to local FS, but with a slow JdbcService or WebServicesQueryService to extract data for
 * lookups).
 * </p>
 * <p>
 * If you specify min-idle, max-idle and pool-size to be equal to each other then you will effectively end up with a fixed size pool
 * of the size requested. There are some instances where a fixed size pool is desirable, such as when the service list that is being
 * pooled takes a significant amount of time to become ready to use (e.g. multiple database connections/JMS connections over a WAN).
 * By making a pool size fixed you only pay the cost of initialisation once when the workflow is first started. Of course, using a
 * fixed size pool can cause its own problems if long-lived connections are terminated silently by the remote system. If you are
 * using {@link com.adaptris.core.SharedConnection} within the service-collection, then it is advised that you use a fixed size pool; otherwise as
 * workers are deactivated then this could cause the underlying connection instance to be closed, which will cause issues for other
 * objects sharing the connection.
 * </p>
 * <p>
 * If <code>stop()</code> is invoked then any messages that are currently being processed will be allowed to finish, however any new
 * messages that enter the workflow via <code>onAdaptrisMessage(AdaptrisMessage)</code> before the
 * <code>AdaptrisMessageConsumer</code> is succesfully stopped will be treated as <b>bad</b> messages and sent directly to the
 * configured {@link com.adaptris.core.ProcessingExceptionHandler}.
 * </p>
 *
 * @config pooling-workflow
 *
 *
 * @author lchan
 * @author $Author: lchan $
 * @see ProcessingExceptionHandler
 */
@XStreamAlias("pooling-workflow")
@AdapterComponent
@ComponentProfile(summary = "Workflow with a thread pool handling the service chain", tag = "workflow,base")
@DisplayOrder(order = {"poolSize", "minIdle", "maxIdle", "threadPriority", "disableDefaultMessageCount"})
public class PoolingWorkflow extends WorkflowWithObjectPool {


  /**
   * The default thread lifetime.
   *
   */
  private static final TimeInterval DEFAULT_THREAD_LIFETIME = new TimeInterval(1L, TimeUnit.MINUTES.name());
  /**
   * The default shutdown wait.
   *
   */
  private static final TimeInterval DEFAULT_SHUTDOWN_WAIT = new TimeInterval(1L, TimeUnit.MINUTES.name());


  private static final Range PRIORITY_RANGE =
      Range.between(Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);

  private transient boolean priorityWarningLogged = false;

  /**
   * Set the lifetime for threads in the pool.
   * <p>
   * Threads that have been dormant for the specified interval are discarded.
   * </p>
   */
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "1 minute")
  @Valid
  @Getter
  @Setter
  private TimeInterval threadKeepAlive;
  /**
   * Set the shutdown wait timeout for the pool.
   * <p>
   * When <code>stop()</code> is invoked, this causes a emptying and shutdown of the pool. The
   * specified value is the amount of time to wait for a clean shutdown. If this timeout is exceeded
   * then a forced shutdown ensues, which may mean messages are in an inconsistent state.
   * </p>
   *
   *
   */
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "1 minute")
  @Valid
  @Getter
  @Setter
  private TimeInterval shutdownWaitTime;


  /**
   * The priority for threads created to handle messages.
   *
   */
  @Getter
  @Setter
  @AdvancedConfig
  @InputFieldDefault(value = "5")
  @Min(Thread.MIN_PRIORITY)
  @Max(Thread.MAX_PRIORITY)
  private Integer threadPriority;

  private transient ExecutorService threadPool;
  private transient ObjectPool<Worker> objectPool;
  private transient FifoMutexLock poolLock;
  private transient String currentThreadName;

  public PoolingWorkflow() {
    super();
    poolLock = new FifoMutexLock();
  }

  public PoolingWorkflow(String uniqueId) throws CoreException {
    this();
    setUniqueId(uniqueId);
  }

  public long threadLifetimeMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getThreadKeepAlive(), DEFAULT_THREAD_LIFETIME);
  }

  public long shutdownWaitTimeMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getShutdownWaitTime(), DEFAULT_SHUTDOWN_WAIT);
  }

  /**
   * Initialise the workflow.
   *
   * @see com.adaptris.core.WorkflowImp#initialiseWorkflow()
   * @throws CoreException if the workflow failed to initialise. This exception encapsulates any underlying exception.
   */
  @Override
  protected void initialiseWorkflow() throws CoreException {
    checkPoolConfig();
    // simply checks that the service collection is ready for work so call prepare()
    // to check any licensing restrictions.
    // init() is done during start.
    ServiceCollection prepareCheck = cloneServiceCollection(getServiceCollection());
    LifecycleHelper.prepare(prepareCheck);
    LifecycleHelper.init(getProducer());
    getConsumer().registerAdaptrisMessageListener(this);
    LifecycleHelper.init(getConsumer());

  }

  /**
   *
   * @see com.adaptris.core.WorkflowImp#startWorkflow()
   */
  @Override
  protected void startWorkflow() throws CoreException {
    LifecycleHelper.start(getProducer());
    objectPool = createObjectPool();
    threadPool = createExecutor();
    populatePool(objectPool);
    LifecycleHelper.start(getConsumer());
  }

  /**
   *
   * @see com.adaptris.core.WorkflowImp#closeWorkflow()
   */
  @Override
  protected void closeWorkflow() {
    LifecycleHelper.close(getConsumer());
    LifecycleHelper.close(getProducer());
  }

  /**
   *
   * @see com.adaptris.core.WorkflowImp#stopWorkflow()
   */
  @Override
  protected void stopWorkflow() {
    LifecycleHelper.stop(getConsumer());
    shutdownPool();
    LifecycleHelper.stop(getProducer());
  }

  @Override
  protected void onMessage(AdaptrisMessage msg) {
    try {
      addConsumeLocation(msg);
      currentThreadName = Thread.currentThread().getName();
      if (poolLock.permitAvailable()) {
        workflowStart(msg);
        // workflowCompletion.add(msg, threadPool.submit(new CallableWorker(msg)));
        threadPool.submit(new CallableWorker(msg));
      }
      else {
        log.warn("Attempt to process message during shutdown; failing it");
        handleBadMessage(msg);
      }
    }
    catch (Exception e) {
      msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, e);
      handleBadMessage(msg);
    }
  }

  /**
   *
   * @see WorkflowImp#handleBadMessage(AdaptrisMessage)
   */
  @Override
  public synchronized void handleBadMessage(AdaptrisMessage msg) {
    super.handleBadMessage(msg);
  }

  @Override
  protected void handleBadMessage(String logMsg, Exception e, AdaptrisMessage msg) {
    if (retrieveActiveMsgErrorHandler() instanceof RetryMessageErrorHandler) {
      log.warn("{} failed with [{}], it will be retried", msg.getUniqueId(), e.getMessage());
    }
    else {
      log.error(logMsg, e);
    }
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, e);
    handleBadMessage(msg);
  }

  /**
   *
   * @see WorkflowImp#doProduce(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public synchronized void doProduce(AdaptrisMessage msg) throws ServiceException, ProduceException {
    super.doProduce(msg);
  }

  /**
   *
   * @see WorkflowImp#handleProduceException()
   */
  @Override
  public synchronized void handleProduceException() {
    super.handleProduceException();
  }

  /**
   *
   * @see WorkflowImp#sendMessageLifecycleEvent(AdaptrisMessage)
   */
  @Override
  protected synchronized void sendMessageLifecycleEvent(AdaptrisMessage msg) {
    super.sendMessageLifecycleEvent(msg);
  }

  private ExecutorService createExecutor() {
    ExecutorService es = Executors.newCachedThreadPool(new WorkerThreadFactory());
    if (es instanceof ThreadPoolExecutor) {
      ((ThreadPoolExecutor) es).setKeepAliveTime(threadLifetimeMs(), TimeUnit.MILLISECONDS);
    }
    return es;
  }


  private void shutdownPool() {
    try {
      poolLock.acquire();
      List<Runnable> list = ManagedThreadFactory.shutdownQuietly(threadPool, shutdownWaitTimeMs());
      for (Runnable l : list) {
        CallableWorker sd = (CallableWorker) l;
        handleBadMessage(sd.getMessage());
      }
      log.trace("All children terminated; existence pointless");
      objectPool.close();
    }
    catch (Exception e) {
      log.warn("Exception shutting down Pool : ", e);
    }
    finally {
      threadPool = null;
      objectPool = null;
    }
    poolLock.release();
  }

  public int threadPriority() {
    int priority = NumberUtils.toIntDefaultIfNull(getThreadPriority(), Thread.NORM_PRIORITY);
    if (!PRIORITY_RANGE.contains(priority)) {
      LoggingHelper.logWarning(priorityWarningLogged, () -> {
        priorityWarningLogged = true;
      }, "thread-priority [{}] isn't in range {}, reset to default", priority,
          PRIORITY_RANGE.toString());
      priority = Thread.NORM_PRIORITY;
    }
    return priority;
  }

  /**
   * Return the total number of objects in the pool. This includes active and idle objects.
   *
   * @return the total number of objects in the pool.
   */
  public int currentObjectPoolCount() {
    return objectPool.getNumActive() + objectPool.getNumIdle();
  }

  /**
   * Return the number of currently active objects. These are objects that may or may not be currently be processing messages but
   * have been borrowed from the pool.
   *
   * @return the currently active objects.
   */
  public int currentlyActiveObjects() {
    return objectPool.getNumActive();
  }

  /**
   * Return the number of currently idle objects. These are objects that are currently in the pool awaiting work.
   *
   * @return the number of idle objects.
   */
  public int currentlyIdleObjects() {
    return objectPool.getNumIdle();
  }

  /**
   * Return the current number of active threads in the thread pool. This number is just a snaphot, and may change immediately upon
   * returning
   *
   * @return the number of threads in the threadpool.
   */
  public int currentThreadPoolCount() {
    return ((ThreadPoolExecutor) threadPool).getPoolSize();
  }

  private class WorkerThreadFactory extends ManagedThreadFactory {

    private transient ThreadGroup threadGroup;

    WorkerThreadFactory() {
      super(PoolingWorkflow.class.getSimpleName());
      threadGroup = new ThreadGroup(toString());
      threadGroup.setMaxPriority(threadPriority());
    }

    @Override
    protected Thread createThread(ThreadGroup group, Runnable r) {
      Thread t = new Thread(threadGroup, r, createName(), 0);
      t.setPriority(threadPriority());
      return t;
    }

  }


  private class CallableWorker implements Callable<AdaptrisMessage> {
    private AdaptrisMessage message;
    private Worker worker;

    CallableWorker(AdaptrisMessage msg) throws Exception {
      message = msg;
      worker = objectPool.borrowObject();
    }

    private AdaptrisMessage getMessage() {
      return message;
    }

    private String getThreadName() {
      return currentThreadName + "(" + Integer.toHexString(Thread.currentThread().hashCode()) + ")";
    }

    @Override
    public AdaptrisMessage call() throws Exception {
      String oldName = Thread.currentThread().getName();
      Thread.currentThread().setName(getThreadName());
      AdaptrisMessage result = null;
      try {
        processingStart(message);
        result = worker.handleMessage(message);
        workflowEnd(message, result);
        objectPool.returnObject(worker);
      } catch (Exception e) {
        log.trace("[{}] failed pool re-entry, attempting to invalidate", toString());
        try {
          objectPool.invalidateObject(worker);
          log.trace("[{}] invalidated", toString());
        } catch (Exception ignoredIntentionally) {
          log.trace("[{}] not invalidated", toString());
        }
      }
      Thread.currentThread().setName(oldName);
      return result;
    }
  }

}
