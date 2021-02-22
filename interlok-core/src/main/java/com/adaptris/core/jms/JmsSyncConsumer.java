package com.adaptris.core.jms;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.validation.constraints.NotBlank;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

/**
 * JMS synchronous consumer implementation that can target queues or topics via an
 * RFC6167 style endpoint.
 * <p>
 * For instance
 * {@code jms:queue:myQueueName} will consume from a queue called {@code myQueueName} and
 * {@code jms:topic:myTopicName} from a topic called {@code myTopicName}
 * </p>
 * <p>
 * While RFC6167 defines the ability to use jndi to lookup the (as part of the 'jndi' variant section); this is not supported. There
 * is also support for {@code subscriptionId} which indicates the subscriptionId that should be used when attaching a subscriber to
 * a topic; {@code jms:topic:MyTopicName?subscriptionId=myId} would return a {@link JmsDestination#subscriptionId()} of
 * {@code myId}. If a subscription ID is not specified, then a durable subscriber is never created; specifying a subscription ID
 * automatically means a durable subscriber.
 * </p>
 * <p>
 * Also supported is the JMS 2.0 sharedConsumerId, should you wish to create a multiple load balancing consumers on a single topic endpoint;
 * {@code jms:topic:MyTopicName?sharedConsumerId=12345}
 * </p>
 * For instance you could have the following destinations:
 * <ul>
 * <li>jms:queue:MyQueueName</li>
 * <li>jms:topic:MyTopicName</li>
 * <li>jms:topic:MyTopicName?subscriptionId=mySubscriptionId</li>
 * <li>jms:topic:MyTopicName?sharedConsumerId=mySharedConsumerId</li>
 * <li>jms:topic:MyTopicName?subscriptionId=mySubscriptionId&sharedConsumerId=mySharedConsumerId</li>
 * </ul>
 * </p>
 *
 * @config jms-poller
 *
 */
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
   * The RFC6167 format topic/queue.
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
    return retrieveAdaptrisMessageListener().friendlyName();
  }

  @Override
  protected Session createSession(int acknowledgeMode, boolean transacted) throws JMSException {
    return retrieveConnection(JmsConnection.class).createSession(isTransacted(), AcknowledgeMode.getMode(getAcknowledgeMode()));
  }

  protected Boolean deferConsumerCreationToVendor() {
    return BooleanUtils.toBooleanDefaultIfNull(getDeferConsumerCreationToVendor(), false);
  }

}
