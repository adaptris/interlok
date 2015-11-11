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

package com.adaptris.core.jms;

import java.util.concurrent.TimeUnit;

import org.perf4j.aop.Profiled;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.GenerateBeanInfo;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullMessageConsumer;
import com.adaptris.core.NullProcessingExceptionHandler;
import com.adaptris.core.NullProduceExceptionHandler;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceExceptionHandler;
import com.adaptris.core.RetryMessageErrorHandler;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Subclass of {@link StandardWorkflow} for use with JMS consumers.
 * <p>
 * This differs from a StandardWorkflow as it assumes that the configured JMS Consumer has a transacted session which needs to
 * informed of any failures so the messages can be rolled back.
 * </p>
 * <p>
 * Additionally, this workflow may not be configured with any {@link ProduceExceptionHandler} as this will not allow the transaction
 * to rolled back correctly. In order to get behaviour similiar to {@link ProduceExceptionHandler}, you should use
 * {@link StandaloneProducer} as part of the service collection in order to produce the payload to the required destination.
 * </p>
 * 
 * @config jms-transacted-workflow
 * 
 */
@XStreamAlias("jms-transacted-workflow")
@GenerateBeanInfo
public final class JmsTransactedWorkflow extends StandardWorkflow {

  private static final ThreadLocal<Boolean> LAST_MSG_FAILED = new ThreadLocal<Boolean>() {
    @Override
    protected synchronized Boolean initialValue() {
      return Boolean.FALSE;
    }
  };

  @AdvancedConfig
  private Boolean strict;
  @AdvancedConfig
  private TimeInterval waitPeriodAfterRollback;
  private static final TimeInterval DEFAULT_WAIT_PERIOD = new TimeInterval(30L, TimeUnit.SECONDS.name());

  public JmsTransactedWorkflow() {
    super();
  }

  @Override
  protected void initialiseWorkflow() throws CoreException {
    AdaptrisMessageConsumer amc = getConsumer();
    if (amc instanceof JmsPollingConsumerImpl) {
      ((JmsPollingConsumerImpl) amc).setRollbackTimeout(waitPeriodAfterRollbackMs());
      ((JmsPollingConsumerImpl) amc).setTransacted(Boolean.TRUE);
    }
    if (amc instanceof JmsConsumerImpl) {
      ((JmsConsumerImpl) amc).setRollbackTimeout(waitPeriodAfterRollbackMs());
      ((JmsConsumerImpl) amc).setTransacted(Boolean.TRUE);
    }
    super.initialiseWorkflow();
  }

  @Override
  public void setConsumer(AdaptrisMessageConsumer amc) {
    if (amc instanceof JmsConsumerImpl) {
      ((JmsConsumerImpl) amc).setTransacted(Boolean.TRUE);
    }
    else if (amc instanceof JmsPollingConsumerImpl) {
      ((JmsPollingConsumerImpl) amc).setTransacted(Boolean.TRUE);
    }
    else if (!(amc instanceof NullMessageConsumer)) {
      throw new IllegalArgumentException(this.getClass().getSimpleName() + " must be used with a JMSConsumer");
    }
    super.setConsumer(amc);
  }

  @Override
  public void setProduceExceptionHandler(ProduceExceptionHandler peh) {
    if (!(peh instanceof NullProduceExceptionHandler)) {
      throw new IllegalArgumentException(this.getClass().getSimpleName() + " may not have a ProduceExceptionHandler set");
    }
    super.setProduceExceptionHandler(peh);
  }

  @Override
  @Profiled(tag = "JmsTransactedWorkflow({$this.getConsumer().getDestination().getDeliveryThreadName()})", logger = "com.adaptris.perf4j.jms.TimingLogger")
  public synchronized void onAdaptrisMessage(AdaptrisMessage msg) {
    if (!obtainChannel().isAvailable()) {
      handleChannelUnavailable(msg); // make pluggable?
    }
    else {
      handleMessage(msg, true);
    }
  }

  @Override
  public void doProduce(AdaptrisMessage msg) throws ServiceException, ProduceException {
    super.doProduce(msg);
    LAST_MSG_FAILED.set(Boolean.FALSE);
  }

  @Override
  protected void resubmitMessage(AdaptrisMessage msg) {
    handleMessage(msg, true);
  }

  @Override
  protected void handleBadMessage(String logMsg, Exception e, AdaptrisMessage msg) {
    LAST_MSG_FAILED.set(Boolean.TRUE);
    if (retrieveActiveMsgErrorHandler() instanceof RetryMessageErrorHandler) {
      log.warn(msg.getUniqueId() + " failed with [" + e.getMessage() + "], it will be retried");
    }
    else {
      log.error(logMsg, e);
    }
    if (!(retrieveActiveMsgErrorHandler() instanceof NullProcessingExceptionHandler)) {
      msg.getObjectMetadata().put(CoreConstants.OBJ_METADATA_EXCEPTION, e);
      handleBadMessage(msg);
      if (!isStrict()) {
        LAST_MSG_FAILED.set(Boolean.FALSE);
      }
    }
  }

  boolean lastMessageFailed() {
    return LAST_MSG_FAILED.get();
  }

  public Boolean getStrict() {
    return strict;
  }

  /**
   * Set the behaviour of this workflow with regards to failures.
   * <p>
   * When strict mode is enabled, then any exception will cause a rollback on
   * the session; when strict is false, then if a
   * {@link ProcessingExceptionHandler} is configured that is not
   * {@link NullProcessingExceptionHandler}, the session will not be rolled
   * back.
   * </p>
   *
   * @param strict true or false (default true)
   */
  public void setStrict(Boolean strict) {
    this.strict = strict;
  }

  boolean isStrict() {
    return strict != null ? strict.booleanValue() : true;
  }


  public TimeInterval getWaitPeriodAfterRollback() {
    return waitPeriodAfterRollback;
  }

  public long waitPeriodAfterRollbackMs() {
    return getWaitPeriodAfterRollback() != null ? getWaitPeriodAfterRollback().toMilliseconds() : DEFAULT_WAIT_PERIOD
        .toMilliseconds();
  }

  /**
   * set the amount of time to wait after issue a {@link javax.jms.Session#rollback()} before continuing processing.
   *
   * @param interval the interval, default if not set is 30 seconds.
   */
  public void setWaitPeriodAfterRollback(TimeInterval interval) {
    waitPeriodAfterRollback = interval;
  }

  @Override
  protected void prepareWorkflow() throws CoreException {}



}
