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
import com.adaptris.core.http.jetty.JettyMessageConsumer;
import com.adaptris.core.util.LoggingHelper;
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
public final class JmsReplyToWorkflow extends StandardWorkflow {
  private static transient boolean warningLogged;

  // not marshalled
  private transient boolean isPas;

  public JmsReplyToWorkflow() {
    super();
    LoggingHelper.logDeprecation(warningLogged, ()-> { warningLogged=true;}, this.getClass().getSimpleName(), "StandardWorkflow+StandaloneProducer+JmsReplyToDestination instead");
  }

  @Override
  protected void initialiseWorkflow() throws CoreException {

    if (this.verifyConfig()) {
      super.initialiseWorkflow();
    }
    else {
      throw new CoreException("attempting to use JmsReplyToWorkflow with"
          + " non-JMS consumer and / or producer");
    }
  }

  private boolean verifyConfig() {
    boolean result = false;

    if (this.getConsumer() instanceof JmsConsumerImpl) {
      if (this.getProducer() instanceof PasProducer) {
        this.isPas = true;
        result = true;
      }
      else {
        if (this.getProducer() instanceof PtpProducer) {
          result = true;
        }
      }
    }

    return result;
  }

  @Override
  public void doProduce(AdaptrisMessage msg) throws ServiceException,
      ProduceException {

    Destination jmsDestination = (Destination) msg.getObjectHeaders().get(
        JmsConstants.OBJ_JMS_REPLY_TO_KEY);

    if (this.verifyDestinationDomain(jmsDestination)) {
      try {
        ((DefinedJmsProducer) this.getProducer()).produce(msg, jmsDestination, null);
      }
      catch (Exception e) {
        throw new ProduceException(e);
      }
    }
    else {
      throw new ProduceException("JMSReplyTo is null or wrong domain ["
          + jmsDestination + "]");
    }
  }

  private boolean verifyDestinationDomain(Destination jmsDestination) {
    boolean result = false;

    if (jmsDestination != null) {
      if (isPas && jmsDestination instanceof Topic) {
        result = true;
      }
      else {
        if (!isPas && jmsDestination instanceof Queue) {
          result = true;
        }
      }
    }

    return result;
  }

}
