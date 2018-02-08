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

import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION;
import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE;
import static com.adaptris.core.CoreConstants.UNIQUE_ID_JMX_PATTERN;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.PlainIdGenerator;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Partial implementation of <code>Workflow</code>.
 *
 * @see StandardWorkflow
 * @see PoolingWorkflow
 */
public abstract class WorkflowImp implements Workflow {
  private static final TimeInterval DEFAULT_CHANNEL_UNAVAILBLE_WAIT = new TimeInterval(30L, TimeUnit.SECONDS);
  private static final String ID_SEPARATOR = "@";

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @NotNull
  @AutoPopulated
  @Valid
  private AdaptrisMessageConsumer consumer;
  @NotNull
  @AutoPopulated
  @Valid
  private ServiceCollection serviceCollection; // could be Service
  @NotNull
  @AutoPopulated
  @Valid
  private AdaptrisMessageProducer producer;

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean disableDefaultMessageCount;

  // Could be null; if it is, then the real one gets injected in later.
  @AdvancedConfig
  @Valid
  private ProcessingExceptionHandler messageErrorHandler; // configured
  // private EventHandler eventHandler;

  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean sendEvents;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean logPayload;
  @NotNull
  @AutoPopulated
  @Valid
  @AdvancedConfig
  private ProduceExceptionHandler produceExceptionHandler;
  @NotNull
  @NotBlank
  @Pattern(regexp = UNIQUE_ID_JMX_PATTERN)
  private String uniqueId;
  @Valid
  @XStreamImplicit
  @NotNull
  @AutoPopulated
  private List<WorkflowInterceptor> interceptors;

  @Valid
  @AdvancedConfig
  private TimeInterval channelUnavailableWaitInterval;

  // not marshalled
  private transient Channel channel;
  private transient ProcessingExceptionHandler activeErrorHandler;
  private transient ComponentState state;
  protected transient EventHandler eventHandler;
  protected transient Date startTime;
  protected transient Date stopTime;
  private transient boolean prepared = false;

  /**
   * <p>
   * Creates a new instance with defaults to prevent NullPointerExceptions.
   * </p>
   */
  public WorkflowImp() {
    stopTime = new Date();

    // default...
    setConsumer(new NullMessageConsumer());
    setServiceCollection(new ServiceList());
    setProducer(new NullMessageProducer());

    // default configured MEH is in Adapter, this is MEHToUse...
    registerActiveMsgErrorHandler(new NullProcessingExceptionHandler());

    setProduceExceptionHandler(new NullProduceExceptionHandler());
    setInterceptors(new ArrayList<WorkflowInterceptor>());
    state = ClosedState.getInstance();
  }

  /**
   * <p>
   * Sets the <code>MessageErrorHandler</code> to use for handling error messages. This may be configured at the Workflow, Channel
   * or Adapter level. May not be null.
   * </p>
   *
   * @param meh the <code>MessageErrorHandler</code> to use
   */
  @Override
  public void registerActiveMsgErrorHandler(ProcessingExceptionHandler meh) {
    activeErrorHandler = Args.notNull(meh, "activeErrorHandler");
    meh.registerWorkflow(this);
  }

  @Override
  public void registerEventHandler(EventHandler eh) {
    eventHandler = Args.notNull(eh, "eventHandler");
  }

  @Override
  public final void prepare() throws CoreException {
    LifecycleHelper.registerEventHandler(getProducer(), eventHandler);
    LifecycleHelper.registerEventHandler(getConsumer(), eventHandler);
    LifecycleHelper.registerEventHandler(getServiceCollection(), eventHandler);
    for (WorkflowInterceptor wi : getInterceptors()) {
      wi.registerParentChannel(obtainChannel());
      wi.registerParentWorkflow(this);
      wi.prepare();
    }
    getProducer().prepare();
    getConsumer().prepare();
    getServiceCollection().prepare();
    prepareWorkflow();
    prepared = true;
  }

