/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adaptris.core;

import static com.adaptris.core.CoreConstants.UNIQUE_ID_JMX_PATTERN;
import static com.adaptris.core.util.LoggingHelper.friendlyName;
import static org.apache.commons.lang.StringUtils.isBlank;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.event.AdapterCloseEvent;
import com.adaptris.core.event.AdapterInitEvent;
import com.adaptris.core.event.AdapterStartEvent;
import com.adaptris.core.event.AdapterStopEvent;
import com.adaptris.core.event.StandardAdapterStartUpEvent;
import com.adaptris.core.runtime.MessageErrorDigester;
import com.adaptris.core.runtime.StandardMessageErrorDigester;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Main class in <code>core</code> package. Uses other <code>core</code> components to consume and produce messages. Provides error
 * and event handling capabilities. Client classes, e.g. <code>com.adaptris.core.management.Bootstrap</code> create and manage
 * instances of this class.
 * </p>
 *
 * @config adapter
 */
@XStreamAlias("adapter")
@AdapterComponent
@ComponentProfile(summary = "The base container for integration activity", tag = "base")
public final class Adapter implements StateManagedComponentContainer, ComponentLifecycleExtension {
  private static final TimeInterval DEFAULT_HB_EVENT_INTERVAL = new TimeInterval(15L, TimeUnit.MINUTES.name());

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  private static final LogHandler DEFAULT_LOG_HANDLER = new NullLogHandler();

  @NotNull
  @NotBlank
  @Pattern(regexp = UNIQUE_ID_JMX_PATTERN)
  private String uniqueId;
  @NotNull
  @AutoPopulated
  @NotBlank
  @AdvancedConfig
  private String startUpEventImp;
  @NotNull
  @AutoPopulated
  @NotBlank
  @AdvancedConfig
  private String heartbeatEventImp;
  @AdvancedConfig
  @Valid
  private LogHandler logHandler;
  @NotNull
  @AutoPopulated
  @Valid
  private SharedComponentList sharedComponents;
  @NotNull
  @AutoPopulated
  @Valid
  private EventHandler eventHandler;
  @Valid
  private TimeInterval heartbeatEventInterval;

  @NotNull
  @AutoPopulated
  @Valid
  private ProcessingExceptionHandler messageErrorHandler;
  @NotNull
  @AutoPopulated
  @Valid
  private FailedMessageRetrier failedMessageRetrier;
  @NotNull
  @AutoPopulated
  @Valid
  private ChannelList channelList;
  @NotNull
  @AutoPopulated
  @Valid
  private MessageErrorDigester messageErrorDigester;

  private transient Date lastStartTime;
  private transient Date lastStopTime;

  // not marshalled...
  private transient Timer heartbeatTimer;
  private transient ComponentState state;
  private transient boolean hasInitialised;

  public Adapter()  {
    lastStopTime = new Date();
    setEventHandler(new DefaultEventHandler());
    setMessageErrorHandler(new NullProcessingExceptionHandler());
    setMessageErrorDigester(new StandardMessageErrorDigester("ErrorDigest"));
    setFailedMessageRetrier(new NoRetries());
    setStartUpEventImp(StandardAdapterStartUpEvent.class.getName());
    setHeartbeatEventImp(HeartbeatEvent.class.getName());
    setChannelList(new ChannelList());
    setSharedComponents(new SharedComponentList());
    state = ClosedState.getInstance();

  }

  @Deprecated
  public void castorWorkAround() throws CoreException {
    prepare();
  }

  /**
   * <p>
   * Ensures that the adapter is ready for initialisation.
   * </p>
   *
   * @throws CoreException wrapping any underlying Exceptions
   */
  @Override
  public void prepare() throws CoreException {
    if (isBlank(uniqueId)) {
      throw new CoreException("invalid unique id [" + uniqueId + "]");
    }
    LifecycleHelper.prepare(getSharedComponents());
    try {
      logHandler().clean();
    } catch (IOException i) {
      log.warn("ignoring exception cleaning log files", i);
    }
    eventHandler.registerSourceId(getUniqueId());
    // eventHandler.requestStart();
    for (Channel c : getChannelList()) {
      LifecycleHelper.registerEventHandler(c, getEventHandler());
    }
    LifecycleHelper.prepare(getChannelList());
    getMessageErrorHandler().registerDigester(getMessageErrorDigester());
    injectErrorHandler();
    registerWorkflowsInRetrier();
  }

  /**
   * <p>
   * Initialise the adapter including the <code>EventHandler</code>. Implements method defined in <code>AdaptrisMBean</code>, hence
   * public.
   * </p>
   *
   * @see com.adaptris.core.AdaptrisComponent
   */
  @Override
  public void init() throws CoreException {
    try {
      prepare();
      initialise();
    } catch (RuntimeException e) {
      log.error("Caught un-handled RuntimeException", e);
      throw new CoreException(e);
    }
    log.info("{} Initialised", friendlyName(this));
  }

