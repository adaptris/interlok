package com.adaptris.core.jms;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.DestinationHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@XStreamAlias("jms-sync-consumer")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from a JMS broker (queue or topic) by actively polling it", tag = "consumer,jms", recommended = {
    JmsConnection.class })
@DisplayOrder(order = { "endpoint", "messageSelector", "poller", "acknowledgeMode", "messageTranslator" })
public class JmsSyncConsumer extends BaseJmsPollingConsumerImpl {

  /**
   * Set to true if you wish to let the JMS message consumer be delegated by the configured vendor implementation.
   * <p>
   * The default is false such that we use standard JMS 1.1/2.0 methods to create the appropriate consumers.
   * </p>
   */
  @AdvancedConfig(rare = true)
  @AutoPopulated
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean deferConsumerCreationToVendor;

  /**
   * The JMS destination in RFC6167 format.
   *
   */
  @NotBlank
  @NonNull
  @Getter
  @Setter
  private String endpoint;

  public JmsSyncConsumer() {
    super();
  }

  public JmsSyncConsumer withEndpoint(String s) {
    setEndpoint(s);
    return this;
  }

  @Override
  public Logger currentLogger() {
    return log;
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
    super.init();
    try {
      initSession();
      initConsumer();
      configuredMessageTranslator().registerSession(currentSession());
      configuredMessageTranslator().registerMessageFactory(defaultIfNull(getMessageFactory()));
      LifecycleHelper.init(configuredMessageTranslator());
    } catch (JMSException e) {
      throw new CoreException(e);
    }
  }

  @Override
  protected MessageConsumer createConsumer() throws JMSException {
    String rfc6167 = endpoint();
    String filterExp = messageSelector();

    VendorImplementation vendor = retrieveConnection(JmsConnection.class).configuredVendorImplementation();
    return new JmsMessageConsumerFactory(vendor, currentSession(), rfc6167, deferConsumerCreationToVendor(), filterExp, this)
        .create();
  }

  @Override
  protected int processMessages() {
    String oldName = renameThread();
    int count = doProcessMessage();
    Thread.currentThread().setName(oldName);
    return count;
  }

  @Override
  protected void prepareConsumer() throws CoreException {
  }

  protected String messageSelector() {
    return getMessageSelector();
  }

  protected String endpoint() {
    return getEndpoint();
  }

  @Override
  protected String newThreadName() {
    return DestinationHelper.threadName(retrieveAdaptrisMessageListener(), null);
  }

  @Override
  protected Session createSession(int acknowledgeMode, boolean transacted) throws JMSException {
    return retrieveConnection(JmsConnection.class).createSession(isTransacted(), AcknowledgeMode.getMode(getAcknowledgeMode()));
  }

  protected Boolean deferConsumerCreationToVendor() {
    return BooleanUtils.toBooleanDefaultIfNull(getDeferConsumerCreationToVendor(), false);
  }

}
