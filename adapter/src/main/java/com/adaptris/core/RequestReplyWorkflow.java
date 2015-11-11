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

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link Workflow} to handle synchronous replies.
 * <p>
 * Key differences to {@link StandardWorkflow} are
 * <ul>
 * <li>uses the <code>request</code> method of the configured {@link AdaptrisMessageProducer}.</li>
 * <li>has a {@link #setReplyServiceCollection(ServiceCollection)} and {@link #setReplyProducer(AdaptrisMessageProducer)} which are
 * used to process any messages prior to returning it back to the requestor</li>
 * <li>Does not obey the use of {@link CoreConstants#KEY_WORKFLOW_SKIP_PRODUCER}, the producer is always triggered.</li>
 * </ul>
 * </p>
 * <p>
 * Note that the reply producer shares the original {@link AdaptrisMessageConsumer}'s connection, on the basis that it will be
 * replying to wherever the request came from.
 * </p>
 * 
 * @config request-reply-workflow
 * 
 * 
 */
@XStreamAlias("request-reply-workflow")
public class RequestReplyWorkflow extends StandardWorkflow {

  private static final TimeInterval DEFAULT_REPLY_TIMEOUT = new TimeInterval(30L, TimeUnit.SECONDS);

  @AdvancedConfig
  private Boolean retainUniqueId;
  @NotNull
  @AutoPopulated
  private ServiceCollection replyServiceCollection;
  @NotNull
  @AutoPopulated
  private AdaptrisMessageProducer replyProducer;
  @AdvancedConfig
  private TimeInterval replyTimeout;

  /**
   * <p>
   * Creates a new instance. Default timeout is 30 seconds, other defaults prevent NullPointerExceptions.
   * </p>
   */
  public RequestReplyWorkflow() {
    replyServiceCollection = new ServiceList();
    replyProducer = new NullMessageProducer();
  }

  /**
   * <p>
   * Overrides <code>Workflow</code> to provide req-rep functionality. Exceptions are correctly handled in super-class.
   * </p>
   *
   * @param msg the message to process
   * @throws ProduceException from both request and reply produce
   * @throws ServiceException from reply services
   * @see com.adaptris.core.Workflow#doProduce(AdaptrisMessage)
   */
  @Override
  public void doProduce(AdaptrisMessage msg) throws ServiceException, ProduceException {

    // blocking request...
    String originalId = msg.getUniqueId();

    AdaptrisMessage reply = getProducer().request(msg, replyTimeout());
    msg.addEvent(getProducer(), true);
    if (reply != null) {
      if (retainUniqueId()) {
        reply.setUniqueId(originalId);
      }
      try {
        replyServiceCollection.doService(reply);
        replyProducer.produce(reply);
        msg.addEvent(getReplyProducer(), true);
      }
      catch (ProduceException e) { // need to apply different event
        msg.addEvent(getReplyProducer(), false); // generate event
        handleBadMessage("Exception processing message", e, msg);
        handleProduceException();
      }
    }
    else { // i.e. reply has timed out
      log.info("request [" + msg.getUniqueId() + "] timed out");
    }
  }

  /**
   * @see com.adaptris.core.WorkflowImp#initialiseWorkflow()
   */
  @Override
  protected void initialiseWorkflow() throws CoreException {
    LifecycleHelper.init(replyProducer);
    LifecycleHelper.init(replyServiceCollection);
    super.initialiseWorkflow();
  }

  /**
   * @see com.adaptris.core.WorkflowImp#startWorkflow()
   */
  @Override
  protected void startWorkflow() throws CoreException {
    LifecycleHelper.start(replyProducer);
    LifecycleHelper.start(replyServiceCollection);
    super.startWorkflow();
  }

  /**
   * @see com.adaptris.core.WorkflowImp#stopWorkflow()
   */
  @Override
  protected void stopWorkflow() {
    super.stopWorkflow();

    LifecycleHelper.stop(replyServiceCollection);
    LifecycleHelper.stop(replyProducer);

  }

  /**
   * @see com.adaptris.core.WorkflowImp#closeWorkflow()
   */
  @Override
  protected void closeWorkflow() {
    super.closeWorkflow();
    LifecycleHelper.close(replyServiceCollection);
    LifecycleHelper.close(replyProducer);
  }

  @Override
  public void registerChannel(Channel c) throws CoreException {
    super.registerChannel(c);
    c.getConsumeConnection().addMessageProducer(getReplyProducer());
  }


  /**
   * <p>
   * Sets the <code>ServiceCollection</code> to use on the reply. May not be null.
   * </p>
   *
   * @param services the <code>ServiceCollection</code> to use on the reply
   */
  public void setReplyServiceCollection(ServiceCollection services) {
    if (services == null) {
      throw new IllegalArgumentException("param [" + services + "]");
    }
    replyServiceCollection = services;
  }

  /**
   * <p>
   * Returns the <code>ServiceCollection</code> to use on the reply.
   * </p>
   *
   * @return the <code>ServiceCollection</code> to use on the reply
   */
  public ServiceCollection getReplyServiceCollection() {
    return replyServiceCollection;
  }

  /**
   * <p>
   * Sets the <code>AdaptrisMessageProducer</code> to use for the reply. NB uses the workflows <b>consume</b> connection (it's a
   * reply...). May not be null.
   * </p>
   *
   * @param producer the <code>AdaptrisMessageProducer</code> to use for the reply
   */
  public void setReplyProducer(AdaptrisMessageProducer producer) {
    if (producer == null) {
      throw new IllegalArgumentException("param [" + producer + "]");
    }
    replyProducer = producer;
  }

  /**
   * <p>
   * Returns the <code>AdaptrisMessageProducer</code> to use for the reply.
   * </p>
   *
   * @return the <code>AdaptrisMessageProducer</code> to use for the reply
   */
  public AdaptrisMessageProducer getReplyProducer() {
    return replyProducer;
  }

  public TimeInterval getReplyTimeout() {
    return replyTimeout;
  }

  /**
   * Set the time to wait for a reply.
   *
   * @param replyTimeout the time to wait for a reply.
   */
  public void setReplyTimeout(TimeInterval replyTimeout) {
    this.replyTimeout = replyTimeout;
  }

  long replyTimeout() {
    return getReplyTimeout() != null ? getReplyTimeout().toMilliseconds() : DEFAULT_REPLY_TIMEOUT.toMilliseconds();
  }

  /** Set whether the reply message should retain the request message id.
   *
   * @param b true or false
   */
  public void setRetainUniqueId(Boolean b) {
    retainUniqueId = b;
  }


  public Boolean getRetainUniqueId() {
    return retainUniqueId;
  }

  boolean retainUniqueId() {
    return getRetainUniqueId() != null ? getRetainUniqueId().booleanValue() : false;
  }

  @Override
  protected void prepareWorkflow() throws CoreException {
    getReplyProducer().prepare();
    getReplyServiceCollection().prepare();
  }
}
