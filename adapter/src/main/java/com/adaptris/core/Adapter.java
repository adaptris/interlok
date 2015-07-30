package com.adaptris.core;

import static com.adaptris.core.util.LoggingHelper.friendlyName;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.event.AdapterCloseEvent;
import com.adaptris.core.event.AdapterInitEvent;
import com.adaptris.core.event.AdapterStartEvent;
import com.adaptris.core.event.AdapterStopEvent;
import com.adaptris.core.event.LicenseExpiryWarningEvent;
import com.adaptris.core.event.StandardAdapterStartUpEvent;
import com.adaptris.core.runtime.MessageErrorDigester;
import com.adaptris.core.runtime.StandardMessageErrorDigester;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.license.License;
import com.adaptris.util.license.LicenseException;
import com.adaptris.util.license.LicenseFactory;
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
public final class Adapter implements StateManagedComponentContainer, ComponentLifecycleExtension {
  private static final TimeInterval DEFAULT_HB_EVENT_INTERVAL = new TimeInterval(15L, TimeUnit.MINUTES.name());

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @NotNull
  @NotBlank
  private String uniqueId;
  @NotNull
  @AutoPopulated
  @NotBlank
  private String startUpEventImp;
  @NotNull
  @AutoPopulated
  @NotBlank
  private String heartbeatEventImp;
  @NotNull
  @AutoPopulated
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
  private transient License license;
  private transient ComponentState state;
  private transient boolean hasInitialised;

