package com.adaptris.core.jms;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.CompletionListener;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

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
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
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
  
  
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
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
  
  public JmsAsyncProducerTest(String name) {
    super(name);
  }
  
  public void testSendFails() throws Exception {
    doThrow(new JMSException("expected"))
      .when(mockMessageProducer).send(any(Destination.class), any(Message.class), any(CompletionListener.class));
      
    try {
      producer.produce(adaptrisMessage, mockJmsDestination);
      fail("Jms producer should have throw an exception on send()");
    } catch (Throwable t) {
      // expected;
    }
  }
  
  public void testSendPerMessageProperties() throws Exception {
    producer.produce(adaptrisMessage, mockJmsDestination);
    
    verify(mockMessageProducer).send(any(Destination.class), any(Message.class), any(int.class), any(int.class), any(long.class), any(CompletionListener.class));
  }
  
  public void testSendNotPerMessageProperties() throws Exception {
    producer.setPerMessageProperties(false);
    producer.produce(adaptrisMessage, mockJmsDestination);
    
    verify(mockMessageProducer).send(any(Destination.class), any(Message.class), any(CompletionListener.class));
  }
  
  public void testCaptureOutgoingMessageProperties() throws Exception {
    producer.setCaptureOutgoingMessageDetails(true);
    producer.produce(adaptrisMessage, mockJmsDestination);
    
    verify(mockMessage).getJMSMessageID();
    verify(mockMessage).getJMSType();
    verify(mockMessage).getJMSDeliveryMode();
    verify(mockMessage).getJMSPriority();
  }
  
  public void testNotCaptureOutgoingMessageProperties() throws Exception {
    producer.setCaptureOutgoingMessageDetails(false);
    producer.produce(adaptrisMessage, mockJmsDestination);
    
    verify(mockMessage, times(0)).getJMSMessageID();
    verify(mockMessage, times(0)).getJMSType();
    verify(mockMessage, times(0)).getJMSDeliveryMode();
    verify(mockMessage, times(0)).getJMSPriority();
  }
  
  public void testInitWithoutExceptionHandlerFails() throws Exception {
    producer.setAsyncMessageErrorHandler(null);
    
    try {
      LifecycleHelper.init(producer);
      fail("Should throw core exception without a configured exception handler.");
    } catch (CoreException ex) {
      //expected.
    }
  }
  
  public void testInitWithExceptionHandler() throws Exception {    
    try {
      LifecycleHelper.init(producer);
    } catch (CoreException ex) {
      fail("Shouldn't throw core exception with a configured exception handler.");
    }
  }
  
  public void testExceptionHandler() throws Exception {
    producer.onException(mockMessage, new Exception());
    
    verify(mockExceptionHandler).handleProcessingException(any(AdaptrisMessage.class));
  }
  
  public void testSuccessHandler() throws Exception {
    producer.onCompletion(mockMessage);
    
    verify(mockExceptionHandler, times(0)).handleProcessingException(any(AdaptrisMessage.class));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    StandardJndiImplementation jndiImplementation = new StandardJndiImplementation();
    jndiImplementation.setJndiName("ConnectionFactory");
    jndiImplementation.getJndiParams().addKeyValuePair(new KeyValuePair("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory"));
    jndiImplementation.getJndiParams().addKeyValuePair(new KeyValuePair("java.naming.provider.url", "tcp://localhost:61616?type=CF"));
    
    JmsConnection c = new JmsConnection(jndiImplementation);
    ConfiguredProduceDestination dest = new ConfiguredProduceDestination("jms:topic:myTopicName?priority=4");

    JmsAsyncProducer asyncProducer = new JmsAsyncProducer();
    asyncProducer.setDestination(dest);
    
    FsProducer fsProducer = new FsProducer(new ConfiguredProduceDestination("my-failed-messages-dir"));
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

}