  /**
   */
  private synchronized void initialise() throws CoreException {
    eventHandler.requestStart();
    List<ComponentLifecycle> toInit = Arrays.asList((ComponentLifecycle) getSharedComponents(), getMessageErrorDigester(),
        getMessageErrorHandler(), getChannelList(), getFailedMessageRetrier());
    try {
      for (ComponentLifecycle c : toInit) {
        LifecycleHelper.init(c);
      }
      handleLifecycleEvent(AdapterInitEvent.class, true);
      sendStartUpEvent();
    } catch (CoreException e) {
      log.error("Failed to initialise Adapter; closing components");
      for (ComponentLifecycle c : reverse(toInit)) {
        LifecycleHelper.close(c);
      }
      handleLifecycleEvent(AdapterInitEvent.class, false);
      throw e;
    }
  }

  /**
   * <p>
   * Sets the adapter unique ID on the passed event then sends it.
   * </p>
   */
  private void handleLifecycleEvent(Class<?> evtImp, boolean wasSuccessful) throws CoreException {
    AdapterLifecycleEvent evt = (AdapterLifecycleEvent) EventFactory.create(evtImp);
    evt.setAdapterUniqueId(getUniqueId());
    evt.setWasSuccessful(wasSuccessful);
    eventHandler.send(evt);
  }



  private void registerWorkflowsInRetrier() {
    // We should clear the workflows, because
    // It is possible via JMX to initialise twice in succession
    // which will screw up the retrier.
    failedMessageRetrier.clearWorkflows();
    for (Channel channel : channelList) {
      try {
        for (Workflow workflow : channel.getWorkflowList()) {
          failedMessageRetrier.addWorkflow(workflow);
        }
      } catch (CoreException e) {
        log.warn("Workflows cannot be uniquely identified at the Adapter" + " level, FailedMessageRetrier reset to null");
        setFailedMessageRetrier(new NoRetries());
        break; // out of Channel loop
      }
    }
    log.debug("FailedMessageRetrier {}", failedMessageRetrier.registeredWorkflowIds());
  }

  private void injectErrorHandler() {
    warnOnErrorHandlerBehaviour(this, messageErrorHandler);
    LifecycleHelper.registerEventHandler(getMessageErrorHandler(), getEventHandler());

    for (Channel channel : channelList) {
      ProcessingExceptionHandler errorHandlerToUse = messageErrorHandler;
      if (channel.getMessageErrorHandler() != null) {
        errorHandlerToUse = channel.getMessageErrorHandler();
        warnOnErrorHandlerBehaviour(channel, errorHandlerToUse);
        errorHandlerToUse.registerParent(messageErrorHandler);
        // LifecycleHelper.registerEventHandler(errorHandlerToUse, getEventHandler());
      }
      channel.registerActiveMsgErrorHandler(errorHandlerToUse);
      for (Workflow workflow : channel.getWorkflowList()) {
        if (workflow.getMessageErrorHandler() != null) {
          warnOnErrorHandlerBehaviour(workflow, workflow.getMessageErrorHandler());
          workflow.registerActiveMsgErrorHandler(workflow.getMessageErrorHandler());
          // LifecycleHelper.registerEventHandler(workflow.getMessageErrorHandler(), getEventHandler());
          workflow.getMessageErrorHandler().registerParent(channel.retrieveActiveMsgErrorHandler());
        } else {
          workflow.registerActiveMsgErrorHandler(errorHandlerToUse);
        }
      }
    }
  }

