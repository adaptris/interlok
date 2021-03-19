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
import static com.adaptris.core.jms.JmsConstants.JMS_DELIVERY_MODE;
import static com.adaptris.core.jms.JmsConstants.JMS_EXPIRATION;
import static com.adaptris.core.jms.JmsConstants.JMS_PRIORITY;
import static com.adaptris.core.jms.NullCorrelationIdSource.defaultIfNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerBase;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.NumberUtils;

public abstract class JmsProducerImpl extends RequestReplyProducerBase implements JmsActorConfig {

  // This is used to track the current message id, for the session factory.
  // There doesn't appear to be a good way of doing this, everything *depends* on currentSession()
  // which means
  // that we need execute setupSession multiple times...
  private transient String CURRENT_MESSAGE_ID = "";
  private static final int DEFAULT_PRIORITY = 4;

  private static final String EXPIRATION_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
  private static final String EXPIRATION_DATE_REGEXP =
      "^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.*$";

  @NotNull
  @AutoPopulated
  @Pattern(regexp = "AUTO_ACKNOWLEDGE|CLIENT_ACKNOWLEDGE|DUPS_OK_ACKNOWLEDGE|[0-9]+")
  @AdvancedConfig
  private String acknowledgeMode;
  @NotNull
  @AutoPopulated
  @Valid
  private MessageTypeTranslator messageTranslator;
  @Valid
  @AdvancedConfig
  private CorrelationIdSource correlationIdSource;

  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "false")
  private Boolean captureOutgoingMessageDetails;

  @NotNull
  @AutoPopulated
  @Pattern(regexp = "PERSISTENT|NON_PERSISTENT|[0-9]+")
  @AdvancedConfig
  private String deliveryMode;

  @Min(0)
  @Max(9)
  @AdvancedConfig
  @InputFieldDefault(value = "4")
  private Integer priority;
  @Min(0)
  @AdvancedConfig
  @InputFieldDefault(value = "0")
  private Long ttl;

  @AdvancedConfig(rare = true)
  @InputFieldDefault(value = "false")
  private Boolean perMessageProperties;
  @AutoPopulated
  @NotNull
  @Valid
  @AdvancedConfig
  private ProducerSessionFactory sessionFactory;


  private transient ProducerSession producerSession;

  private transient Boolean transactedSession;
  private transient long rollbackTimeout = 30000;

  private enum ExpirationConverter {
    Milliseconds {
      @Override
      Date convert(String s) {
        return new Date(Long.parseLong(s));
      }

      @Override
      boolean convertable(String s) {
        boolean result = false;
        try {
          Long.parseLong(s);
          result = true;
        } catch (NumberFormatException e) {
        }
        return result;
      }
    },
    ISO8601 {
      @Override
      Date convert(String s) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(EXPIRATION_DATE_FORMAT);
        return sdf.parse(s);
      }

      @Override
      boolean convertable(String s) {
        return s.matches(EXPIRATION_DATE_REGEXP);
      }
    };
    abstract Date convert(String s) throws ParseException;

    abstract boolean convertable(String s);
  };


  public JmsProducerImpl() {
    // defaults
    setAcknowledgeMode(AcknowledgeMode.Mode.CLIENT_ACKNOWLEDGE.name());
    setDeliveryMode(DeliveryMode.Mode.PERSISTENT.name());
    setMessageTranslator(new TextMessageTranslator());
    setSessionFactory(new DefaultProducerSessionFactory());
  }

  @Override
  public void prepare() throws CoreException {
    LifecycleHelper.prepare(getMessageTranslator());
    LifecycleHelper.prepare(getSessionFactory());
  }

  @Override
  public void init() throws CoreException {
    messageTranslator.registerMessageFactory(defaultIfNull(getMessageFactory()));
    LifecycleHelper.init(messageTranslator);
    LifecycleHelper.init(getSessionFactory());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getMessageTranslator());
    LifecycleHelper.stop(getSessionFactory());
    CURRENT_MESSAGE_ID = "";
    producerSession = null;
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getMessageTranslator());
    LifecycleHelper.start(getSessionFactory());
  }

  @Override
  public void close() {
    LifecycleHelper.close(getMessageTranslator());
    LifecycleHelper.close(getSessionFactory());
  }

  @Override
  protected long defaultTimeout() {
    return 0L;
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException {
    return request(msg, defaultTimeout());
  }

  protected ProducerSession setupSession(AdaptrisMessage msg) throws JMSException {
    if (!msg.getUniqueId().equals(CURRENT_MESSAGE_ID) || producerSession == null) {
      producerSession = getSessionFactory().createProducerSession(this, msg);
      configuredMessageTranslator().registerSession(producerSession.getSession());
      CURRENT_MESSAGE_ID = msg.getUniqueId();
    }
    return producerSession;
  }

  protected void logLinkedException(String prefix, Exception e) {
    if (!(e instanceof JMSException))
      return;
    JMSException je = (JMSException) e;
    currentLogger().warn("JMSException caught [{}], [{}]", StringUtils.defaultIfEmpty(prefix, ""), e.getMessage());
    if (je.getLinkedException() != null) {
      currentLogger().trace("Linked Exception available...");
      currentLogger().trace(je.getLinkedException().getMessage(), je.getLinkedException());
    } else {
      currentLogger().trace("No Linked Exception available");
    }
  }

  protected Destination retrieveObjectDestination(String endpoint, AdaptrisMessage msg) throws CoreException {
    Object resolveObject = msg.resolveObject(endpoint);
    return resolveObject instanceof Destination ? (Destination) resolveObject : null;
  }

  protected int calculateDeliveryMode(AdaptrisMessage msg, String defaultDeliveryMode) {
    int deliveryMode;
    if (msg.headersContainsKey(JMS_DELIVERY_MODE)) {
      deliveryMode = DeliveryMode.getMode(msg.getMetadataValue(JMS_DELIVERY_MODE));
    } else {
      deliveryMode = DeliveryMode.getMode(defaultDeliveryMode);
    }
    currentLogger().trace("deliveryMode overridden to be {}", deliveryMode);
    return deliveryMode;
  }


  protected long calculateTimeToLive(AdaptrisMessage msg, Long defaultTTL)
      throws JMSException {
    long ttl = NumberUtils.toLongDefaultIfNull(defaultTTL, 0);
    try {
      if (msg.headersContainsKey(JMS_EXPIRATION)) {
        Date expiration = new Date();
        String value = msg.getMetadataValue(JMS_EXPIRATION);
        for (ExpirationConverter c : ExpirationConverter.values()) {
          if (c.convertable(value)) {
            expiration = c.convert(value);
            break;
          }
        }
        currentLogger().trace("Expiration Date from metadata is " + expiration);
        ttl = expiration.getTime() - System.currentTimeMillis();
        if (ttl < 0) {
          currentLogger().trace("TTL calculated as negative number, using configured ttl");
          ttl = NumberUtils.toLongDefaultIfNull(defaultTTL, 0);
        }
      }
    } catch (ParseException e) {
      JmsUtils.rethrowJMSException(e);
    }
    currentLogger().trace("Time to live overridden to be " + ttl);
    return ttl;
  }


  protected Message translate(AdaptrisMessage msg, Destination replyTo) throws JMSException {
    Message result = configuredMessageTranslator().translate(msg);
    configuredCorrelationIdSource().processCorrelationId(msg, result);
    if (replyTo != null) { // OpenJMS is fussy about null here
      result.setJMSReplyTo(replyTo);
    }
    return result;
  }

  protected int calculatePriority(AdaptrisMessage msg, Integer defaultPriority) {
    int priority = NumberUtils.toIntDefaultIfNull(defaultPriority, DEFAULT_PRIORITY);

    if (msg.headersContainsKey(JMS_PRIORITY)) {
      priority = Integer.parseInt(msg.getMetadataValue(JMS_PRIORITY));
    }
    currentLogger().trace("Priority overridden to be {}", priority);
    return priority;
  }

  /**
   * <p>
   * Returns the JMS delivery mode.
   * </p>
   *
   * @return the JMS delivery mode
   */
  public String getDeliveryMode() {
    return deliveryMode;
  }

  /**
   * <p>
   * Sets the JMS delivery mode.
   * </p>
   * <p>
   * The value may be either "PERSISENT", "NON_PERSISTENT", or the int corresponding to the
   * javax.jms.DeliveryMode constant.
   *
   * @param i the JMS delivery mode
   */
  public void setDeliveryMode(String i) {
    deliveryMode = i;
  }

  /**
   * <p>
   * Returns the JMS priority.
   * </p>
   *
   * @return the JMS priority
   */
  public Integer getPriority() {
    return priority;
  }

  /**
   * <p>
   * Sets the JMS priority. Valid values are 0 to 9.
   * </p>
   *
   * @param i the JMS priority
   */
  public void setPriority(Integer i) {
    priority = i;
  }

  public int messagePriority() {
    return NumberUtils.toIntDefaultIfNull(getPriority(), DEFAULT_PRIORITY);
  }

  /**
   * <p>
   * Returns the time to live. 0 means live forever.
   * </p>
   *
   * @return the time to live
   */
  public Long getTtl() {
    return ttl;
  }

  public long timeToLive() {
    return NumberUtils.toLongDefaultIfNull(getTtl(), 0);
  }

  /**
   * <p>
   * Sets the time to live.
   * </p>
   *
   * @param l the time to live
   */
  public void setTtl(Long l) {
    ttl = l;
  }

  /**
   * <p>
   * Sets the <code>MessageTypeTranslator</code> to use.
   * </p>
   *
   * @param translator the <code>MessageTypeTranslator</code> to use
   */
  public void setMessageTranslator(MessageTypeTranslator translator) {
    messageTranslator = Args.notNull(translator, "messageTranslator");
  }

  /**
   * <p>
   * Returns the <code>MessageTypeTranslator</code> to use.
   * </p>
   *
   * @return the <code>MessageTypeTranslator</code> to use
   */
  public MessageTypeTranslator getMessageTranslator() {
    return messageTranslator;
  }

  /**
   * <p>
   * Sets the JMS acknowledge mode.
   * </p>
   * <p>
   * The value may be AUTO_KNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE or the int values
   * corresponding to the JMS Session Constant
   * </p>
   */
  public void setAcknowledgeMode(String s) {
    acknowledgeMode = s;
  }

  /**
   * <p>
   * Returns the JMS acknowledge mode.
   * </p>
   *
   * @return the JMS acknowledge mode
   */
  public String getAcknowledgeMode() {
    return acknowledgeMode;
  }

  /**
   * <p>
   * Returns correlationIdSource.
   * </p>
   *
   * @return correlationIdSource
   */
  public CorrelationIdSource getCorrelationIdSource() {
    return correlationIdSource;
  }

  /**
   * <p>
   * Sets correlationIdSource.
   * </p>
   *
   * @param c the correlationIdSource to set
   */
  public void setCorrelationIdSource(CorrelationIdSource c) {
    correlationIdSource = c;
  }

  /**
   * @return the perMessageProperties
   */
  public Boolean getPerMessageProperties() {
    return perMessageProperties;
  }

  /**
   * Specify message properties per message rather than per producer.
   * <p>
   * If set to true, then each message that is produced can have its own individual time-to-live,
   * priority and delivery mode. These properties are taken from the producer's configuration but
   * can be overriden via metadata.
   * </p>
   *
   * @see JmsConstants#JMS_PRIORITY
   * @see JmsConstants#JMS_DELIVERY_MODE
   * @see JmsConstants#JMS_EXPIRATION
   * @param b the perMessageProperties to set
   */
  public void setPerMessageProperties(Boolean b) {
    perMessageProperties = b;
  }

  public boolean perMessageProperties() {
    return BooleanUtils.toBooleanDefaultIfNull(getPerMessageProperties(), false);
  }

  // BUG#915
  public void commit() throws JMSException {
    if (currentSession().getTransacted()) {
      currentLogger().trace("Committing transacted session");
      currentSession().commit();
    }
  }

  // BUG#915
  public void rollback() {
    boolean tryRollback = false;
    try {
      tryRollback = currentSession().getTransacted();
    } catch (JMSException f) {
      // session is probably broken, can't rollback anyway.
    }
    if (tryRollback) {
      try {
        currentLogger().trace("Attempting to rollback transacted session");
        currentSession().rollback();
      } catch (JMSException f) {
        currentLogger().trace("Error encountered rolling back transaction : {}", f.getMessage());
      }
    }
  }

  protected ProducerSession producerSession() {
    return producerSession;
  }

  protected void acknowledge(Message msg) throws JMSException {
    if (msg == null) {
      return;
    }
    if (configuredAcknowledgeMode() != Session.AUTO_ACKNOWLEDGE
        && !currentSession().getTransacted()) {
      msg.acknowledge();
    }
  }

  @Override
  public CorrelationIdSource configuredCorrelationIdSource() {
    return defaultIfNull(getCorrelationIdSource());
  }

  @Override
  public MessageTypeTranslator configuredMessageTranslator() {
    return getMessageTranslator();
  }

  @Override
  public int configuredAcknowledgeMode() {
    return AcknowledgeMode.getMode(getAcknowledgeMode());
  }

  @Override
  public AdaptrisMessageListener configuredMessageListener() {
    throw new UnsupportedOperationException("No Message Listener associated with a producer");
  }

  @Override
  public Session currentSession() {
    return producerSession.getSession();
  }

  @Override
  public Logger currentLogger() {
    return log;
  }

  /**
   * @return the transacted
   */
  private Boolean getTransactedSession() {
    return transactedSession;
  }

  /**
   * @param b the transacted to set
   */
  @SuppressWarnings("unused")
  private void setTransacted(boolean b) {
    transactedSession = b;
  }

  public boolean transactedSession() {
    return BooleanUtils.toBooleanDefaultIfNull(getTransactedSession(), false);
  }

  /**
   * @return the rollbackTimeout
   */
  @SuppressWarnings("unused")
  private long getRollbackTimeout() {
    return rollbackTimeout;
  }

  /**
   * Not directly configurable, as it is done by JmsTransactedWorkflow.
   *
   * @param l the rollbackTimeout to set
   */
  @SuppressWarnings("unused")
  private void setRollbackTimeout(long l) {
    rollbackTimeout = l;
  }

  @Override
  public long rollbackTimeout() {
    return rollbackTimeout;
  }

  @Override
  public boolean isManagedTransaction() {
    return false;
  }

  protected boolean captureOutgoingMessageDetails() {
    return BooleanUtils.toBooleanDefaultIfNull(getCaptureOutgoingMessageDetails(), false);
  }

  public Boolean getCaptureOutgoingMessageDetails() {
    return captureOutgoingMessageDetails;
  }

  /**
   * Specify whether or not to capture the outgoing message details as object metadata.
   * <p>
   * Some JMS providers may not make information such as {@link Message#getJMSMessageID()} available
   * until the message is accepted for delivery by the provider. Set this to be true, if you need to
   * make use of that information later on in the workflow. All information captured is stored
   * against the object metadata key "javax.jms.Message.{propertyName}" e.g.
   * "javax.jms.Message.JMSMessageID" where JMSMessageID is derived from the associated
   * {@link JmsConstants} constant.
   * </p>
   *
   * @param b true to capture standard JMS Headers as object metadata post produce. If unspecified,
   *        defaults to false.
   */
  public void setCaptureOutgoingMessageDetails(Boolean b) {
    captureOutgoingMessageDetails = b;
  }

  protected void captureOutgoingMessageDetails(Message jmsMsg, AdaptrisMessage msg) {
    if (captureOutgoingMessageDetails()) {
      String objectMetadataPrefix = Message.class.getCanonicalName() + ".";
      Map<String, String> jmsDetails = new HashMap<>();
      for (MetadataHandler.JmsPropertyHandler handler : MetadataHandler.JmsPropertyHandler
          .values()) {
        try {
          jmsDetails.put(objectMetadataPrefix + handler.getKey(), handler.getValue(jmsMsg));
        } catch (JMSException ignore) {

        }
      }
      msg.getObjectHeaders().putAll(jmsDetails);
    }
  }

  public ProducerSessionFactory getSessionFactory() {
    return sessionFactory;
  }

  /**
   * Set the behavioural characteristics of the session used by this producer.
   *
   * @param s the {@link ProducerSessionFactory} instance, default is
   *        {@link DefaultProducerSessionFactory}
   */
  public void setSessionFactory(ProducerSessionFactory s) {
    sessionFactory = Args.notNull(s, "sessionFactory");
  }

}
