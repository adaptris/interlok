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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.pool2.ObjectPool;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Closer;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Workflow that executes services in the current thread.
 *
 * <p>
 * This is different to {@link StandardWorkflow} in the sense that it does not synchronize on the
 * {@code onAdaptrisMessage()} method and processes the message in the same thread as the caller.
 * This is useful if you have a consumer that may be triggered by multiple threads (e.g. Jetty or
 * JMS) but you don't want to use {@link PoolingWorkflow}. It is probably of little or no benefit
 * where the consumer is a polling implementation.
 * </p>
 * <p>
 * Since services cannot be guaranteed to be thread-safe; it maintains an internal object pool that
 * is independently sizeable.
 * </p>
 *
 * @config thread-context-workflow
 */
@XStreamAlias("thread-context-workflow")
@ComponentProfile(summary = "Workflow that executes within the current thread context.",
    tag = "workflow",
    since = "3.12.0")
@DisplayOrder(order =
{
    "poolSize", "minIdle", "maxIdle", "disableDefaultMessageCount"
})
@NoArgsConstructor
public class ThreadContextWorkflow extends WorkflowWithObjectPool {

  private enum PoolActivity {
    Create, Borrow, Return, Close
  };

  /**
   * Append the current thread name to any generated friendly name.
   * <p>
   * If you are using this workflow, then there may be thread naming in play (by jetty or otherwise)
   * which you may wish to preserve. Setting this flag to true means that the current thread name
   * from {@code Thread.currentThread().currentName()} is appended to any generated friendly name.
   * </p>
   * <p>
   * It defaults to true if not explicitly configured
   * </p>
   *
   */
  @Getter
  @Setter
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean addCurrentThreadName;

  /**
   * Log the internal object pool state.
   *
   */
  @Getter
  @Setter
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "false")
  private Boolean additionalDebug;

  @Getter(AccessLevel.PACKAGE)
  private transient ObjectPool<Worker> objectPool;

  @Override
  protected void onMessage(AdaptrisMessage msg) {
    Worker worker = null;
    try {
      workflowStart(msg);
      processingStart(msg);
      addConsumeLocation(msg);
      worker = objectPool.borrowObject();
      logPoolState(PoolActivity.Borrow);
      AdaptrisMessage result = worker.handleMessage(msg);
      workflowEnd(msg, result);
      returnObject(objectPool, worker);
      logPoolState(PoolActivity.Return);
    } catch (Exception e) {
      msg.addObjectHeader(CoreConstants.OBJ_METADATA_EXCEPTION, e);
      handleBadMessage(msg);
    }
  }

  private void logPoolState(PoolActivity activity) {
    if (additionalDebug()) {
      log.trace("Current Pool after [{}]: Idle [{}], Active [{}], Max [{}]",
          activity.name(),
          objectPool.getNumIdle(),
          objectPool.getNumActive(), poolSize());
    }
  }

  private boolean additionalDebug() {
    return BooleanUtils.toBooleanDefaultIfNull(getAdditionalDebug(), false);
  }

  // Override the friendly name for logging purposes.
  //
  @Override
  public String friendlyName() {
    return super.friendlyName() + threadName();
  }

  private String threadName() {
    return BooleanUtils.toBooleanDefaultIfNull(getAddCurrentThreadName(), true)
        ? "(" + Thread.currentThread().getName() + ")"
        : "";
  }

  @Override
  protected void initialiseWorkflow() throws CoreException {
    preFlightServiceCheck();
    LifecycleHelper.init(getProducer());
    getConsumer().registerAdaptrisMessageListener(this);
    LifecycleHelper.init(getConsumer());
  }

  @Override
  protected void startWorkflow() throws CoreException {
    LifecycleHelper.start(getProducer());
    objectPool = populatePool(createObjectPool());
    logPoolState(PoolActivity.Create);
    LifecycleHelper.start(getConsumer());
  }

  @Override
  protected void stopWorkflow() {
    LifecycleHelper.stop(getConsumer());
    Closer.closeQuietly(objectPool);
    logPoolState(PoolActivity.Close);
    LifecycleHelper.stop(getProducer());
  }

  @Override
  protected void closeWorkflow() {
    LifecycleHelper.close(getConsumer());
    LifecycleHelper.close(getProducer());
  }
}