  private void warnOnErrorHandlerBehaviour(StateManagedComponent comp, ProcessingExceptionHandler h) {
    if (!h.hasConfiguredBehaviour()) {
      log.warn("[{}] has a MessageErrorHandler with no behaviour; messages may be discarded upon exception", friendlyName(comp));
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#start()
   */
  @Override
  public void start() throws CoreException {

    List<ComponentLifecycle> toStart =
        Arrays.asList(getMessageErrorDigester(), getMessageErrorHandler(), getChannelList(), getFailedMessageRetrier(),
            (ComponentLifecycle) getSharedComponents());

    try {
      for (ComponentLifecycle c : toStart) {
        LifecycleHelper.start(c);
      }
      startHeartbeat();
      handleLifecycleEvent(AdapterStartEvent.class, true);
      lastStartTime = new Date();
      log.info("{} Started", friendlyName(this));
    } catch (CoreException | RuntimeException e) {
      log.error("Failed to start Adapter; stopping components");
      for (ComponentLifecycle c : reverse(toStart)) {
        LifecycleHelper.stop(c);
      }
      handleLifecycleEvent(AdapterStartEvent.class, false);
      ExceptionHelper.rethrowCoreException(e);
    }
  }

  private void startHeartbeat() throws CoreException {
    try {
      @SuppressWarnings("unchecked")
      HeartbeatTimerTask task = new HeartbeatTimerTask((Class<HeartbeatEvent>) Class.forName(getHeartbeatEventImp()), this);
      heartbeatTimer = new Timer(true); // is daemon
      heartbeatTimer.schedule(task, heartbeatInterval(), heartbeatInterval());
    } catch (ClassNotFoundException e) {
      throw new CoreException(e);
    }
  }

  // nb called from synch'd method only
  private void sendStartUpEvent() throws CoreException {
    if (!hasInitialised) {
      AdapterStartUpEvent evt = (AdapterStartUpEvent) EventFactory.create(startUpEventImp);

      evt.setAdapterUniqueId(getUniqueId());
      evt.setAdapter(this);

      eventHandler.send(evt);

      hasInitialised = true;
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent
   */
  @Override
  public void stop() {
    lastStopTime = new Date();

    log.info("stopping adapter [{}] if there are polling loops configured this may take some time", getUniqueId());
    if (heartbeatTimer != null) {
      heartbeatTimer.cancel();
    }
    LifecycleHelper.stop(failedMessageRetrier);
    LifecycleHelper.stop(channelList);
    LifecycleHelper.stop(messageErrorHandler);
    LifecycleHelper.stop(getMessageErrorDigester());
    try {
      handleLifecycleEvent(AdapterStopEvent.class, true);
    } catch (CoreException e) {
      log.trace("Failed to stop component cleanly, logging exception for informational purposes only", e);
    }
    LifecycleHelper.stop(getSharedComponents());
    log.info("{} Stopped", friendlyName(this));
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent
   */
  @Override
  public void close() {
    LifecycleHelper.close(failedMessageRetrier);
    LifecycleHelper.close(channelList);
    LifecycleHelper.close(messageErrorHandler);
    LifecycleHelper.close(getMessageErrorDigester());
    try {
      handleLifecycleEvent(AdapterCloseEvent.class, true);
    } catch (CoreException e) {
      log.trace("Failed to shutdown component cleanly, logging exception for informational purposes only", e);
    }
    LifecycleHelper.close(getSharedComponents());
    eventHandler.requestClose();
    log.info("{} Closed", friendlyName(this));
  }

  // Castor gets & sets...

  /**
   * <p>
   * Sets the <code>ChannelList</code>.
   * </p>
   *
   * @param param the <code>ChannelList</code> to use.
   */
  public void setChannelList(ChannelList param) {
    channelList = Args.notNull(param, "channelList");
  }

  /**
   * <p>
   * Returns the <code>ChannelList</code>.
   * </p>
   *
   * @return the <code>ChannelList</code>
   */
  public ChannelList getChannelList() {
    return channelList;
  }

  /**
   * <p>
   * Sets this instance's unique id. Unique ids are implementation-specific.
   * </p>
   *
   * @param s the unique id to set
   */
  public void setUniqueId(String s) {
    uniqueId = Args.notBlank(s, "uniqueId");
  }

  /**
   * <p>
   * Returns this instances unique id.
   * </p>
   *
   * @return this instances unique id
   */
  @Override
  public String getUniqueId() {
    return uniqueId;
  }

  /**
   * <p>
   * Sets the <code>EventHandler</code>.
   * </p>
   *
   * @param param the <code>EventHandler</code> to use.
   */
  public void setEventHandler(EventHandler param) {
    eventHandler = Args.notNull(param, "eventHandler");
    eventHandler.registerSourceId(getUniqueId());
  }

  /**
   * <p>
   * Returns the <code>EventHandler</code>.
   * </p>
   *
   * @return the <code>EventHandler</code>
   */
  public EventHandler getEventHandler() {
    return eventHandler;
  }

  /**
   * <p>
   * Sets the configured {@link com.adaptris.core.ProcessingExceptionHandler} for the Adapter level. May not be null, but need not
   * be configured at
   * this level.
   * </p>
   *
   * @param param the <code>MessageErrorHandler</code> to use
   */
  public void setMessageErrorHandler(ProcessingExceptionHandler param) {
    messageErrorHandler = Args.notNull(param, "messageErrorHandler");
  }

  /**
   * <p>
   * Returns the configured <code>MessageErrorHandler</code>.
   * </p>
   *
   * @return the configured <code>MessageErrorHandler</code>
   */
  public ProcessingExceptionHandler getMessageErrorHandler() {
    return messageErrorHandler;
  }

  /**
   * <p>
   * Sets the name of the start up event class to use. May not be null.
   * </p>
   *
   * @param name the name of the start up event class to use
   */
  public void setStartUpEventImp(String name) {
    startUpEventImp = Args.notBlank(name, "startupEventImp");
  }

  /**
   * <p>
   * Returns the name of the start up event class to use.
   * </p>
   *
   * @return the name of the start up event class to use
   */
  public String getStartUpEventImp() {
    return startUpEventImp;
  }

  /**
   * <p>
   * Sets the class name of the heartbeat event imp to use.
   * </p>
   *
   * @param name the class name of the heartbeat event imp to use
   */
  public void setHeartbeatEventImp(String name) {
    heartbeatEventImp = Args.notBlank(name, "heartbeatEventImp");
  }

  /**
   * <p>
   * Returns the class name of the heartbeat event imp to use.
   * </p>
   *
   * @return the class name of the heartbeat event imp to use
   */
  public String getHeartbeatEventImp() {
    return heartbeatEventImp;
  }

  long heartbeatInterval() {
    return TimeInterval.toMillisecondsDefaultIfNull(getHeartbeatEventInterval(),
        DEFAULT_HB_EVENT_INTERVAL);
  }

  /**
   * <p>
   * Sets the <code>FailedMessageRetrier</code> to use. May not be null.
   * </p>
   *
   * @param retrier the <code>FailedMessageRetrier</code> to use
   */
  public void setFailedMessageRetrier(FailedMessageRetrier retrier) {
    failedMessageRetrier = Args.notNull(retrier, "failedMessageRetrier");
  }

  /**
   * <p>
   * Returns the <code>FailedMessageRetrier</code> to use.
   * </p>
   *
   * @return the <code>FailedMessageRetrier</code> to use
   */
  public FailedMessageRetrier getFailedMessageRetrier() {
    return failedMessageRetrier;
  }

  /**
   * Set the LogHandler implementation.
   *
   * @param lh the log handler implementation.
   */
  public void setLogHandler(LogHandler lh) {
    logHandler = Args.notNull(lh, "logHandler");
  }

  /**
   * Return the configured LogHandler.
   *
   * @return the log handler.
   */
  public LogHandler getLogHandler() {
    return logHandler;
  }

  public LogHandler logHandler() {
    return getLogHandler() != null ? getLogHandler() : DEFAULT_LOG_HANDLER;
  }

  public TimeInterval getHeartbeatEventInterval() {
    return heartbeatEventInterval;
  }

  /**
   * Set the event between which heartbeat events are emitted.
   *
   * @param interval
   */
  public void setHeartbeatEventInterval(TimeInterval interval) {
    heartbeatEventInterval = interval;
  }

  @Override
  public synchronized void requestInit() throws CoreException {
    state.requestInit(this);
  }

  @Override
  public synchronized void requestStart() throws CoreException {
    state.requestStart(this);
  }

  @Override
  public synchronized void requestStop() {
    state.requestStop(this);
  }

  @Override
  public synchronized void requestClose() {
    state.requestClose(this);
  }

  @Override
  public ComponentState retrieveComponentState() {
    return state;
  }

  @Override
  public void changeState(ComponentState newState) {
    state = newState;
  }

  @Override
  public void requestChildInit() throws CoreException {
    channelList.init();
  }

  @Override
  public void requestChildStart() throws CoreException {
    channelList.start();
  }

  @Override
  public void requestChildStop() {
    channelList.stop();
  }

  @Override
  public void requestChildClose() {
    channelList.close();
  }

  public SharedComponentList getSharedComponents() {
    return sharedComponents;
  }

  /**
   * Get the shared components that are available for injection into other managed components.
   *
   * @param scl the shared components.
   */
  public void setSharedComponents(SharedComponentList scl) {
    sharedComponents = scl;
  }

  public MessageErrorDigester getMessageErrorDigester() {
    return messageErrorDigester;
  }

  public void setMessageErrorDigester(MessageErrorDigester messageErrorDigest) {
    messageErrorDigester = messageErrorDigest;
  }

  /**
   * The last time the adapter was started
   *
   * @return the last time the adapter was started
   */
  public Date lastStartTime() {
    return lastStartTime;
  }

  /**
   * The last time the adapter was stopped
   *
   * @return the last time the adapter was stopped
   */
  public Date lastStopTime() {
    return lastStopTime;
  }


  static <T> ReverseIterator<T> reverse(List<T> original) {
    return new ReverseIterator<T>(original);
  }

  private static class ReverseIterator<T> implements Iterable<T> {
    private final List<T> original;

    public ReverseIterator(List<T> original) {
      this.original = original;
    }

    @Override
    public Iterator<T> iterator() {
      final ListIterator<T> i = original.listIterator(original.size());

      return new Iterator<T>() {
        @Override
        public boolean hasNext() {
          return i.hasPrevious();
        }

        @Override
        public T next() {
          return i.previous();
        }

        @Override
        public void remove() {
          i.remove();
        }
      };
    }
  }
}