  protected abstract void prepareWorkflow() throws CoreException;

  /**
   * <p>
   * Because the order in which concrete workflows may need to init their components, this method simply ensures that the
   * <code>MessageErrorHandler
   * </code> is inited, and then delegates all other init requirements to the concrete implementation.
   *
   * @see com.adaptris.core.AdaptrisComponent#init()
   * @see #initialiseWorkflow()
   * @throws CoreException encapsulating any underlying Exception
   */
  @Override
  public final void init() throws CoreException {
    if (!prepared) {
      prepare();
    }
    if (!channel.retrieveActiveMsgErrorHandler().equals(activeErrorHandler)) {
      LifecycleHelper.registerEventHandler(activeErrorHandler, eventHandler);
      LifecycleHelper.init(activeErrorHandler);
    }
    for (WorkflowInterceptor wi : getInterceptors()) {
      LifecycleHelper.init(wi);
    }
    initialiseWorkflow();
  }

  /**
   * Initialise the workflow.
   *
   * @throws CoreException encapsulating any underlying Exception
   */
  protected abstract void initialiseWorkflow() throws CoreException;

  /**
   * Start this component.
   * <p>
   * Because the order in which concrete workflows may need to start their components, this method simply ensures that the
   * <code>MessageErrorHandler
   * </code> is started, and then delegates to the concrete imp.
   *
   * @see com.adaptris.core.AdaptrisComponent#start()
   * @see #startWorkflow()
   * @throws CoreException encapsulating any underlying Exception
   */
  @Override
  public final void start() throws CoreException {
    if (!channel.retrieveActiveMsgErrorHandler().equals(activeErrorHandler)) {
      LifecycleHelper.start(activeErrorHandler);
    }
    for (WorkflowInterceptor wi : getInterceptors()) {
      LifecycleHelper.start(wi);
    }

    startWorkflow();
    startTime = new Date();
  }

  /**
   * Start the workflow.
   *
   * @see com.adaptris.core.AdaptrisComponent#start()
   * @throws CoreException encapsulating any underlying Exception
   */
  protected abstract void startWorkflow() throws CoreException;

  /**
   * Stop this component.
   * <p>
   * Because the order in which concrete workflows may need to stop their components, this method simply uses
   * <code>stopWorkflow</code> to stop the concrete workflow, and then ensures that the MessageErrorHandler is closed.
   *
   * @see com.adaptris.core.AdaptrisComponent#stop()
   * @see #stopWorkflow()
   */
  @Override
  public final void stop() {
    stopTime = new Date();
    stopWorkflow();
    if (!channel.retrieveActiveMsgErrorHandler().equals(activeErrorHandler)) {
      LifecycleHelper.stop(activeErrorHandler);
    }
    for (WorkflowInterceptor wi : getInterceptors()) {
      LifecycleHelper.stop(wi);
    }

  }

  /**
   * Stop the workflow.
   */
  protected abstract void stopWorkflow();

  /**
   * Close this component.
   * <p>
   * Because the order in which concrete workflows may need to close their components, this method delegates all other close
   * requirements to the concrete implementation, after the concrete workflow has performed its close it ensures that the
   * <code>MessageErrorHandler</code> is closed.
   *
   * @see com.adaptris.core.AdaptrisComponent#close()
   * @see #closeWorkflow()
   */
  @Override
  public final void close() {
    closeWorkflow();
    if (!channel.retrieveActiveMsgErrorHandler().equals(activeErrorHandler)) {
      LifecycleHelper.close(activeErrorHandler);

    }
    for (WorkflowInterceptor wi : getInterceptors()) {
      LifecycleHelper.close(wi);
    }
  }

  /**
   * Close the workflow.
   */
  protected abstract void closeWorkflow();

  @Override
  public void changeState(ComponentState s) {
    state = s;
  }

  @Override
  public void requestInit() throws CoreException {
    state.requestInit(this);
  }

