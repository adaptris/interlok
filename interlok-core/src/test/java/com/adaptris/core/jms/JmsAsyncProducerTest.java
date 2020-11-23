package com.adaptris.core.jms;

import static org.junit.Assert.assertTrue;
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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ListenerCallbackHelper;
import com.adaptris.core.ProduceException;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.activemq.EmbeddedArtemis;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Closer;
import com.adaptris.util.KeyValuePair;

public class JmsAsyncProducerTest
    extends com.adaptris.interlok.junit.scaffolding.jms.JmsProducerExample {
  
  private static final String ID_HEADER = "interlokMessageId";

  private static EmbeddedArtemis activeMqBroker;
  
  private JmsAsyncProducer producer;

  private AdaptrisMessage adaptrisMessage;
  
  @Mock private ProducerSessionFactory mockSessionFactory;
  @Mock private ProducerSession mockSession;
  @Mock private MessageTypeTranslator mockTranslator;
  @Mock private Message mockMessage;
  @Mock private MessageProducer mockMessageProducer;
  @Mock private JmsDestination mockJmsDestination;
  @Mock private JmsConnection mockConnection;
  @Mock private ConnectionErrorHandler mockConnectionErrorHandler;

  private AutoCloseable openMocks;

  @BeforeClass
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedArtemis();
    activeMqBroker.start();
  }
  
  @AfterClass
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null) activeMqBroker.destroy();
  }
  
  @Before
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);
    
    adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage();
    
    ListenerCallbackHelper.prepare(adaptrisMessage, 
    (message) -> { try { mockMessage.acknowledge(); } catch (JMSException e) {} },
    (message) -> { });

    producer = new JmsAsyncProducer();

    producer.setSessionFactory(mockSessionFactory);
    producer.setMessageTranslator(mockTranslator);
    producer.registerConnection(mockConnection);

    when(mockMessage.getJMSMessageID())
      .thenReturn("messageId");
    
    when(mockMessage.getStringProperty(ID_HEADER))
      .thenReturn("messageId");
    
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
    
    when(mockConnection.retrieveConnection(JmsConnection.class))
      .thenReturn(mockConnection);
    
    when(mockConnection.getConnectionErrorHandler())
      .thenReturn(mockConnectionErrorHandler);

    LifecycleHelper.init(producer);
  }

  @After
  public void tearDown() throws Exception {
    Closer.closeQuietly(openMocks);
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

    verify(mockMessageProducer).send(any(), eq(mockMessage), any(JmsAsyncProducerEventHandler.class));
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
  public void testInitSetsEventhandlerAccept() throws Exception {
    LifecycleHelper.init(producer);
    assertTrue(producer.getEventHandler().getAcceptSuccessCallbacks());
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
    producer.getEventHandler().addUnAckedMessage(mockMessage.getJMSMessageID(), adaptrisMessage);
    producer.getEventHandler().onException(mockMessage, new Exception());

    verify(mockMessage, times(0)).acknowledge();
  }

  @Test
  public void testSuccessHandler() throws Exception {
    producer.getEventHandler().addUnAckedMessage(mockMessage.getJMSMessageID(), adaptrisMessage);
    producer.getEventHandler().onCompletion(mockMessage);

    verify(mockMessage, times(1)).acknowledge();
  }
  
  @Test
  public void testExceptionThrown() throws Exception {
    doThrow(new JMSException("expected"))
        .when(mockMessageProducer).send(any(), any(), any());
    try {
      producer.setPerMessageProperties(false);
      producer.produce(adaptrisMessage, mockJmsDestination);
      fail("Should throw produce exception");
    } catch (ProduceException ex) {
      // expected
    }
  }
  
  @Test
  public void testOnSuccessFails() throws Exception {
    doThrow(new JMSException("expected"))
      .when(mockMessage).getStringProperty(ID_HEADER);
    
    producer.getEventHandler().addUnAckedMessage(mockMessage.getJMSMessageID(), adaptrisMessage);
    producer.getEventHandler().onCompletion(mockMessage);

    verify(mockMessage, times(0)).acknowledge();
  }

  @Test
  public void testEmbeddedSuccessHandler() throws Exception {
    JmsAsyncProducer producer = new JmsAsyncProducer();

    producer.setEndpoint("jms:topic:myTopicName?priority=4");
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage("Some message content");
    StandaloneProducer standaloneProducer = new StandaloneProducer();

    try {
      standaloneProducer.setConnection(activeMqBroker.getJmsConnection());
      standaloneProducer.setProducer(producer);

      LifecycleHelper.initAndStart(standaloneProducer);

      standaloneProducer.doService(message);

    } finally {
      LifecycleHelper.stopAndClose(standaloneProducer);
    }
  }

  @Test
  public void testEmbeddedJmsException() throws Exception {
    JmsAsyncProducer producer = new JmsAsyncProducer();

    producer.setEndpoint("{}");
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage("Some message content");
    StandaloneProducer standaloneProducer = new StandaloneProducer();

    try {
      standaloneProducer.setConnection(activeMqBroker.getJmsConnection());
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
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JmsConnection c = createArtemisConnection();
    JmsAsyncProducer asyncProducer =
        new JmsAsyncProducer().withEndpoint("jms:topic:myTopicName?priority=4");

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
