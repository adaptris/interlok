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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.ManagedThreadFactory;
import com.adaptris.util.NumberUtils;
import com.adaptris.util.TimeInterval;

/**
 * <p>
 * Contains behaviour common to implementations of <code>EventHandler</code>.
 * </p>
 */
public abstract class EventHandlerBase implements EventHandler {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  private static final int DEFAULT_SHUTDOWN_WAIT = 60;

  @Valid
  @AdvancedConfig
  private AdaptrisMarshaller marshaller;
  @Valid
  @AdvancedConfig
  private AdaptrisMessageFactory messageFactory;
  private String uniqueId;
  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "60")
  private Integer shutdownWaitSeconds;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean logAllExceptions;

  protected transient EventEmissary eventProducerDelegate;
  private transient ComponentState state;
  private transient String sourceId;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   */
  public EventHandlerBase() {
    changeState(ClosedState.getInstance());
    eventProducerDelegate = new EventEmissary();
  }

  public abstract AdaptrisMessageSender retrieveProducer() throws CoreException;

  private AdaptrisMessage createMessage(Event evt, Map<String, String> metadata) throws CoreException {
    evt.setSourceId(retrieveSourceId());
    Map<String, String> metadataToUse = metadata == null ? new HashMap<>() : metadata;
    AdaptrisMessage result = currentMessageFactory().newMessage(currentMarshaller().marshal(evt), new MetadataCollection(metadataToUse).toSet());
    result.setUniqueId(evt.getUniqueId());
    result.addMetadata(CoreConstants.EVENT_NAME_SPACE_KEY, evt.getNameSpace());
    result.addMetadata(CoreConstants.EVENT_CLASS, evt.getClass().getName());
    return result;
  }

  @Override
  public void send(Event evt) throws CoreException {
    this.send(evt, (Map) null);
  }

  @Override
  public void send(Event evt, Map<String, String> properties) throws CoreException {
    eventProducerDelegate.produce(retrieveProducer(), createMessage(evt, properties));
  }

  /**
   * Set the {@link AdaptrisMarshaller} implementation to use when sending events.
   *
   * @param m the implementation to use, if null then {@link DefaultMarshaller#getDefaultMarshaller()} is used.
   */
  public void setMarshaller(AdaptrisMarshaller m) {
    marshaller = m;
  }

  public AdaptrisMarshaller getMarshaller() {
    return marshaller;
  }

  public AdaptrisMarshaller currentMarshaller() {
    return DefaultMarshaller.defaultIfNull(getMarshaller());
  }

  /** @see com.adaptris.core.EventHandler#registerSourceId(java.lang.String) */
  @Override
  public void registerSourceId(String s) {
    sourceId = s;
  }

  @Override
  public String retrieveSourceId() {
    return sourceId;
  }

  public AdaptrisMessageFactory currentMessageFactory() throws CoreException {
    return getMessageFactory() != null ? getMessageFactory() : AdaptrisMessageFactory.getDefaultInstance();
  }

  /**
   * @return the messageFactory
   */
  public AdaptrisMessageFactory getMessageFactory() {
    return messageFactory;
  }

  /**
   * Set the message factory used when creating AdaptrisMessage instances.
   *
   * @param f the messageFactory to set
   */
  public void setMessageFactory(AdaptrisMessageFactory f) {
    messageFactory = f;
  }

  /**
   * @see com.adaptris.core.StateManagedComponent#retrieveComponentState()
   */
  @Override
  public ComponentState retrieveComponentState() {
    return state;
  }

  public Integer getShutdownWaitSeconds() {
    return shutdownWaitSeconds;
  }

  public int shutdownWaitSeconds() {
    return NumberUtils.toIntDefaultIfNull(getShutdownWaitSeconds(), DEFAULT_SHUTDOWN_WAIT);
  }

  /**
   * Set the number of seconds to wait when shutting down any internal threads.
   *
   * @param i the number of seconds, default if not specified is 60
   */
  public void setShutdownWaitSeconds(Integer i) {
    shutdownWaitSeconds = i;
  }

  /**
   *
   * @see com.adaptris.core.StateManagedComponent#getUniqueId()
   */
  @Override
  public String getUniqueId() {
    return StringUtils.defaultIfEmpty(uniqueId, this.getClass().getSimpleName());
  }

  /**
   *
   * @see com.adaptris.core.StateManagedComponent#getUniqueId()
   */
  public void setUniqueId(String s) {
    uniqueId = s;
  }

  @Override
  public void changeState(ComponentState c) {
    state = c;
  }

  @Override
  public final void init() throws CoreException {
    LifecycleHelper.init(eventProducerDelegate);
    eventHandlerInit();
  }

  protected abstract void eventHandlerInit() throws CoreException;

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(eventProducerDelegate);
    eventHandlerStart();
  }

  protected abstract void eventHandlerStart() throws CoreException;

  protected abstract void eventHandlerStop();

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    LifecycleHelper.stop(eventProducerDelegate);
    eventHandlerStop();
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    LifecycleHelper.close(eventProducerDelegate);
    eventHandlerClose();
  }

  protected abstract void eventHandlerClose();

  /**
   * @return the logAllExceptions
   */
  public Boolean getLogAllExceptions() {
    return logAllExceptions;
  }

  /**
   * @param logAllExceptions the logAllExceptions to set
   */
  public void setLogAllExceptions(Boolean logAllExceptions) {
    this.logAllExceptions = logAllExceptions;
  }

  private boolean logAllExceptions() {
    return BooleanUtils.toBooleanDefaultIfNull(getLogAllExceptions(), false);
  }

  /**
   * @see com.adaptris.core.StateManagedComponent#requestInit()
   */
  @Override
  public void requestInit() throws CoreException {
    state.requestInit(this);
  }

  /**
   * @see com.adaptris.core.StateManagedComponent#requestStart()
   */
  @Override
  public void requestStart() throws CoreException {
    state.requestStart(this);
  }

  /**
   * @see com.adaptris.core.StateManagedComponent#requestStop()
   */
  @Override
  public void requestStop() {
    state.requestStop(this);
  }

  /**
   * @see com.adaptris.core.StateManagedComponent#requestClose()
   */
  @Override
  public void requestClose() {
    state.requestClose(this);
  }


  protected class EventEmissary implements ComponentLifecycle {
    private ExecutorService executor = null;

    protected EventEmissary() {
    }

    public void produce(final AdaptrisMessageSender producer, final AdaptrisMessage msg) {
      executor.execute(new Thread() {
        @Override
        public void run() {
          String name = Thread.currentThread().getName();
          Thread.currentThread().setName("EventProducerThread");
          String eventClass = msg.getMetadataValue(CoreConstants.EVENT_CLASS);
          try {
            producer.produce(msg);
          } catch (Exception e) {
            if (logAllExceptions()) {
              log.error("Failed to produce event [{}] to destination. Results dependent on this event may not be accurate.",
                eventClass, e);
            }
          }
          Thread.currentThread().setName(name);
        }
      });
    }

    @Override
    public void close() {
    }

    @Override
    public void init() throws CoreException {
    }

    @Override
    public void start() throws CoreException {
      executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void stop() {
      ManagedThreadFactory.shutdownQuietly(executor,
          new TimeInterval(Integer.valueOf(shutdownWaitSeconds()).longValue(), TimeUnit.SECONDS));
    }
  }

}