  @Override
  public void requestStart() throws CoreException {
    state.requestStart(this);
  }

  @Override
  public void requestStop() {
    state.requestStop(this);
  }

  @Override
  public void requestClose() {
    state.requestClose(this);
  }

  @Override
  public ComponentState retrieveComponentState() {
    return state;
  }

  /**
   * Accessor to allow sub-classes access to the <code>MessageErrorHandler</code> that is in use.
   *
   * @return the message error handler in use.
   */
  protected ProcessingExceptionHandler retrieveActiveMsgErrorHandler() {
    return activeErrorHandler;
  }

  /**
   * Allows common functionality when the channel is unavailable.
   *
   * @param msg the current message.
   */
  protected void handleChannelUnavailable(AdaptrisMessage msg) {
    log.debug("channel unavailable, waiting to resubmit...");

    LifecycleHelper.waitQuietly(channelUnavailableWait());

    if (obtainChannel().isAvailable()) {
      log.debug("Channel now available, resubmitting...");
      resubmitMessage(msg);
    }
    else {
      log.debug("Channel still not available...");
      handleBadMessage(msg);
    }
  }

  /**
   * Resubmit a message upon the channel becoming available again.
   *
   * @param msg the AdaptrisMessage.
   */
  protected abstract void resubmitMessage(AdaptrisMessage msg);

  /** @see com.adaptris.core.Workflow#handleProduceException() */
  @Override
  public void handleProduceException() {
    produceExceptionHandler.handle(this);
  }

  /**
   * <p>
   * This method contains the behaviour that varies between standard and request -reply workflows. It is overridden in
   * <code>RequestReplyWorkflow</code>.
   * </p>
   *
   * @param msg the message to process
   * @throws ProduceException if any occur
   * @throws ServiceException not thrown by this implementation
   */
  @Override
  public void doProduce(AdaptrisMessage msg) throws ServiceException, ProduceException {
    if (!Boolean.valueOf(msg.getMetadataValue(CoreConstants.KEY_WORKFLOW_SKIP_PRODUCER)).booleanValue()) {
      producer.produce(msg);
      msg.addEvent(producer, true);
    }
    else {
      log.debug("Skipping message producer, " + CoreConstants.KEY_WORKFLOW_SKIP_PRODUCER + " set to true");
    }
  }

  /**
   * @see com.adaptris.core.Workflow#handleBadMessage(AdaptrisMessage)
   */
  @Override
  public void handleBadMessage(AdaptrisMessage msg) {
    try {
      log.debug("handling bad message");
      msg.addMetadata(WORKFLOW_ID_KEY, obtainWorkflowId());
      // remove old previous guid if one exists...
      if (msg.getMetadata(PREVIOUS_GUID_KEY) != null) {
        msg.removeMetadata(msg.getMetadata(PREVIOUS_GUID_KEY));
      }
      msg.addMetadata(PREVIOUS_GUID_KEY, msg.getUniqueId());
      activeErrorHandler.handleProcessingException(msg);
    }
    catch (Exception e) { // unlikely runtime Exc.
      log.error("exception handling bad message [" + msg.toString(true) + "]", e);
    }
  }

  protected void sendMessageLifecycleEvent(AdaptrisMessage wip) {
    try {
      if (sendEvents()) { // eventH guaranteed not null
        eventHandler.send(wip.getMessageLifecycleEvent());
      }
    }
    catch (Exception e) {
      log.warn("failed to produce MessageLifecycleEvent for this message, though the message itself was successful : "
          + e.getMessage());
      log.trace("logging exception for informational purposes only", e);

    }
  }

  @Override
  public String obtainWorkflowId() {

    StringBuffer result = new StringBuffer();
    if (!channel.hasUniqueId()) {
      result.append(getConsumer().getClass().getName());
      result.append(ID_SEPARATOR);
      result.append(getProducer().getClass().getName());
      result.append(ID_SEPARATOR);
      result.append(generateId());
    }
    else {
      result.append(generateId());
      result.append(ID_SEPARATOR);
      result.append(channel.getUniqueId());
    }
    return result.toString();
  }

