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

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;
import static com.adaptris.core.jms.JmsConstants.JMS_ASYNC_STATIC_REPLY_TO;
import static com.adaptris.core.util.DestinationHelper.logWarningIfNotNull;
import static com.adaptris.core.util.DestinationHelper.mustHaveEither;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import java.util.Optional;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.util.DestinationHelper;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.interlok.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JMS Producer implementation that can target queues or topics via an RFC6167 style destination.
 * <p>
 * This differs from the standard {@link PtpProducer} and {@link PasProducer} in that it supports a
 * destination that is specified in RFC6167 style. For instance {@code jms:queue:myQueueName} will
 * produce to a queue called {@code myQueueName} and {@code jms:topic:myTopicName} to a topic called
 * {@code myTopicName}
 * </p>
 * <p>
 * While RFC6167 defines the ability to use jndi to lookup the (as part of the 'jndi' variant
 * section); this is not supported. The standard deliveryMode, timeToLive, priority, replyToName
 * properties are supported. If not specified, then they will be inherited from the producers
 * configuration. For instance you could have the following destinations:
 * <ul>
 * <li>jms:queue:MyQueueName</li>
 * <li>jms:topic:MyTopicName</li>
 * <li>jms:queue:MyQueueName?replyToName=StaticReplyTo&amp;priority=1&amp;timeToLive=1234</li>
 * <li>jms:topic:MyTopicName?replyToName=StaticReplyTo</li>
 * </ul>
 * </p>
 * <p>
 * As the RFC6167 string can specify priority, timeToLive and deliveryMode; this producer defaults
 * {@link #getPerMessageProperties()} to be true.
 * </p>
 *
 * @config jms-producer
 *
 */
@XStreamAlias("jms-producer")
@AdapterComponent
@ComponentProfile(summary = "Place message on a JMS queue or topic", tag = "producer,jms", recommended = {JmsConnection.class})
@DisplayOrder(order = {"endpoint", "destination", "messageTranslator", "deliveryMode",
    "priority", "ttl", "acknowledgeMode"})
@NoArgsConstructor
public class JmsProducer extends JmsProducerImpl {

  /**
   * The ProduceDestination is the JMS Endpoint to write to.
   * <p>
   * Note that this is deprecated, but you may need to use something like
   * {@link JmsReplyToDestination} until support is available directly using string expressions.
   * </p>
   */
  @Getter
  @Setter
  @Deprecated
  @Valid
  @Removal(version = "4.0.0", message = "Use 'endpoint' instead if possible")
  private ProduceDestination destination;

  /**
   * The JMS Endpoint defined in an RFC6167 manner.
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  // Needs to be @NotBlank when destination is removed.
  private String endpoint;

  private transient boolean destWarning;

  @Override
  public void prepare() throws CoreException {
    logWarningIfNotNull(destWarning, () -> destWarning = true, getDestination(),
        "{} uses destination, use 'endpoint' instead if possible",
        LoggingHelper.friendlyName(this));
    mustHaveEither(getEndpoint(), getDestination());
    super.prepare();
  }

  @Override
  @Deprecated
  @Removal(version = "4.0")
  public void produce(AdaptrisMessage msg, ProduceDestination dest) throws ProduceException {
    try {
      setupSession(msg);
      JmsDestination target = buildDestination(dest, msg, false);
      Args.notNull(target, "destination");
      produce(msg, target);
      commit();
    } catch (Exception e) {
      rollback();
      logLinkedException("", e);
      throw ExceptionHelper.wrapProduceException(e);
    }
  }

  protected void produce(AdaptrisMessage msg, JmsDestination jmsDest)
      throws JMSException, CoreException {
    setupSession(msg);
    Message jmsMsg = translate(msg, jmsDest.getReplyToDestination());
    if (!perMessageProperties()) {
      producerSession().getProducer().send(jmsDest.getDestination(), jmsMsg);
    } else {
      producerSession().getProducer().send(jmsDest.getDestination(), jmsMsg,
          calculateDeliveryMode(msg, jmsDest.deliveryMode()),
          calculatePriority(msg, jmsDest.priority()),
          calculateTimeToLive(msg, jmsDest.timeToLive()));
    }
    captureOutgoingMessageDetails(jmsMsg, msg);
    log.info("msg produced to destination [{}]", jmsDest);
  }

  @Override
  @Deprecated
  @Removal(version = "4.0")
  public AdaptrisMessage request(AdaptrisMessage msg, ProduceDestination dest, long timeout)
      throws ProduceException {

    AdaptrisMessage translatedReply = defaultIfNull(getMessageFactory()).newMessage();
    Destination replyTo = null;
    MessageConsumer receiver = null;
    try {
      setupSession(msg);
      JmsDestination target = buildDestination(dest, msg, true);
      replyTo = target.getReplyToDestination();
      // Listen for the reply.
      receiver = currentSession().createConsumer(replyTo);
      produce(msg, target);
      translatedReply = waitForReply(receiver, timeout);
      // BUG#915
      commit();
    } catch (Exception e) {
      logLinkedException("", e);
      rollback();
      throw ExceptionHelper.wrapProduceException(e);
    } finally {
      JmsUtils.closeQuietly(receiver);
      JmsUtils.deleteTemporaryDestination(replyTo);
    }
    return mergeReply(translatedReply, msg);
  }

  /**
   * Wait for a reply.
   *
   * @param receiver the {@code MessageConsumer}
   * @param timeout the timeout (ms)
   * @return an AdaptrisMessage translated by the configured MessageTranslator
   * @throws JMSException on Exception (including a timeout exception).
   */
  protected AdaptrisMessage waitForReply(MessageConsumer receiver, long timeout) throws JMSException {
    Message jmsReply = receiver.receive(timeout);
    AdaptrisMessage translatedReply =
        Optional.ofNullable(MessageTypeTranslatorImp.translate(getMessageTranslator(), jmsReply))
            .orElseThrow(() -> new JMSException("No Reply Received within " + timeout + "ms"));
    acknowledge(jmsReply);
    return translatedReply;
  }


  /**
   * Build a JMSDestination.
   *
   * @param dest the ProduceDestination
   * @param msg the message
   * @param createReplyTo - passed through to
   *        {@link #createReplyTo(AdaptrisMessage, JmsDestination, boolean)}
   * @return a JMSDestination instanced.
   */
  protected JmsDestination buildDestination(ProduceDestination dest, AdaptrisMessage msg, boolean createReplyTo)
      throws JMSException, CoreException {
    MyJmsDestination target = null;
    // First of all try and get a jms destination directory from the produce destination
    // (JmsReplyToDestination)
    Destination jmsDest = createDestination(dest, msg);
    if (jmsDest != null) {
      target = new MyJmsDestination(jmsDest);
    } else {
      String destString = dest.getDestination(msg);
      target = new MyJmsDestination(vendorImplementation().createDestination(destString, this));
      target.setReplyTo(createReplyTo(msg, target, createReplyTo));
    }
    return target;
  }


  /**
   * Create a Destination for JMSReplyTo if one doesn't already exist or if
   * {@code JMS_ASYNC_STATIC_REPLY_TO} exists as metadata.
   *
   * @param msg the message (which will be checked for {@code JMS_ASYNC_STATIC_REPLY_TO}.
   * @param createTmpDest - create a temporary destination if {@code JMS_ASYNC_STATIC_REPLY_TO}
   *        isn't available.
   * @return a javax.jms.Destination
   */
  protected Destination createReplyTo(AdaptrisMessage msg, JmsDestination target,
      boolean createTmpDest)
      throws JMSException {
    Destination replyTo = null;
    if (target.getReplyToDestination() == null) {
      if (msg.headersContainsKey(JMS_ASYNC_STATIC_REPLY_TO)) {
        replyTo = target.destinationType().create(vendorImplementation(), this,
            msg.getMetadataValue(JMS_ASYNC_STATIC_REPLY_TO));
      } else {
        replyTo = createTmpDest ? target.destinationType().createTemporaryDestination(currentSession()) : null;
      }
    } else {
      replyTo = target.getReplyToDestination();
    }
    return replyTo;
  }

  protected <T extends VendorImplementationBase> T vendorImplementation() {
    return retrieveConnection(JmsConnectionConfig.class).configuredVendorImplementation();
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException {
    ProduceDestination dest = Optional.ofNullable(getDestination())
        .orElse(new ConfiguredProduceDestination(endpoint(msg)));
    return request(msg, dest, timeout);
  }

  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    ProduceDestination dest = Optional.ofNullable(getDestination())
        .orElse(new ConfiguredProduceDestination(endpoint(msg)));
    produce(msg, dest);
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return DestinationHelper.resolveProduceDestination(getEndpoint(), getDestination(), msg);
  }

  @Override
  public boolean perMessageProperties() {
    return BooleanUtils.toBooleanDefaultIfNull(getPerMessageProperties(), true);
  }

  @SuppressWarnings("unchecked")
  public <T extends JmsProducer> T withEndpoint(String s) {
    setEndpoint(s);
    return (T) this;
  }

  protected class MyJmsDestination implements JmsDestination {

    private Destination destination;
    private String deliveryMode;
    private Destination replyTo;
    private Long timeToLive;
    private Integer priority;
    private String subscriptionId;
    private String sharedConsumerId;
    private boolean noLocal;
    private JmsDestination.DestinationType destType;

    private MyJmsDestination(JmsDestination orig) {
      destination = orig.getDestination();
      replyTo = orig.getReplyToDestination();
      deliveryMode = isEmpty(orig.deliveryMode()) ? getDeliveryMode() : orig.deliveryMode();
      timeToLive = orig.timeToLive() != null ? orig.timeToLive() : timeToLive();
      priority = orig.priority() != null ? orig.priority() : messagePriority();
      destType = orig.destinationType();
      noLocal = orig.noLocal();
      subscriptionId = orig.subscriptionId();
      sharedConsumerId = orig.sharedConsumerId();
    }

    private MyJmsDestination(Destination d) {
      destination = d;
      deliveryMode = getDeliveryMode();
      timeToLive = timeToLive();
      priority = messagePriority();
    }

    @Override
    public Destination getDestination() {
      return destination;
    }

    @Override
    public Destination getReplyToDestination() {
      return replyTo;
    }

    @Override
    public String deliveryMode() {
      return deliveryMode;
    }

    @Override
    public Long timeToLive() {
      return timeToLive;
    }

    @Override
    public Integer priority() {
      return priority;
    }

    private void setReplyTo(Destination replyTo) {
      this.replyTo = replyTo;
    }

    @Override
    public JmsDestination.DestinationType destinationType() {
      return destType;
    }

    @Override
    public String subscriptionId() {
      return subscriptionId;
    }

    @Override
    public boolean noLocal() {
      return noLocal;
    }

    @Override
    public String toString() {
      return getDestination().toString();
    }

    @Override
    public String sharedConsumerId() {
      return sharedConsumerId;
    }
  }

}
