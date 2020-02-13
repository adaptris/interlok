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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.apache.commons.lang3.Range;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
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
public class PoolingWorkflow extends WorkflowImp {

  /**
   * The default maximum pool size.
   *
   */
  public static final int DEFAULT_MAX_POOLSIZE = 10;
  /**
   * the default minimum idle size.
   *
   */
  public static final int DEFAULT_MIN_IDLE = 1;
  /**
   * The default max idle size.
   */
  public static final int DEFAULT_MAX_IDLE = DEFAULT_MAX_POOLSIZE;

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
  /**
   * The default wait time for pool initialisation
   *
   */
  private static final TimeInterval DEFAULT_INIT_WAIT = new TimeInterval(1L, TimeUnit.MINUTES.name());

  private static final Range PRIORITY_RANGE =
      Range.between(Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);

  private transient boolean priorityWarningLogged = false;

  @InputFieldDefault(value = "10")
  private Integer poolSize;
  @InputFieldDefault(value = "1")
  private Integer minIdle;
  @InputFieldDefault(value = "10")
  private Integer maxIdle;

  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "1 minute")
  @Valid
  private TimeInterval threadKeepAlive;
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "1 minute")
  @Valid
  private TimeInterval shutdownWaitTime;
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "1 minute")
  @Valid
  private TimeInterval initWaitTime;

  @AdvancedConfig
  @InputFieldDefault(value = "5")
  @Min(Thread.MIN_PRIORITY)
  @Max(Thread.MAX_PRIORITY)
  private Integer threadPriority;

  private transient ExecutorService threadPool;
  private transient GenericObjectPool<Worker> objectPool;
  private transient FifoMutexLock poolLock;
  private transient AdaptrisMarshaller serviceListMarshaller;
  private transient String currentThreadName;
  private transient ServiceCollection marshalledServiceCollection;

  public PoolingWorkflow() {
    super();
    poolLock = new FifoMutexLock();
    serviceListMarshaller = DefaultMarshaller.getDefaultMarshaller();
  }

  public PoolingWorkflow(String uniqueId) throws CoreException {
    this();
    setUniqueId(uniqueId);
  }

  /**
   * Set the size of the pool.
   *
   * @param i the size of the pool
   */
  public void setPoolSize(Integer i) {
    poolSize = i;
  }

  /**
   * Get the size of the pool.
   *
   * @return the size.
   * @see #setPoolSize(Integer)
   */
  public Integer getPoolSize() {
    return poolSize;
  }

  public int poolSize() {
    return NumberUtils.toIntDefaultIfNull(getPoolSize(), DEFAULT_MAX_POOLSIZE);
  }

  public long threadLifetimeMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getThreadKeepAlive(), DEFAULT_THREAD_LIFETIME);
  }

  public long shutdownWaitTimeMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getShutdownWaitTime(), DEFAULT_SHUTDOWN_WAIT);
  }

  public TimeInterval getThreadKeepAlive() {
    return threadKeepAlive;
  }

  /**
   * Set the lifetime for threads in the pool.
   * <p>
   * Threads that have been dormant for the specified interval are discarded.
   * </p>
   *
   * @param interval the lifetime (default is 60 seconds)
   */
  public void setThreadKeepAlive(TimeInterval interval) {
    threadKeepAlive = interval;
  }

  public TimeInterval getShutdownWaitTime() {
    return shutdownWaitTime;
  }

  /**
   * Set the shutdown wait timeout for the pool.
   * <p>
   * When <code>stop()</code> is invoked, this causes a emptying and shutdown of the pool. The specified value is the amount of time
   * to wait for a clean shutdown. If this timeout is exceeded then a forced shutdown ensues, which may mean messages are in an
   * inconsistent state.
   * </p>
   *
   * @param interval the shutdown time (default is 60 seconds)
   * @see #stop()
   */
  public void setShutdownWaitTime(TimeInterval interval) {
    shutdownWaitTime = interval;
  }

  /**
   * Initialise the workflow.
   *
   * @see com.adaptris.core.WorkflowImp#initialiseWorkflow()
   * @throws CoreException if the workflow failed to initialise. This exception encapsulates any underlying exception.
   */
  @Override
  protected void initialiseWorkflow() throws CoreException {

    if (maxIdle() > poolSize()) {
      log.warn("Maximum number of idle workers > pool-size, re-sizing max-idle");
      setMaxIdle(poolSize());
    }
    if (minIdle() > poolSize()) {
      log.warn("Minimum number of idle workers > pool-size, re-sizing min-idle");
      setMinIdle(poolSize());
    }
    if (minIdle() > maxIdle()) {
      log.warn("Minimum number of idle workers > max-idle, max-idle modified");
      setMaxIdle(minIdle());
    }
    marshalledServiceCollection = cloneServiceCollection(getServiceCollection());
    LifecycleHelper.prepare(marshalledServiceCollection);
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
    populatePool();
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

  /**
   * Process a message from the <code>MessageConsumer</code>
   * <p>
   * If <code>stop()</code> is invoked then any messages that are currently being processed will be allowed to finish, however any
   * new messages that enter the workflow via <code>onAdaptrisMessage(AdaptrisMessage)</code> will be treated as BAD messages and
   * sent directly to the configured MessageErrorHandler.
   * </p>
   *
   * @see AdaptrisMessageListener#onAdaptrisMessage(AdaptrisMessage)
   * @see WorkflowImp#handleBadMessage(AdaptrisMessage)
   *
   * @param msg the AdaptrisMessage.
   */
  @Override
  public void onAdaptrisMessage(final AdaptrisMessage msg, Consumer<AdaptrisMessage> success) {
    ListenerCallbackHelper.prepare(msg, success);
    if (!obtainChannel().isAvailable()) {
      handleChannelUnavailable(msg);
    }
    else {
      onMessage(msg);
    }
  }

  /**
   *
   * @see WorkflowImp#resubmitMessage(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  protected void resubmitMessage(AdaptrisMessage msg) {
    onMessage(msg);
  }

  private void onMessage(AdaptrisMessage msg) {
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

  private GenericObjectPool<Worker> createObjectPool() {
    GenericObjectPool<Worker> pool = new GenericObjectPool<>(new WorkerFactory());
    long lifetime = threadLifetimeMs();
    pool.setMaxTotal(poolSize());
    pool.setMinIdle(minIdle());
    pool.setMaxIdle(maxIdle());
    pool.setMaxWaitMillis(-1L);
    pool.setBlockWhenExhausted(true);
    pool.setSoftMinEvictableIdleTimeMillis(lifetime);
    pool.setTimeBetweenEvictionRunsMillis(
        lifetime + ThreadLocalRandom.current().nextLong(lifetime));
    return pool;
  }

  private ExecutorService createExecutor() {
    ExecutorService es = Executors.newCachedThreadPool(new WorkerThreadFactory());
    if (es instanceof ThreadPoolExecutor) {
      ((ThreadPoolExecutor) es).setKeepAliveTime(threadLifetimeMs(), TimeUnit.MILLISECONDS);
    }
    return es;
  }

  private void populatePool() throws CoreException {
    int size = minIdle();
    ExecutorService populator = Executors.newCachedThreadPool();
    try {
      final CyclicBarrier barrier = new CyclicBarrier(size + 1);
      log.trace("Need more ({}) children as soon as possible to handle work. Get to it", size);
      final List<Worker> workers = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        populator.execute(new Runnable() {
          @Override
          public void run() {
            try {
              Worker w = objectPool.borrowObject();
              workers.add(w);
              barrier.await(initWaitTimeMs(), TimeUnit.MILLISECONDS);
            }
            catch (Exception e) {
              barrier.reset();
            }
          }
        });
      }
      barrier.await(initWaitTimeMs(), TimeUnit.MILLISECONDS);
      for (Worker worker : workers) {
        objectPool.returnObject(worker);
      }
    }
    catch (Exception e) {
      throw new CoreException(e);
    } finally {
      populator.shutdownNow();
    }
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

  public Integer getThreadPriority() {
    return threadPriority;
  }

  public void setThreadPriority(Integer i) {
    threadPriority = i;
  }

  /**
   * Return the minimum idle objects in the pool.
   *
   * @return the minimum idle number
   */
  public Integer getMinIdle() {
    return minIdle;
  }

  /**
   * Set the minimum number of idle objects in the pool.
   *
   * @param i the minIdle to set
   */
  public void setMinIdle(Integer i) {
    minIdle = i;
  }

  /**
   * Return the maximum idle objects in the pool.
   *
   * @return the maximum idle number
   */
  public int minIdle() {
    return NumberUtils.toIntDefaultIfNull(getMinIdle(), DEFAULT_MIN_IDLE);
  }

  /**
   * Return the maximum idle objects in the pool.
   *
   * @return the maximum idle number
   */
  public Integer getMaxIdle() {
    return maxIdle;
  }

  /**
   * Set the maximum number of idle objects in the pool.
   *
   * @param i the maxIdle to set (default 10)
   */
  public void setMaxIdle(Integer i) {
    maxIdle = i;
  }

  /**
   * Return the maximum idle objects in the pool.
   *
   * @return the maximum idle number
   */
  public int maxIdle() {
    return NumberUtils.toIntDefaultIfNull(getMaxIdle(), DEFAULT_MAX_IDLE);
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
   * @return the initWaitTime
   */
  public TimeInterval getInitWaitTime() {
    return initWaitTime;
  }

  /**
   * Set the amount of time to wait for object pool population.
   * <p>
   * Upon start the object pool is populated with the {@link #minIdle()} number of workers.
   * </p>
   * 
   * @param t the initWaitTime to set, default if not specified is 1 minute
   */
  public void setInitWaitTime(TimeInterval t) {
    this.initWaitTime = t;
  }

  public long initWaitTimeMs() {
    return TimeInterval.toMillisecondsDefaultIfNull(getInitWaitTime(), DEFAULT_INIT_WAIT);
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

  private ServiceCollection cloneServiceCollection(ServiceCollection original) throws CoreException {
    ServiceCollection result = null;
    result = (ServiceCollection) serviceListMarshaller.unmarshal(serviceListMarshaller.marshal(original));
    LifecycleHelper.registerEventHandler(result, eventHandler);
    return result;
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

  @Override
  protected void prepareWorkflow() throws CoreException {}


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

  /**
   * The Manager private class. This is responsible for creating objects when requested by the object pool
   */
  private class WorkerFactory implements PooledObjectFactory<Worker> {

    WorkerFactory() {
    }


    @Override
    public PooledObject<Worker> makeObject() throws Exception {
      Worker w = null;
      try {
        w = new Worker();
        w.start();
      }
      catch (Exception e) {
        log.error("Error creating object for pool", e);
        throw e;
      }
      return new DefaultPooledObject<>(w);
    }


    @Override
    public void destroyObject(PooledObject<Worker> arg0) throws Exception {
      arg0.getObject().stop();
    }

    @Override
    public boolean validateObject(PooledObject<Worker> arg0) {
      return arg0.getObject().isValid();
    }


    @Override
    public void activateObject(PooledObject<Worker> arg0) throws Exception {
    }

    @Override
    public void passivateObject(PooledObject<Worker> arg0) throws Exception {
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


  class Worker {

    private ServiceCollection sc;

    Worker() throws CoreException {
      try {
        sc = cloneServiceCollection(marshalledServiceCollection);
      }
      catch (Exception e) {
        throw new CoreException(e);
      }
    }

    public void start() throws CoreException {
      LifecycleHelper.initAndStart(sc, false);
    }

    public void stop() throws CoreException {
      LifecycleHelper.stopAndClose(sc, false);
    }

    public boolean isValid() {
      return true;
    }

    public AdaptrisMessage handleMessage(AdaptrisMessage msg) {
      AdaptrisMessage wip = null;
      try {
        long start = System.currentTimeMillis();
        log.debug("start processing msg [{}]", messageLogger().toString(msg));
        wip = (AdaptrisMessage) msg.clone();
        // Set the channel id and workflow id on the message lifecycle.
        wip.getMessageLifecycleEvent().setChannelId(obtainChannel().getUniqueId());
        wip.getMessageLifecycleEvent().setWorkflowId(obtainWorkflowId());
        wip.addEvent(getConsumer(), true);
        sc.doService(wip);
        doProduce(wip);
        // handle success callback here.
        // failure callback will be handled by the message-error-handler that's configured...
        ListenerCallbackHelper.handleSuccessCallback(wip);
        logSuccess(wip, start);
      }
      catch (ProduceException e) {
        wip.addEvent(getProducer(), false);
        handleBadMessage("Exception producing message", e, copyExceptionHeaders(wip, msg));
        handleProduceException();
      }
      catch (Exception e) {
        handleBadMessage("Exception processing message", e, copyExceptionHeaders(wip, msg));
      }
      finally {
        sendMessageLifecycleEvent(wip);
      }
      return wip;
    }
  }

}