  /**
   * <p>
   * Creates a new instance. Defaults to <code>EventHandlerImp</code>, <code>MessageErrorHandler</code>,
   * <code>DefaultFailedMessageRetrier</code>, <code>NullLogHandler</code> all of which do nothing. Uses
   * <code>DefaultAdapterStartUpEvent</code>, <code>HeartbeatEvent</code>, default heartbeat interval is 15 minutes.
   * </p>
   * 
   * @throws CoreException wrapping any underlying Exceptions
   */
  public Adapter() throws CoreException {
    lastStopTime = new Date();

    setEventHandler(new DefaultEventHandler());
    setMessageErrorHandler(new NullProcessingExceptionHandler());
    setMessageErrorDigester(new StandardMessageErrorDigester("ErrorDigest"));
    setFailedMessageRetrier(new NoRetries());
    setStartUpEventImp(StandardAdapterStartUpEvent.class.getName());
    setLogHandler(new NullLogHandler());
    setHeartbeatEventImp(HeartbeatEvent.class.getName());
    setChannelList(new ChannelList());
    setSharedComponents(new SharedComponentList());
    state = ClosedState.getInstance();
    try {
      license = LicenseFactory.create(null);
    }
    catch (LicenseException e) {
    }
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
    getSharedComponents().prepare();
    checkLicense();
    try {
      getLogHandler().clean();
    }
    catch (IOException i) {
      log.warn("ignoring exception cleaning log files", i);
    }
    eventHandler.registerSourceId(getUniqueId());
    // eventHandler.requestStart();
    for (Channel c : getChannelList()) {
      LifecycleHelper.registerEventHandler(c, getEventHandler());
    }
    channelList.prepare();
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
   * @see AdaptrisComponent
   */
  @Override
  public void init() throws CoreException {
    try {
      prepare();
      initialise();
    }
    catch (RuntimeException e) {
      log.error("Caught un-handled RuntimeException", e);
      throw new CoreException(e);
    }

  }

  /**
   */
  private synchronized void initialise() throws CoreException {
    eventHandler.requestStart();
    try {
      LifecycleHelper.init(getSharedComponents());
      LifecycleHelper.init(getMessageErrorDigester());
      LifecycleHelper.init(messageErrorHandler);
      LifecycleHelper.init(channelList);
      LifecycleHelper.init(failedMessageRetrier);
      handleLifecycleEvent(AdapterInitEvent.class, true);
      generateLicenseExpiryEvent();
      sendStartUpEvent();
    }
    catch (CoreException e) {
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

  void generateLicenseExpiryEvent() throws CoreException {
    try {
      // LicenseExpiryWarning event sent?
      java.util.Calendar expiryWait = java.util.Calendar.getInstance();
      // Only send the license Expiry if it's in 2 weeks time.
      expiryWait.add(java.util.Calendar.WEEK_OF_YEAR, 2);
      if (license.getExpiry().after(expiryWait.getTime())) {
        return;
      }
      log.warn("License expires soon : {}", license.getExpiry());
      LicenseExpiryWarningEvent evt = EventFactory.create(LicenseExpiryWarningEvent.class);
      evt.setAdapterUniqueId(getUniqueId());
      evt.setExpiryDate(license.getExpiry());
      eventHandler.send(evt);
    }
    catch (LicenseException e) {
      // Technically, failure to generate this event is not an error.
      // as it's merely a warning about impending failure, not in fact
      // a failure in its own right.
      log.trace("ignoring Exception [{}]", e.getMessage());
    }
  }

  public final void checkLicense() throws CoreException {
    try {
      license.verify();
    }
    catch (LicenseException e) {
      log.error("License not valid for this adapter; verification failed with " + e.getMessage());
      throw new CoreException(e);
    }
    if (!isEnabled(license)) {
      log.error("Adapter configuration is incompatible with the configured license");
      throw new CoreException("Adapter configuration is incompatible with the configured license");
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#isEnabled (com.adaptris.util.license.License)
   */
  @Override
  public boolean isEnabled(License l) throws CoreException {
    return sharedComponents.isEnabled(l) && channelList.isEnabled(l);
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
      }
      catch (CoreException e) {
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
        }
        else {
          workflow.registerActiveMsgErrorHandler(errorHandlerToUse);
        }
      }
    }
  }

  private void warnOnErrorHandlerBehaviour(StateManagedComponent comp, ProcessingExceptionHandler h) {
    if (!h.hasConfiguredBehaviour()) {
      log.warn("[" + friendlyName(comp) + "] has a MessageErrorHandler with no behaviour; "
          + "messages may be discarded upon exception");
    }
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#start()
   */
  @Override
  public void start() throws CoreException {
    try {
      LifecycleHelper.start(getMessageErrorDigester());
      LifecycleHelper.start(messageErrorHandler);
      LifecycleHelper.start(channelList);
      LifecycleHelper.start(failedMessageRetrier);
      LifecycleHelper.start(getSharedComponents());

      startHeartbeat();
      handleLifecycleEvent(AdapterStartEvent.class, true);

      lastStartTime = new Date();
    }
    catch (CoreException e) {
      handleLifecycleEvent(AdapterStartEvent.class, false);
      throw e;
    }
    catch (RuntimeException e) {
      log.error("Caught un-handled RuntimeException", e);
      throw new CoreException(e);
    }
  }

  private void startHeartbeat() throws CoreException {
    try {
      @SuppressWarnings("unchecked")
      HeartbeatTimerTask task = new HeartbeatTimerTask((Class<HeartbeatEvent>) Class.forName(getHeartbeatEventImp()), this);
      heartbeatTimer = new Timer(true); // is daemon
      heartbeatTimer.schedule(task, heartbeatInterval(), heartbeatInterval());
    }
    catch (ClassNotFoundException e) {
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

  /** @see AdaptrisComponent */
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
    }
    catch (CoreException e) {
      log.trace("Failed to stop component cleanly, logging exception for informational purposes only", e);
    }
    LifecycleHelper.stop(getSharedComponents());
  }

  /** @see AdaptrisComponent */
  @Override
  public void close() {
    LifecycleHelper.close(failedMessageRetrier);
    LifecycleHelper.close(channelList);
    LifecycleHelper.close(messageErrorHandler);
    LifecycleHelper.close(getMessageErrorDigester());
    try {
      handleLifecycleEvent(AdapterCloseEvent.class, true);
    }
    catch (CoreException e) {
      log.trace("Failed to shutdown component cleanly, logging exception for informational purposes only", e);
    }
    LifecycleHelper.close(getSharedComponents());
    eventHandler.requestClose();
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
    if (param == null) {
      throw new IllegalArgumentException();
    }
    channelList = param;
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
    if (isEmpty(s)) {
      throw new IllegalArgumentException();
    }
    uniqueId = s;
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
    if (param == null) {
      throw new IllegalArgumentException();
    }
    eventHandler = param;
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
   * Sets the configured {@link ProcessingExceptionHandler} for the Adapter level. May not be null, but need not be configured at
   * this level.
   * </p>
   * 
   * @param param the <code>MessageErrorHandler</code> to use
   */
  public void setMessageErrorHandler(ProcessingExceptionHandler param) {

    if (param == null) {
      throw new IllegalArgumentException();
    }
    messageErrorHandler = param;
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
    if (isEmpty(name)) {
      throw new IllegalArgumentException();
    }
    startUpEventImp = name;
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
    if (isEmpty(name)) {
      throw new IllegalArgumentException();
    }
    heartbeatEventImp = name;
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
    return getHeartbeatEventInterval() != null ? getHeartbeatEventInterval().toMilliseconds() : DEFAULT_HB_EVENT_INTERVAL
        .toMilliseconds();
  }

  /**
   * <p>
   * Sets the <code>FailedMessageRetrier</code> to use. May not be null.
   * </p>
   * 
   * @param retrier the <code>FailedMessageRetrier</code> to use
   */
  public void setFailedMessageRetrier(FailedMessageRetrier retrier) {

    if (retrier == null) {
      throw new IllegalArgumentException();
    }
    failedMessageRetrier = retrier;
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
   * <p>
   * Sets the <code>License</code> to use. Must be public (for Boostrap). May not be null.
   * </p>
   * 
   * @param l the <code>License</code> to use
   */
  public void registerLicense(License l) {
    if (l == null) {
      throw new IllegalArgumentException("param [" + l + "]");
    }
//    log.trace("Registered License : " + l);
    license = l;
  }

  /**
   * <p>
   * Returns the <code>License</code> to use.
   * </p>
   * 
   * @return the <code>License</code> to use
   */
  public License currentLicense() {
    return license;
  }

  /**
   * Set the LogHandler implementation.
   * 
   * @param lh the log handler implementation.
   */
  public void setLogHandler(LogHandler lh) {
    if (lh == null) {
      throw new IllegalArgumentException();
    }
    logHandler = lh;
  }

  /**
   * Return the configured LogHandler.
   * 
   * @return the log handler.
   */
  public LogHandler getLogHandler() {
    return logHandler;
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

}