  private String generateId() {
    if (!isBlank(uniqueId)) {
      return uniqueId;
    }
    else {
      if (consumer.getDestination() != null) {
        // log.warn("Consumer.getDestination() != null " +
        // consumer.getDestination());
        uniqueId = consumer.getDestination().getUniqueId();
      }
      else {
        log.warn("consumer has no destination, you may not be able to retry messages that fail in this workflow.");
        uniqueId = new PlainIdGenerator().create(this);
      }
    }
    return uniqueId;
  }

  @Override
  public String friendlyName() {
    return obtainWorkflowId();
  }

  /**
   * <p>
   * Sets the <code>ServiceCollection</code> to use. May not be null.
   * </p>
   *
   * @param services the <code>ServiceCollection</code> to use
   */
  public void setServiceCollection(ServiceCollection services) {
    serviceCollection = Args.notNull(services, "service");
  }

  /**
   * <p>
   * Returns the <code>ServiceCollection</code> to use.
   * </p>
   *
   * @return the <code>ServiceCollection</code> to use
   */
  public ServiceCollection getServiceCollection() {
    return serviceCollection;
  }

  /**
   * <p>
   * Sets the <code>AdaptrisMessageConsumer</code> to use. May not be null. Sets the unique ID of this Workflow based on the
   * consumer's destination's unique ID. If there is no destination (e.g. from a <code>NullMessageConsumer</code>) , ID is set to
   * "default".
   * </p>
   *
   * @param param the <code>AdaptrisMessageConsumer</code> to use
   */
  public void setConsumer(AdaptrisMessageConsumer param) {
    consumer = Args.notNull(param, "consumer");
  }

  /**
   * <p>
   * Returns the <code>AdaptrisMessageConsumer</code> to use.
   * </p>
   *
   * @return the <code>AdaptrisMessageConsumer</code> to use
   */
  @Override
  public AdaptrisMessageConsumer getConsumer() {
    return consumer;
  }

  /**
   * <p>
   * Sets the <code>AdaptrisMessageProducer</code> to use. May not be null.
   * </p>
   *
   * @param param the <code>AdaptrisMessagePRoducer</code> to use
   */
  public void setProducer(AdaptrisMessageProducer param) {
    producer = Args.notNull(param, "producer");
  }

  /**
   * <p>
   * Returns the <code>AdaptrisMessageProducer</code> to use.
   * </p>
   *
   * @return the <code>AdaptrisMessageProducer</code> to use
   */
  @Override
  public AdaptrisMessageProducer getProducer() {
    return producer;
  }

  /**
   * <p>
   * Sets a configured <code>MessageErrorHandler</code>. Will over-ride any MEH configured at the Channel or Workflow level. May be
   * null.
   * </p>
   *
   * @param errorHandler the configured <code>MessageErrorHandler</code>
   */
  public void setMessageErrorHandler(ProcessingExceptionHandler errorHandler) {
    messageErrorHandler = errorHandler;
  }

  /**
   * <p>
   * Returns the <code>MessageErrorHandler</code> to use.
   * </p>
   *
   * @return the <code>MessageErrorHandler</code> to use
   */
  @Override
  public ProcessingExceptionHandler getMessageErrorHandler() {
    return messageErrorHandler;
  }

  /**
   * <p>
   * Sets whether events should be sent.
   * </p>
   *
   * @param events whether events should be sent; default is null (true).
   */
  public void setSendEvents(Boolean events) {
    sendEvents = events;
  }

  /**
   * <p>
   * Return whether events should be sent.
   * </p>
   *
   * @return whether events should be sent
   */
  public Boolean getSendEvents() {
    return sendEvents;
  }

