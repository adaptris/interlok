package com.adaptris.core.jms;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullMessageConsumer;
import com.adaptris.core.NullMessageProducer;
import com.adaptris.core.ProduceException;
import com.adaptris.core.stubs.MockMessageListener;

public class OnMessageHandlerTest {
  
  private OnMessageHandler handler;
  
  private MockConsumer config;
  
  private CorrelationIdSource mockCorrelationSourceId;
  
  private MockMessageListener mockListener;
  
  private MessageTypeTranslator mockTranslator;
  
  private Enumeration<?> messageProperties;
  
  @Mock private Message jmsMessage;
  
  @Mock private Session mockSession;
  
  @Mock private Channel mockChannel;
  
  @Mock private AdaptrisConnection mockConnection;
  
  @Mock private AdaptrisMessageProducer mockProducer;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    
    mockCorrelationSourceId = new MessageIdCorrelationIdSource();
    mockListener = new MockMessageListener();
    mockListener.setChannel(mockChannel);
    mockTranslator = new TextMessageTranslator();
    
    config = new MockConsumer();
    config.setCurrentSession(mockSession);
    
    messageProperties = Collections.emptyEnumeration();
    
    when(jmsMessage.getPropertyNames())
        .thenReturn(messageProperties);
    
    when(jmsMessage.getJMSMessageID())
        .thenReturn("id");
    
    when(mockSession.getTransacted())
        .thenReturn(false);
    
    when(mockChannel.isAvailable())
        .thenReturn(true);
    
    when(mockChannel.getConsumeConnection())
        .thenReturn(mockConnection);
    
