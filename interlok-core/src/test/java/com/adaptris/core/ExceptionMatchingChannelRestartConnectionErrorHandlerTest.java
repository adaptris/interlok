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

import com.adaptris.core.jms.DefinedJmsProducer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.ProducerSession;
import com.adaptris.util.TimeInterval;
import org.junit.jupiter.api.Test;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExceptionMatchingChannelRestartConnectionErrorHandlerTest extends com.adaptris.interlok.junit.scaffolding.BaseCase {

  public ExceptionMatchingChannelRestartConnectionErrorHandlerTest() {

  }

  @Test
  public void testChannelUnavailableConnectionErrorHandlingProducer() throws Exception {
      Channel channel = new Channel();
      ExceptionMatchingConnectionErrorHandler handler = spy(new ExceptionMatchingConnectionErrorHandler());

      ChannelRestartConnectionErrorHandler delegate = spy(new ChannelRestartConnectionErrorHandler());
      // set the last connection time in the future, so it doesn't try to restart the channel
      delegate.setLastConnectionExceptionDateTime(LocalDateTime.now()
              .plus(Duration.ofDays(1)));
      handler.setDelegate(delegate);
      delegate.setDurationBetweenRestarts(Duration.ofSeconds(10));

      InstanceOfExceptionMatcher matcher = new InstanceOfExceptionMatcher();
      matcher.setClazz(ProduceException.class);

      handler.setExceptionMatcher(matcher);

      NullConnection connection = new NullConnection();
      ExceptionMatchingConnectionErrorHandler handler1 = spy(new ExceptionMatchingConnectionErrorHandler());
      handler1.setExceptionMatcher(matcher);
      connection.setConnectionErrorHandler(handler1);
      channel.setProduceConnection(connection);

      ChannelUnavailableConnectionErrorHandlingProducer producer = new ChannelUnavailableConnectionErrorHandlingProducer();
      producer.setConnectionErrorWaitDuration("PT10S");
      CustomisableProducer producerDelegateSuccess = new CustomisableProducer(
              (msg, dest1, dest2) -> {},
              (msg, dest1, dest2) -> { return msg; }
              );
      CustomisableProducer producerDelegateFailure = new CustomisableProducer(
              (msg, dest1, dest2) -> { throw new ProduceException(); },
              (msg, dest1, dest2) -> { throw new ProduceException(); }
      );
      producerDelegateSuccess.registerConnection(connection);
      producerDelegateFailure.registerConnection(connection);

      producer.setDelegate(producerDelegateFailure);
      producer.setConnectionErrorThreshold(5);
      producer.setConnectionErrorWaitDuration(Duration.ofSeconds(10));

      WorkflowList workflowList = new WorkflowList();
      StandardWorkflow workflow = new StandardWorkflow();
      workflow.setChannelUnavailableWaitInterval(new TimeInterval(0L, TimeUnit.SECONDS));
      channel.setWorkflowList(workflowList);
      workflowList.add(workflow);
      workflow.setProducer(producer);

      AdaptrisMessage msg = mock(AdaptrisMessage.class);

      start(channel);

      // after encountering the threshold of connection errors, the channel becomes unavailable
      assertEquals(0, producer.getConnectionErrors());
      assertTrue(channel.isAvailable());
      assertThrows(ProduceException.class, () -> producer.produce(msg));

      assertEquals(1, producer.getConnectionErrors());
      assertTrue(channel.isAvailable());
      assertThrows(ProduceException.class, () -> producer.request(msg));
      assertTrue(channel.isAvailable());
      assertThrows(ProduceException.class, () -> producer.produce(msg));
      assertTrue(channel.isAvailable());
      assertThrows(ProduceException.class, () -> producer.produce(msg));
      assertTrue(channel.isAvailable());
      assertThrows(ProduceException.class, () -> producer.produce(msg));
      assertEquals(5, producer.getConnectionErrors());
      assertFalse(channel.isAvailable());

      // after the connection error wait duration, the channel is available
      Thread.sleep((producer.connectionErrorWaitDuration().getSeconds()+1)*1000);
      assertTrue(channel.isAvailable());
      assertEquals(5, producer.getConnectionErrors());

      assertThrows(ProduceException.class, () -> producer.produce(msg));
      assertFalse(channel.isAvailable());
      assertEquals(6, producer.getConnectionErrors());

      // after a successful produce, the channel is available and error count is reset
      DefaultMessageFactory factory = new DefaultMessageFactory();
      AdaptrisMessage message = factory.newMessage();
      producer.setDelegate(producerDelegateSuccess);

      assertDoesNotThrow(() -> producer.produce(message));
      assertTrue(channel.isAvailable());
      assertEquals(0, producer.getConnectionErrors());


  }

  @Test
  public void testAdaptrisComponentConnectionErrorHandler() throws Exception {
      ExceptionMatchingConnectionErrorHandler handler = spy(new ExceptionMatchingConnectionErrorHandler());
      ChannelRestartConnectionErrorHandler delegate = spy(new ChannelRestartConnectionErrorHandler());
      handler.setDelegate(delegate);
      delegate.setDurationBetweenRestarts(Duration.ofSeconds(10));

      InstanceOfExceptionMatcher matcher = new InstanceOfExceptionMatcher();
      matcher.setClazz(ProduceException.class);

      handler.setExceptionMatcher(matcher);


      TriggeredFailingConnection connection = new TriggeredFailingConnection();
      connection.setConnectionErrorHandler(handler);

      Channel channel = new Channel();
      channel.setConsumeConnection(connection);
      channel.setProduceConnection(connection);

      WorkflowList workflowList = new WorkflowList();
      StandardWorkflow workflow = new StandardWorkflow();
      workflow.setChannelUnavailableWaitInterval(new TimeInterval(0L, TimeUnit.SECONDS));
      channel.setWorkflowList(workflowList);
      workflowList.add(workflow);

      CustomisableProducer producerDelegate = new CustomisableProducer((msg, dest, reply) -> {
          throw new ProduceConnectionException();
      }, (msg, dest, timeout) -> {
          throw new ProduceConnectionException();
      });
      ConnectionErrorHandlingProducer producer = new ConnectionErrorHandlingProducer();
      producer.setDelegate(producerDelegate);
      workflow.setProducer(producer);

      DefaultMessageFactory factory = new DefaultMessageFactory();
      AdaptrisMessage message = factory.newMessage();

      connection.setFail(false);
      start(channel);
      connection.setFail(true);
      // trigger restart
      workflow.onAdaptrisMessage(message);
      assertFalse(channel.isAvailable());

      // won't trigger restart
      workflow.onAdaptrisMessage(message);
      assertFalse(channel.isAvailable());

      Thread.sleep((delegate.durationBetweenRestarts().getSeconds()/2)*1000);

      // won't trigger restart
      workflow.onAdaptrisMessage(message);
      assertFalse(channel.isAvailable());

      Thread.sleep((delegate.durationBetweenRestarts().getSeconds()/2+1)*1000);

      // won't trigger restart as channel is still unavailable
      workflow.onAdaptrisMessage(message);
      assertFalse(channel.isAvailable());

      // set channel available and will trigger restart
      channel.toggleAvailability(true);
      assertTrue(channel.isAvailable());
      workflow.onAdaptrisMessage(message);
      // after attempting to send the message, encountering an exception will set channel false
      assertFalse(channel.isAvailable());

      // stop connection from failing
      channel.toggleAvailability(true);
      connection.setFail(false);
      delegate.reset();
      // sending a message should throw an exception due to producer failing
      workflow.onAdaptrisMessage(message);

      assertFalse(channel.isAvailable());

      // set producer to succeed
      channel.toggleAvailability(true);
      delegate.reset();
      producerDelegate.setDoProduceFn((msg, dest, reply) -> {});
      producerDelegate.setDoRequestFn((msg, dest, timeout) -> msg);

      workflow.onAdaptrisMessage(message);
      assertTrue(channel.isAvailable());

      stop(channel);

      verify(delegate, times(4)).restartAffectedComponents();
      verify(handler, times(6)).canHandleException(any(ProduceException.class));
  }

  private class TriggeredFailingConnection extends JmsConnection {
      private boolean fail = true;
    public TriggeredFailingConnection() {
      this(new ConnectionErrorHandlerImp() {
        @Override
        public void handleConnectionException() {
          super.restartAffectedComponents();
        };

        @Override
        public void init() throws CoreException {

        }
        @Override
        public void start() throws CoreException {
        }

        @Override
        public void stop() {
        }

        @Override
        public void close() {
        }
      });
    }

    public TriggeredFailingConnection(ConnectionErrorHandler c) {
      super();
      setConnectionErrorHandler(c);
    }

    public void triggerError() {
      connectionErrorHandler().handleConnectionException();
    }

    @Override
    protected void initConnection() throws CoreException {
      ;
    }

      public boolean isFail() {
          return fail;
      }

      public void setFail(boolean fail) {
          this.fail = fail;
      }

      /**
     *
     * @see AdaptrisConnectionImp#startConnection()
     */
    @Override
    protected void startConnection() throws CoreException {
      if (fail) throw new CoreException();
    }

    /**
     *
     * @see AdaptrisConnectionImp#stopConnection()
     */
    @Override
    protected void stopConnection() {
      ;
    }

    /**
     *
     * @see AdaptrisConnectionImp#closeConnection()
     */
    @Override
    protected void closeConnection() {
      ;
    }

    @Override
    protected void prepareConnection() throws CoreException {
    }


  }

  static class ProduceConnectionException extends ProduceException {

      public ProduceConnectionException() {
      }

      public ProduceConnectionException(Throwable cause) {
          super(cause);
      }

      public ProduceConnectionException(String description) {
          super(description);
      }

      public ProduceConnectionException(String description, Throwable cause) {
          super(description, cause);
      }
  }

  static class CustomisableProducer extends DefinedJmsProducer {
      TriConsumerThrowsException<AdaptrisMessage, Destination, Destination> doProduceFn;
      TriFunctionThrowsException<AdaptrisMessage, String, Long, AdaptrisMessage> doRequestFn;
      public CustomisableProducer(TriConsumerThrowsException<AdaptrisMessage, Destination, Destination> doProduceFn,
                                  TriFunctionThrowsException<AdaptrisMessage, String, Long, AdaptrisMessage> doRequestFn) {
          this.doProduceFn = doProduceFn;
          this.doRequestFn = doRequestFn;
      }

      public TriConsumerThrowsException<AdaptrisMessage, Destination, Destination> getDoProduceFn() {
          return doProduceFn;
      }

      public void setDoProduceFn(TriConsumerThrowsException<AdaptrisMessage, Destination, Destination> doProduceFn) {
          this.doProduceFn = doProduceFn;
      }

      public TriFunctionThrowsException<AdaptrisMessage, String, Long, AdaptrisMessage> getDoRequestFn() {
          return doRequestFn;
      }

      public void setDoRequestFn(TriFunctionThrowsException<AdaptrisMessage, String, Long, AdaptrisMessage> doRequestFn) {
          this.doRequestFn = doRequestFn;
      }

      @Override
      public Destination createDestination(String name) throws JMSException {
          return mock(Destination.class);
      }

      @Override
      public void doProduce(AdaptrisMessage msg, Destination dest, Destination replyTo)
              throws JMSException, CoreException {
          doProduceFn.accept(msg, replyTo, dest);
      }

      @Override
      public Destination createTemporaryDestination() throws JMSException {
          return mock(Destination.class);
      }

      @Override
      public void rollback() {
          // do nothing
      }

      @Override
      public void commit() throws JMSException {
          // do nothing
      }

      @Override
      public String endpoint(AdaptrisMessage msg) throws ProduceException {
          return "endpoint";
      }

      @Override
      public AdaptrisMessage doRequest(AdaptrisMessage msg, String dest, long timeout) throws ProduceException {
          return doRequestFn.apply(msg, dest, timeout);
      }

      @Override
      public ProducerSession setupSession(AdaptrisMessage msg) throws JMSException {
          return null;
      }

      @Override
      public void logLinkedException(String prefix, Exception e) {
          super.logLinkedException(prefix, e);
      }

      @Override
      public int calculateDeliveryMode(AdaptrisMessage msg, String defaultDeliveryMode) {
          return super.calculateDeliveryMode(msg, defaultDeliveryMode);
      }


      @Override
      public long calculateTimeToLive(AdaptrisMessage msg, Long defaultTTL) throws JMSException {
          return super.calculateTimeToLive(msg, defaultTTL);
      }


      @Override
      public Message translate(AdaptrisMessage msg, Destination replyTo) throws JMSException {
          return super.translate(msg, replyTo);
      }

      @Override
      public int calculatePriority(AdaptrisMessage msg, Integer defaultPriority) {
          return super.calculatePriority(msg, defaultPriority);
      }

      @Override
      public boolean captureOutgoingMessageDetails() {
          return super.captureOutgoingMessageDetails();
      }
  }

    @FunctionalInterface
    public interface TriConsumerThrowsException<T, U, V> {
        void accept(T t, U u, V v) throws ProduceException;
    }

    @FunctionalInterface
    public interface TriFunctionThrowsException<T, U, V, R> {
        R apply(T t, U u, V v) throws ProduceException;
    }
}