  boolean sendEvents() {
    return BooleanUtils.toBooleanDefaultIfNull(getSendEvents(), true);
  }

  /**
   * <p>
   * Returns true if payload should be logged.
   * </p>
   *
   * @return true if payload should be logged
   */
  public Boolean getLogPayload() {
    return logPayload;
  }

  /**
   * <p>
   * Sets whether payload should be logged.
   * </p>
   *
   * @param b true if payload should be logged
   */
  public void setLogPayload(Boolean b) {
    logPayload = b;
  }

  boolean logPayload() {
    return BooleanUtils.toBooleanDefaultIfNull(getLogPayload(), false);
  }

  /**
   * @see com.adaptris.core.Workflow#obtainChannel()
   */
  @Override
  public Channel obtainChannel() {
    return channel;
  }

  /**
   * @see com.adaptris.core.Workflow#registerChannel(com.adaptris.core.Channel)
   */
  @Override
  public void registerChannel(Channel ch) throws CoreException {
    channel = Args.notNull(ch, "parentChannel");
    channel.getConsumeConnection().addMessageConsumer(getConsumer());
    channel.getProduceConnection().addMessageProducer(getProducer());
  }

  /**
   * <p>
   * Get the time the {@link Workflow} implementation will wait if its parent {@link com.adaptris.core.Channel} is unavailable before resubmitting the
   * message.
   * </p>
   *
   * return the time it will wait.
   */
  public TimeInterval getChannelUnavailableWaitInterval() {
    return channelUnavailableWaitInterval;
  }

  /**
   * <p>
   * Sets the time the {@link Workflow} implementation will wait if its parent {@link com.adaptris.core.Channel} is unavailable before resubmitting
   * the message.
   * </p>
   *
   * @param channelUnavailableWaitInterval the time
   */
  public void setChannelUnavailableWaitInterval(TimeInterval channelUnavailableWaitInterval) {
    this.channelUnavailableWaitInterval = channelUnavailableWaitInterval;
  }

  public long channelUnavailableWait() {
    return getChannelUnavailableWaitInterval() != null
        ? getChannelUnavailableWaitInterval().toMilliseconds()
            : DEFAULT_CHANNEL_UNAVAILBLE_WAIT.toMilliseconds();
  }

  /**
   * <p>
   * Returns produceExceptionHandler.
   * </p>
   *
   * @return produceExceptionHandler
   */
  public ProduceExceptionHandler getProduceExceptionHandler() {
    return produceExceptionHandler;
  }

