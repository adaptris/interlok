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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.interlok.util.Args;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Extension of {@link com.adaptris.core.StandardWorkflow} for use with JMS consumers and producers.
 *
 * <p>
 * Generally it is preferred that you configure a {@link com.adaptris.core.StandardWorkflow} and make use the
 * appropriate {@link JmsProducerImpl} implementation with a {@link JmsReplyToDestination} instead.
 * </p>
 * <p>
 * Key differences to {@link com.adaptris.core.StandardWorkflow} are
 * <ul>
 * <li>Any configured {@link com.adaptris.core.ProduceDestination} is ignored; the appropriate
 * destination is derived from object metadata</li>
 * <li>The {@link JmsProducerImpl} implementations must correspond to the associated
 * {@link JmsConsumerImpl} implementation; i.e. {@link PtpProducer} must be used with
 * {@link PtpConsumer}.</li>
 * <li>Does not obey the use of {@link com.adaptris.core.CoreConstants#KEY_WORKFLOW_SKIP_PRODUCER}, the producer is
 * always triggered.
 * </ul>
 * </p>
 *
 * @config jms-reply-to-workflow
 *
 * @deprecated Use a {@link com.adaptris.core.StandardWorkflow} with a {@link com.adaptris.core.StandaloneProducer} with a
 * {@link JmsReplyToDestination}.
 */
@XStreamAlias("jms-reply-to-workflow")
@Deprecated
@AdapterComponent
@ComponentProfile(summary = "Deprecated: use StandardWorkflow+StandaloneProducer+JmsReplyToDestination instead",
tag = "workflow,jms")
@DisplayOrder(order = {"disableDefaultMessageCount", "sendEvents", "logPayload"})
@ConfigDeprecated(removalVersion = "4.0.0", groups = Deprecated.class)
public final class JmsReplyToWorkflow extends StandardWorkflow {
  private transient boolean warningLogged;

  private static final Map<Class<?>, List<Class<?>>> VALID_PRODUCER_FOR_CONSUMER;
  private static final Map<Class<?>, ProducerType> PRODUCER_BY_TYPE;


  static {
    Map<Class<?>, List<Class<?>>> consumerToProducers = new HashMap<>();
    consumerToProducers.put(PasConsumer.class, Arrays.asList(PasProducer.class));
    consumerToProducers.put(PtpConsumer.class, Arrays.asList(PtpProducer.class));
    VALID_PRODUCER_FOR_CONSUMER = Collections.unmodifiableMap(consumerToProducers);

    Map<Class<?>, ProducerType> producersToType = new HashMap<>();
    producersToType.put(PtpProducer.class, ProducerType.QueueProducer);
    producersToType.put(PasProducer.class, ProducerType.TopicProducer);
    PRODUCER_BY_TYPE = Collections.unmodifiableMap(producersToType);
  }


  protected enum ProducerType {
    TopicProducer {

      @Override
      public Destination validate(Destination d) throws ProduceException {
        if (!(d instanceof Topic)) {
          throw new ProduceException("JMSReplyTo is not a topic");
        }
        return d;
      }

    },
    QueueProducer {
      @Override
      public Destination validate(Destination d) throws ProduceException {
        if (!(d instanceof Queue)) {
          throw new ProduceException("JMSReplyTo is not a queue");
        }
        return d;
      }

    };

    public abstract Destination validate(Destination d) throws ProduceException;
  };

  private transient ProducerType producerType;


  public JmsReplyToWorkflow() {
    super();
  }

  @Override
  protected void prepareWorkflow() throws CoreException {
    LoggingHelper.logDeprecation(warningLogged, () -> {
      warningLogged = true;
    }, this.getClass().getSimpleName(),
        "StandardWorkflow+StandaloneProducer+JmsReplyToDestination instead");
    validateConfiguration();
    super.prepareWorkflow();
  }

  private void validateConfiguration() throws CoreException {
    Class<?> consumerClass = getConsumer().getClass();
    Class<?> producerClass = getProducer().getClass();
    List<Class<?>> validProducers = Optional.ofNullable(VALID_PRODUCER_FOR_CONSUMER.get(consumerClass))
        .orElseThrow(
            () -> new CoreException(
                "Can't handle consumer : " + consumerClass.getSimpleName()));

    if (!validProducers.contains(producerClass)) {
      throw new CoreException(String.format("%s cannot be used with %s",
          consumerClass.getSimpleName(), producerClass.getSimpleName()));
    }
    // Should exist in the class, otherwise the test above should have failed!
    producerType = Args.notNull(PRODUCER_BY_TYPE.get(producerClass), "producerType");
  }

  @Override
  public void doProduce(AdaptrisMessage msg) throws ServiceException, ProduceException {
    try {
      Destination jmsDestination =
          Args.notNull((Destination) msg.getObjectHeaders().get(JmsConstants.OBJ_JMS_REPLY_TO_KEY),
              "jmsreplyto");
      ((DefinedJmsProducer) getProducer()).doProduce(msg, producerType.validate(jmsDestination), null);
    } catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
  }
}
