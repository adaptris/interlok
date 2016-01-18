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

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.perf4j.aop.Profiled;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.FifoMutexLock;
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

  private Integer poolSize;
  private Integer minIdle;
  private Integer maxIdle;

  @AdvancedConfig
  private TimeInterval threadKeepAlive;
  @AdvancedConfig
  private TimeInterval shutdownWaitTime;

  @AdvancedConfig
  private Integer threadPriority;

  private transient ExecutorService threadPool;
  private transient GenericObjectPool objectPool;
  private transient FifoMutexLock poolLock;
  private transient AdaptrisMarshaller serviceListMarshaller;
  private transient String currentThreadName;
  private transient ServiceCollection marshalledServiceCollection;
  private transient String friendlyWorkflowName;
  private transient ReentrantLock lock = new ReentrantLock(true);

  public PoolingWorkflow() throws CoreException {
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
    return getPoolSize() != null ? getPoolSize().intValue() : DEFAULT_MAX_POOLSIZE;
  }

  public long threadLifetimeMs() {
    return getThreadKeepAlive() != null ? getThreadKeepAlive().toMilliseconds() : DEFAULT_THREAD_LIFETIME.toMilliseconds();
  }

  public long shutdownWaitTimeMs() {
    return getShutdownWaitTime() != null ? getShutdownWaitTime().toMilliseconds() : DEFAULT_SHUTDOWN_WAIT.toMilliseconds();
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
    if (minIdle() > maxIdle()) {
      log.warn("Minimum number of idle workers > max-idle, max-idle modified");
      setMaxIdle(minIdle());
    }
    if (maxIdle() > poolSize()) {
      log.warn("Maximum number of idle workers > pool-size, re-sizing pool");
      setPoolSize(maxIdle());
    }
    marshalledServiceCollection = cloneServiceCollection(getServiceCollection());
    marshalledServiceCollection.prepare();
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
    createObjectPool();
    populatePool();
    try {
      friendlyWorkflowName = getConsumer().getDestination().getDeliveryThreadName();
    }
    catch (Exception e) {
      friendlyWorkflowName = this.getClass().getSimpleName();
    }
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
  public void onAdaptrisMessage(final AdaptrisMessage msg) {
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
    lock.lock();
    try {
      currentThreadName = Thread.currentThread().getName();
      if (poolLock.permitAvailable()) {
        threadPool.execute(new WorkerThread(friendlyWorkflowName, msg));
      }
      else {
        log.warn("Attempt to process message during shutdown");
        handleBadMessage(msg);
      }
    }
    catch (Exception e) {
      msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, e);
      handleBadMessage(msg);
    } finally {
      lock.unlock();
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
      log.warn(msg.getUniqueId() + " failed with [" + e.getMessage() + "], it will be retried");
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

  private void createObjectPool() {

    objectPool = new GenericObjectPool(new WorkerFactory());
    objectPool.setMaxActive(poolSize());
    objectPool.setMinIdle(minIdle());
    objectPool.setMaxIdle(maxIdle());
    objectPool.setMaxWait(-1L);
    objectPool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
    objectPool.setMinEvictableIdleTimeMillis(threadLifetimeMs());
    objectPool.setTimeBetweenEvictionRunsMillis(threadLifetimeMs() + new Random(threadLifetimeMs()).nextLong());

    threadPool = Executors.newCachedThreadPool(new WorkerThreadFactory());
    if (threadPool instanceof ThreadPoolExecutor) {
      ((ThreadPoolExecutor) threadPool).setKeepAliveTime(threadLifetimeMs(), TimeUnit.MILLISECONDS);
    }
  }

  private void populatePool() throws CoreException {
    try {
      int size = minIdle();
      Worker[] workers = new Worker[size];
      log.trace("Creating " + size + " objects for initial population of the pool");
      for (int i = 0; i < size; i++) {
        workers[i] = (Worker) objectPool.borrowObject();
      }
      for (Worker worker : workers) {
        objectPool.returnObject(worker);
      }
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  private void shutdownPool() {
    try {
      poolLock.acquire();
      if (threadPool != null) {
        log.trace("ThreadPool Shutdown Requested, awaiting Pool Shutdown");
        threadPool.shutdown();
        boolean success = false;
        try {
          success = threadPool.awaitTermination(shutdownWaitTimeMs(), TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
        }
        if (!success) {
          log.trace("Pool failed to shutdown in " + shutdownWaitTimeMs() + "ms, forcing shutdown");
          List<Runnable> list = threadPool.shutdownNow();
          for (Runnable l : list) {
            WorkerThread sd = (WorkerThread) l;
            handleBadMessage(sd.getMessage());
          }
        }
      }
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
    if (i == null) {
      throw new IllegalArgumentException("Thread Priority may not be Null");
    }
    if (i.intValue() > Thread.MAX_PRIORITY) {
      throw new IllegalArgumentException("Greater than " + Thread.MAX_PRIORITY);
    }
    if (i.intValue() < Thread.MIN_PRIORITY) {
      throw new IllegalArgumentException("Less than than " + Thread.MIN_PRIORITY);
    }
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
    return getMinIdle() != null ? getMinIdle().intValue() : DEFAULT_MIN_IDLE;
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
    return getMaxIdle() != null ? getMaxIdle().intValue() : DEFAULT_MAX_IDLE;
  }

  public int threadPriority() {
    return getThreadPriority() != null ? getThreadPriority().intValue() : Thread.NORM_PRIORITY;
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
    for (Iterator i = result.getServices().iterator(); i.hasNext();) {
      Service s = (Service) i.next();
    }
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

  protected void prepareWorkflow() throws CoreException {}


  private class WorkerThreadFactory extends ManagedThreadFactory {

    private transient ThreadGroup threadGroup;

    WorkerThreadFactory() {
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
  private class WorkerFactory implements PoolableObjectFactory {

    WorkerFactory() {
    }

    /**
     * @see PoolableObjectFactory#makeObject()
     */
    @Override
    public Object makeObject() throws Exception {
      Worker w = null;
      try {
        w = new Worker();
        w.start();
      }
      catch (Exception e) {
        log.error("Error creating object for pool", e);
        throw e;
      }
      return w;
    }

    /**
     * @see PoolableObjectFactory#destroyObject(java.lang.Object)
     */
    @Override
    public void destroyObject(Object arg0) throws Exception {
      ((Worker) arg0).stop();
    }

    /**
     * @see PoolableObjectFactory#validateObject(java.lang.Object)
     */
    @Override
    public boolean validateObject(Object arg0) {
      return ((Worker) arg0).isValid();
    }

    /**
     * @see PoolableObjectFactory#activateObject(java.lang.Object)
     */
    @Override
    public void activateObject(Object arg0) throws Exception {
    }

    /**
     * @see PoolableObjectFactory#passivateObject(java.lang.Object)
     */
    @Override
    public void passivateObject(Object arg0) throws Exception {
    }

  }

  private class WorkerThread implements Runnable {
    private String logicalId;
    private AdaptrisMessage message;
    private Worker slave;

    WorkerThread(String name, AdaptrisMessage msg) throws Exception {
      logicalId = name;
      message = msg;
      slave = (Worker) objectPool.borrowObject();
    }

    private AdaptrisMessage getMessage() {
      return message;
    }

    @Override
    public void run() {
      String oldName = Thread.currentThread().getName();
      Thread.currentThread().setName(getThreadName());

      try {
        slave.handleMessage(logicalId, message);
        objectPool.returnObject(slave);
      }
      catch (Exception e) {
        log.trace("[" + toString() + "] failed pool re-entry, attempting to invalidate");
        try {
          objectPool.invalidateObject(slave);
          log.trace("[" + toString() + "] invalidated");
        }
        catch (Exception ignoredIntentionally) {
          log.trace("[" + toString() + "] was not invalidated");
        }
      }
      Thread.currentThread().setName(oldName);
    }

    private String getThreadName() {
      return currentThreadName + "@T-" + Integer.toHexString(Thread.currentThread().hashCode());
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
      LifecycleHelper.init(sc);
      LifecycleHelper.start(sc);
    }

    public void stop() throws CoreException {
      LifecycleHelper.stop(sc);
      LifecycleHelper.close(sc);
    }

    public boolean isValid() {
      return true;
    }

    // This is just really a frig so that we can get profiling
    // information out from PoolingWorkflow as $this by this time
    // isn't all that useful...
    @Profiled(tag = "PoolingWorkflow({$0})", logger = "com.adaptris.perf4j.TimingLogger")
    public void handleMessage(String id, AdaptrisMessage msg) {
      AdaptrisMessage wip = null;
      workflowStart(msg);
      try {
        long start = System.currentTimeMillis();
        log.debug("start processing message [" + msg.toString(logPayload()) + "]");
        wip = (AdaptrisMessage) msg.clone();
        // Set the channel id and workflow id on the message lifecycle.
        wip.getMessageLifecycleEvent().setChannelId(obtainChannel().getUniqueId());
        wip.getMessageLifecycleEvent().setWorkflowId(obtainWorkflowId());
        wip.addEvent(getConsumer(), true);
        sc.doService(wip);
        doProduce(wip);
        logSuccess(wip, start);
      }
      catch (ProduceException e) {
        wip.addEvent(getProducer(), false);
        handleBadMessage("Exception producing message", e, msg);
        handleProduceException();
      }
      catch (Exception e) {
        handleBadMessage("Exception processing message", e, msg);
      }
      finally {
        sendMessageLifecycleEvent(wip);
      }
      workflowEnd(msg, wip);
    }

    @Override
    public String toString() {
      String className = this.getClass().getSimpleName();
      className += "@T-";
      className += Integer.toHexString(Thread.currentThread().hashCode());
      return className;
    }
  }


}