  /**
   * <p>
   * Sets produceExceptionHandler.
   * </p>
   *
   * @param p the produceExceptionHandler to set
   */
  public void setProduceExceptionHandler(ProduceExceptionHandler p) {
    produceExceptionHandler = Args.notNull(p, "produceExceptionHandler");

  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(String id) {
    uniqueId = id;
  }

  @Override
  public List<WorkflowInterceptor> getInterceptors() {
    return interceptors;
  }

  public void setInterceptors(List<WorkflowInterceptor> list) {
    interceptors = Args.notNull(list, "interceptors");
  }

  public void addInterceptor(WorkflowInterceptor wi) {
    interceptors.add(Args.notNull(wi, "interceptor"));
  }

  /**
   * Mark the workflow having started processing on a message.
   *
   * @param msg the input message
   * @see WorkflowInterceptor
   */
  protected void workflowStart(AdaptrisMessage msg) {
    for (WorkflowInterceptor i : getInterceptors()) {
      i.workflowStart(msg);
    }
  }

  /**
   * Mark the workflow as finished on this message.
   *
   * @param input the input message
   * @param output the message that was produced.
   * @see WorkflowInterceptor
   */
  protected void workflowEnd(AdaptrisMessage input, AdaptrisMessage output) {
    for (WorkflowInterceptor i : getInterceptors()) {
      i.workflowEnd(input, output);
    }
  }

  /**
   * Handle the message in a standard way
   *
   * @param msg the message.
   * @param clone whether or not to attempt a msg.clone() preserving the original for error handling purposes.
   * @see com.adaptris.core.lms.LargeMessageWorkflow
   */
  protected void handleMessage(final AdaptrisMessage msg, boolean clone) {
    AdaptrisMessage wip = msg;
    workflowStart(msg);
    try {
      long start = System.currentTimeMillis();
      log.debug("start processing msg [" + msg.toString(logPayload()) + "]");
      if (clone) {
        wip = (AdaptrisMessage) msg.clone(); // retain orig. for error handling
      }
      wip.getMessageLifecycleEvent().setChannelId(obtainChannel().getUniqueId());
      wip.getMessageLifecycleEvent().setWorkflowId(obtainWorkflowId());
      wip.addEvent(getConsumer(), true); // initial receive event
      getServiceCollection().doService(wip);
      doProduce(wip);
      logSuccess(msg, start);
    }
    catch (ServiceException e) {
      handleBadMessage("Exception from ServiceCollection", e, copyExceptionHeaders(wip, msg));
    }
    catch (ProduceException e) {
      wip.addEvent(getProducer(), false); // generate event
      handleBadMessage("Exception producing msg", e, copyExceptionHeaders(wip, msg));
      handleProduceException();
    }
    catch (Exception e) { // all other Exc. inc. runtime
      handleBadMessage("Exception processing message", e, copyExceptionHeaders(wip, msg));
    }
    finally {
      sendMessageLifecycleEvent(wip);
    }
    workflowEnd(msg, wip);
  }

  protected AdaptrisMessage copyExceptionHeaders(AdaptrisMessage workingCopy, AdaptrisMessage orig) {
    if (workingCopy != orig) {
      Map<Object, Object> working = workingCopy.getObjectHeaders();
      if (working.get(OBJ_METADATA_EXCEPTION) != null)
        orig.addObjectHeader(OBJ_METADATA_EXCEPTION, working.get(OBJ_METADATA_EXCEPTION));
      if (working.get(OBJ_METADATA_EXCEPTION_CAUSE) != null)
        orig.addObjectHeader(OBJ_METADATA_EXCEPTION_CAUSE, working.get(OBJ_METADATA_EXCEPTION_CAUSE));
    }
    return orig;
  }

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

  protected void logSuccess(AdaptrisMessage msg, long start) {
    log.info("message [{}] processed in [{}] ms", msg.getUniqueId(), (System.currentTimeMillis() - start));
  }

  /**
   * Get the last time this workflow was started
   *
   * @return workflow start time
   */
  @Override
  public Date lastStartTime() {
    return startTime;
  }

  /**
   * Get the last time this channel was stopped. This is set when the channel is initialised so it may have been subsequently
   * started.
   *
   * @return channel stop time
   */
  @Override
  public Date lastStopTime() {
    return stopTime;
  }

  public Boolean getDisableDefaultMessageCount() {
    return disableDefaultMessageCount;
  }

  /**
   * Disable the default {@link com.adaptris.core.interceptor.MessageMetricsInterceptor} that keeps a message count.
   * <p>
   * If the workflow has a unique-id, and there are no {@link com.adaptris.core.interceptor.MessageMetricsInterceptor} instances
   * configured on the workflow, one
   * is created with the same name as the workflow with a default configured
   * {@link com.adaptris.core.interceptor.MessageMetricsInterceptor#setTimesliceDuration(com.adaptris.util.TimeInterval)} of 1
   * minute
   * </p>
   *
   * @param b true to disable, default null (false);
   * @since 3.0.3
   */
  public void setDisableDefaultMessageCount(Boolean b) {
    this.disableDefaultMessageCount = b;
  }

  @Override
  public boolean disableMessageCount() {
    return getDisableDefaultMessageCount() != null ? getDisableDefaultMessageCount().booleanValue() : false;
  }
}