    when(mockChannel.getProduceConnection())
        .thenReturn(mockConnection);

  }
  
  @After
  public void tearDown() throws Exception {
    
  }
  
  @Test
  public void testMisConfig() {    
    try {
      new OnMessageHandler(config);
      fail("JmsActorConfig ");
    } catch (CoreException ex) {
      // expected
    }
  }
  
  @Test
  public void testOnMessageHandlerSuccessAck() throws Exception {
    config.setCorrelationIdSource(mockCorrelationSourceId);
    config.setMessageTranslator(mockTranslator);
    config.registerAdaptrisMessageListener(mockListener);
    
    handler = new OnMessageHandler(config);
    
    handler.onMessage(jmsMessage);
    
    verify(jmsMessage).acknowledge();
  }
  
  @Test
  public void testOnMessageHandlerSuccessTransactedCommit() throws Exception {
    when(mockSession.getTransacted())
        .thenReturn(true);
    
    JmsTransactedWorkflow jmsTransactedWorkflow = new JmsTransactedWorkflow();
    jmsTransactedWorkflow.setConsumer(new NullMessageConsumer());
    jmsTransactedWorkflow.setProducer(new NullMessageProducer());
    
    jmsTransactedWorkflow.registerChannel(mockChannel);
    
    config.setCorrelationIdSource(mockCorrelationSourceId);
    config.setMessageTranslator(mockTranslator);
    config.registerAdaptrisMessageListener(jmsTransactedWorkflow);
    
    handler = new OnMessageHandler(config);
    
    handler.onMessage(jmsMessage);
    
    verify(mockSession).commit();
  }
  
  @Test
  public void testOnMessageHandlerSuccessTransactedRollback() throws Exception {
    when(mockSession.getTransacted())
        .thenReturn(true);
    
    doThrow(new ProduceException("Expected"))
        .when(mockProducer).produce(any());
    
    when(mockProducer.createName())
        .thenReturn("name");
    
    when(mockProducer.createQualifier())
        .thenReturn("Qualifier");
    
    when(mockProducer.isTrackingEndpoint())
        .thenReturn(false);
    
    JmsTransactedWorkflow jmsTransactedWorkflow = new JmsTransactedWorkflow();
    jmsTransactedWorkflow.setConsumer(new NullMessageConsumer());
    jmsTransactedWorkflow.setProducer(mockProducer);
    
    jmsTransactedWorkflow.registerChannel(mockChannel);
    
    config.setCorrelationIdSource(mockCorrelationSourceId);
    config.setMessageTranslator(mockTranslator);
    config.registerAdaptrisMessageListener(jmsTransactedWorkflow);
    
    handler = new OnMessageHandler(config);
    
    handler.onMessage(jmsMessage);
    
    verify(mockSession).rollback();
  }
  
  @Test
  public void testOnMessageHandlerTranslatorFailsRollback() throws Exception {
    when(mockSession.getTransacted())
        .thenReturn(true);
    
    doThrow(new JMSException("Expected"))
        .when(jmsMessage).getPropertyNames();
    
    JmsTransactedWorkflow jmsTransactedWorkflow = new JmsTransactedWorkflow();
    jmsTransactedWorkflow.setConsumer(new NullMessageConsumer());
    jmsTransactedWorkflow.setProducer(new NullMessageProducer());
    
    jmsTransactedWorkflow.registerChannel(mockChannel);
    
    config.setCorrelationIdSource(mockCorrelationSourceId);
    config.setMessageTranslator(mockTranslator);
    config.registerAdaptrisMessageListener(jmsTransactedWorkflow);
    
    handler = new OnMessageHandler(config);
    
    handler.onMessage(jmsMessage);
    
    verify(mockSession).rollback();
  }

  @Test
  public void testOnMessageHandlerCommitFailsThenRollback() throws Exception {
    when(mockSession.getTransacted())
        .thenReturn(true);
    
    doThrow(new JMSException("Expected"))
        .when(mockSession).commit();
    
    JmsTransactedWorkflow jmsTransactedWorkflow = new JmsTransactedWorkflow();
    jmsTransactedWorkflow.setConsumer(new NullMessageConsumer());
    jmsTransactedWorkflow.setProducer(new NullMessageProducer());
    
    jmsTransactedWorkflow.registerChannel(mockChannel);
    
    config.setCorrelationIdSource(mockCorrelationSourceId);
    config.setMessageTranslator(mockTranslator);
    config.registerAdaptrisMessageListener(jmsTransactedWorkflow);
    
    handler = new OnMessageHandler(config);
    
    handler.onMessage(jmsMessage);
    
    verify(mockSession).commit();
    verify(mockSession).rollback();
  }
  
  @Test
  public void testOnMessageHandlerCommitRollbackFailsCaughtExceptions() throws Exception {
    when(mockSession.getTransacted())
        .thenReturn(true);
    
    doThrow(new JMSException("Expected"))
        .when(mockSession).commit();
    
    doThrow(new JMSException("Expected"))
        .when(mockSession).rollback();
    
    JmsTransactedWorkflow jmsTransactedWorkflow = new JmsTransactedWorkflow();
    jmsTransactedWorkflow.setConsumer(new NullMessageConsumer());
    jmsTransactedWorkflow.setProducer(new NullMessageProducer());
    
    jmsTransactedWorkflow.registerChannel(mockChannel);
    
    config.setCorrelationIdSource(mockCorrelationSourceId);
    config.setMessageTranslator(mockTranslator);
    config.registerAdaptrisMessageListener(jmsTransactedWorkflow);
    
    handler = new OnMessageHandler(config);
    
    handler.onMessage(jmsMessage);
    
    verify(mockSession).commit();
    verify(mockSession).rollback();
  }
  
  @Test
  public void testOnMessageHandlerProducerFailsRollback() throws Exception {
    when(mockSession.getTransacted())
        .thenReturn(true);
    
    JmsTransactedWorkflow jmsTransactedWorkflow = new JmsTransactedWorkflow();
    jmsTransactedWorkflow.setConsumer(new NullMessageConsumer());
    jmsTransactedWorkflow.setProducer(new MockProducer());
    jmsTransactedWorkflow.setStrict(true);
    
    jmsTransactedWorkflow.registerChannel(mockChannel);
    
    config.setCorrelationIdSource(mockCorrelationSourceId);
    config.setMessageTranslator(mockTranslator);
    config.registerAdaptrisMessageListener(jmsTransactedWorkflow);
    
    handler = new OnMessageHandler(config);
    
    handler.onMessage(jmsMessage);
    
    verify(mockSession).rollback();
  }
}
