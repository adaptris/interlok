package com.adaptris.core.services.splitter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.EventHandler;
import com.adaptris.core.EventHandlerAware;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCollection;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Splits incoming {@link AdaptrisMessage}s into several using an implementation of {@link MessageSplitter}.
 * </p>
 * <p>
 * Rather than directly producing the message to a producer, this allows the use of a {@link ServiceCollection} as the target for
 * the resulting split messages.
 * </p>
 * 
 * @config advanced-message-splitter-service
 * 
 * @license STANDARD
 */
@XStreamAlias("advanced-message-splitter-service")
public class AdvancedMessageSplitterService extends MessageSplitterServiceImp implements EventHandlerAware {

  @NotNull
  @AutoPopulated
  @Valid
  private Service service;
  private transient EventHandler eventHandler;
  private Boolean sendEvents;

  /**
   * <p>
   * Creates a new instance. Defaults to copying all metadata from the original
   * message to the new, split messages.
   * </p>
   */
  public AdvancedMessageSplitterService() {
    super();
    setService(new NullService());
  }

  /**
   *
   * @see com.adaptris.core.services.splitter.MessageSplitterServiceImp#handleSplitMessage(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void handleSplitMessage(AdaptrisMessage msg) throws ServiceException {
    try {
      service.doService(msg);
    }
    finally {
      if (eventHandler != null && sendEvents()) {
        try {
          eventHandler.send(msg.getMessageLifecycleEvent());
        }
        catch (CoreException e) {
          throw new ServiceException(e);
        }
      }
    }
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    LifecycleHelper.registerEventHandler(service, eventHandler);
    super.init();
    LifecycleHelper.init(service);
  }

  /** @see com.adaptris.core.AdaptrisComponent#start() */
  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(service);
    super.start();

  }

  /** @see com.adaptris.core.AdaptrisComponent#stop() */
  @Override
  public void stop() {
    LifecycleHelper.stop(service);
    super.stop();
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    LifecycleHelper.stop(service);
  }

  /**
   * @return the serviceList
   */
  public Service getService() {
    return service;
  }

  /**
   * @param sc the serviceList to set
   */
  public void setService(Service sc) {
    if (sc == null) {
      throw new IllegalArgumentException("service may not be null");
    }
    service = sc;
  }

  /**
   * @return the sendEvents
   */
  public Boolean getSendEvents() {
    return sendEvents;
  }

  public boolean sendEvents() {
    return getSendEvents() != null ? getSendEvents().booleanValue() : false;
  }

  /**
   * Whether or not to send events for the message that has been split.
   * <p>
   * Note that even if this is set to true, because each child message has its
   * own unique id, you will have to externally correlate the message lifecycle
   * events together. Child messages will always have the metadata
   * {@link CoreConstants#PARENT_UNIQUE_ID_KEY} set with the originating message
   * id.
   * </p>
   *
   * @param b true to send messages (default false)
   */
  public void setSendEvents(Boolean b) {
    sendEvents = b;
  }

  /**
   * @see com.adaptris.core.EventHandlerAware#registerEventHandler(com.adaptris.core.EventHandler)
   */
  public void registerEventHandler(EventHandler eh) {
    eventHandler = eh;
  }

  @Override
  public boolean isEnabled(License l) throws CoreException {
    return l.isEnabled(LicenseType.Standard) && getService().isEnabled(l);
  }
}
