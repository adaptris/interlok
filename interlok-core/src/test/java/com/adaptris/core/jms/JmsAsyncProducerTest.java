package com.adaptris.core.jms;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import javax.jms.CompletionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.NullConnection;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.StandardProcessingExceptionHandler;
import com.adaptris.core.fs.FsProducer;
import com.adaptris.core.jms.activemq.EmbeddedArtemis;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.KeyValuePair;

public class JmsAsyncProducerTest extends JmsProducerExample {

  private JmsAsyncProducer producer;

  private AdaptrisMessage adaptrisMessage;

  @Mock private ProducerSessionFactory mockSessionFactory;
  @Mock private ProducerSession mockSession;
  @Mock private MessageTypeTranslator mockTranslator;
  @Mock private Message mockMessage;
  @Mock private MessageProducer mockMessageProducer;
  @Mock private JmsDestination mockJmsDestination;
  @Mock private StandardProcessingExceptionHandler mockExceptionHandler;

  private AutoCloseable openMocks;

  @Before
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);

    adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage();

    producer = new JmsAsyncProducer();

    producer.setSessionFactory(mockSessionFactory);
    producer.setMessageTranslator(mockTranslator);
    producer.setAsyncMessageErrorHandler(mockExceptionHandler);

    when(mockSessionFactory.createProducerSession(any(), any()))
      .thenReturn(mockSession);
    when(mockSession.getProducer())
      .thenReturn(mockMessageProducer);
    when(mockTranslator.translate(any(AdaptrisMessage.class)))
      .thenReturn(mockMessage);

    when(mockJmsDestination.deliveryMode())
      .thenReturn("PERSISTENT");
    when(mockJmsDestination.priority())
    .thenReturn(1);
    when(mockJmsDestination.timeToLive())
    .thenReturn(1l);

  }

  @After
  public void tearDown() throws Exception {
    JdbcUtil.closeQuietly(openMocks);
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testSendFails() throws Exception {
    doThrow(new JMSException("expected"))
      .when(mockMessageProducer).send(any(), eq(mockMessage), any(CompletionListener.class));

    try {
      producer.produce(adaptrisMessage, mockJmsDestination);
      fail("Jms producer should have throw an exception on send()");
    } catch (Throwable t) {
      // expected;
    }
  }

  @Test
  public void testSendPerMessageProperties() throws Exception {
    producer.produce(adaptrisMessage, mockJmsDestination);

    verify(mockMessageProducer).send(any(), eq(mockMessage), any(int.class), any(int.class), any(long.class), any(CompletionListener.class));
  }

  @Test
  public void testSendNotPerMessageProperties() throws Exception {
    producer.setPerMessageProperties(false);
    producer.produce(adaptrisMessage, mockJmsDestination);

    verify(mockMessageProducer).send(any(), eq(mockMessage), any(JmsAsyncProducer.class));
  }

  @Test
  public void testCaptureOutgoingMessageProperties() throws Exception {
    producer.setCaptureOutgoingMessageDetails(true);
    producer.produce(adaptrisMessage, mockJmsDestination);

    verify(mockMessage).getJMSMessageID();
    verify(mockMessage).getJMSType();
    verify(mockMessage).getJMSDeliveryMode();
    verify(mockMessage).getJMSPriority();
  }

  @Test
  public void testNotCaptureOutgoingMessageProperties() throws Exception {
    producer.setCaptureOutgoingMessageDetails(false);
    producer.produce(adaptrisMessage, mockJmsDestination);

    verify(mockMessage, times(0)).getJMSMessageID();
    verify(mockMessage, times(0)).getJMSType();
    verify(mockMessage, times(0)).getJMSDeliveryMode();
    verify(mockMessage, times(0)).getJMSPriority();
  }

  @Test
  public void testInitWithoutExceptionHandlerFails() throws Exception {
    producer.setAsyncMessageErrorHandler(null);

    try {
      LifecycleHelper.init(producer);
      fail("Should throw core exception without a configured exception handler.");
    } catch (CoreException ex) {
      //expected.
    }
  }

  @Test
  public void testInitWithExceptionHandler() throws Exception {
    try {
      LifecycleHelper.init(producer);
    } catch (CoreException ex) {
      fail("Shouldn't throw core exception with a configured exception handler.");
    }
  }

  @Test
  public void testExceptionHandler() throws Exception {
    producer.onException(mockMessage, new Exception());

    verify(mockExceptionHandler).handleProcessingException(any());
  }

  @Test
  public void testSuccessHandler() throws Exception {
    producer.onCompletion(mockMessage);

    verify(mockExceptionHandler, times(0)).handleProcessingException(any(AdaptrisMessage.class));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testEmbeddedSuccessHandler() throws Exception {
    EmbeddedArtemis broker = new EmbeddedArtemis();
    JmsAsyncProducer producer = new JmsAsyncProducer();
    producer.setAsyncMessageErrorHandler(mockExceptionHandler);

    producer.setDestination(new ConfiguredProduceDestination("jms:topic:myTopicName?priority=4"));
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage("Some message content");
    StandaloneProducer standaloneProducer = new StandaloneProducer();

    try {
      broker.start();

      standaloneProducer.setConnection(broker.getJmsConnection());
      standaloneProducer.setProducer(producer);

      LifecycleHelper.initAndStart(standaloneProducer);

      standaloneProducer.doService(message);

    } finally {
      LifecycleHelper.stopAndClose(standaloneProducer);
      broker.destroy();
    }

    verify(mockExceptionHandler, times(0)).handleProcessingException(any(AdaptrisMessage.class));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testEmbeddedJmsException() throws Exception {
    EmbeddedArtemis broker = new EmbeddedArtemis();
    JmsAsyncProducer producer = new JmsAsyncProducer();
    producer.setAsyncMessageErrorHandler(mockExceptionHandler);

    producer.setDestination(new ConfiguredProduceDestination("{}"));
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage("Some message content");
    StandaloneProducer standaloneProducer = new StandaloneProducer();

    try {
      broker.start();

      standaloneProducer.setConnection(broker.getJmsConnection());
      standaloneProducer.setProducer(producer);

      LifecycleHelper.initAndStart(standaloneProducer);

      try {
        standaloneProducer.doService(message);
        fail("Should fail, the destination is not allowed.");
      } catch (CoreException ex) {
        // expected
      }

    } finally {
      LifecycleHelper.stopAndClose(standaloneProducer);
      broker.destroy();
    }

    verify(mockExceptionHandler, times(0)).handleProcessingException(any(AdaptrisMessage.class));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JmsConnection c = createArtemisConnection();
    JmsAsyncProducer asyncProducer =
        new JmsAsyncProducer().withEndpoint("jms:topic:myTopicName?priority=4");
    FsProducer fsProducer = new FsProducer().withBaseDirectoryUrl("my-failed-messages-dir");
    StandaloneProducer fsStandaloneProducer = new StandaloneProducer(new NullConnection(), fsProducer);

    asyncProducer.setAsyncMessageErrorHandler(new StandardProcessingExceptionHandler(fsStandaloneProducer));

    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    NullCorrelationIdSource mcs = new NullCorrelationIdSource();
    asyncProducer.setCorrelationIdSource(mcs);

    StandaloneProducer result = new StandaloneProducer();

    result.setConnection(c);
    result.setProducer(asyncProducer);

    return result;
  }

  protected JmsConnection createArtemisConnection() {
    StandardJndiImplementation jndiImplementation = new StandardJndiImplementation();
    jndiImplementation.setJndiName("ConnectionFactory");
    jndiImplementation.getJndiParams().addKeyValuePair(new KeyValuePair("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"));
    jndiImplementation.getJndiParams().addKeyValuePair(new KeyValuePair("java.naming.provider.url", "tcp://localhost:61616?type=CF"));

    return new JmsConnection(jndiImplementation);
  }

}
