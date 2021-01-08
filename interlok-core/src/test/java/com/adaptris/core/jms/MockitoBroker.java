package com.adaptris.core.jms;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.jms.activemq.BasicActiveMqImplementation;
import com.adaptris.util.TimeInterval;

public class MockitoBroker implements TestJmsBroker {

  private JmsConnection mockJmsConnection;
  
  @Mock private Session mockSession;
  
  @Mock private VendorImplementation mockVendorImp;
  
  @Mock private ConnectionFactory mockConnectionFactory;
  
  @Mock private Connection mockConnection;
  
  @Mock private MessageConsumer mockMessageConsumer;
  
  @Mock private TopicSubscriber mockTopicSubscriber;
  
  @Mock private Queue mockQueue;
  
  @Mock private Topic mockTopic;
  
  @Mock private TextMessage mockJmsTextMessage;
  
  @Mock private MessageProducer mockMessageProducer;
  
  private VendorImplementation vendorImplementation;
  
  private AutoReplier autoReplier;
  
  public MockitoBroker() {
    MockitoAnnotations.openMocks(this);
    
    JmsConnection jmsConnection = new JmsConnection();
    jmsConnection.setConnectionAttempts(3);
    jmsConnection.setConnectionRetryInterval(new TimeInterval(1l, TimeUnit.SECONDS));
    
    setJmsConnection(jmsConnection);
  }
 
  public void perTestSetup() throws Exception {
    getJmsConnection().setVendorImplementation(mockVendorImp);
  }
  
  public void start() throws Exception {
    // Create a new mock JMS session, when requested.
    when(mockVendorImp.createSession(any(), anyBoolean(), anyInt()))
        .thenReturn(mockSession);
    
    when(mockVendorImp.createConnectionFactory())
        .thenReturn(mockConnectionFactory);
    
    when(mockVendorImp.createConnection(mockConnectionFactory, mockJmsConnection))
        .thenReturn(mockConnection);
    
    // when creating an endpoint, take the destination string and actually generate a real thing
    // We do this because the destination string will determine if we need to create a topic or queue, which will likely be tested in test classes.
    when(mockVendorImp.createDestination(any(String.class), any(JmsActorConfig.class)))
        .thenAnswer( (invocation) -> {
          return new TempVendorImplementationImp().createDestination(invocation.getArgument(0, String.class), invocation.getArgument(1, JmsActorConfig.class));
        });
    
    // now make sure the mock session creates our various message consumers.
    when(mockSession.createSharedDurableConsumer(any(Topic.class), anyString(), anyString()))
        .thenReturn(mockTopicSubscriber);
    when(mockSession.createDurableSubscriber(any(Topic.class), anyString()))
        .thenReturn(mockTopicSubscriber);
    when(mockSession.createSharedConsumer(any(Topic.class), anyString(), anyString()))
        .thenReturn(mockTopicSubscriber);
    when(mockSession.createConsumer(any(Destination.class), any()))
        .thenReturn(mockTopicSubscriber);
    
    // now when we send a message many of our tests expect to be able to consume it back, so
    // let's record the message listener now and when a send happens we'll just post it back through to the not-null listener.
    doAnswer((invocation) -> {
      autoReplier = new AutoReplier();
      autoReplier.setAdaptrispMessageListener(invocation.getArgument(0, MessageListener.class));
      
      return null;
    }).when(mockTopicSubscriber).setMessageListener(any(MessageListener.class));
    
    // And this bit will post the produced message back into the message listener (our consumers onMessage) if set.
    doAnswer( (invocation) -> {
      if((autoReplier != null) && (autoReplier.getAdaptrispMessageListener() != null)){
        autoReplier.getAdaptrispMessageListener().onMessage(invocation.getArgument(1, Message.class));
      }
      return null;
    }).when(mockMessageProducer).send(any(Destination.class), any(Message.class), anyInt(), anyInt(), anyLong());
    
    // Copy from above, but handles the 2 argument send.
    doAnswer( (invocation) -> {
      if((autoReplier != null) && (autoReplier.getAdaptrispMessageListener() != null)){
        autoReplier.getAdaptrispMessageListener().onMessage(invocation.getArgument(1, Message.class));
      }
      return null;
    }).when(mockMessageProducer).send(any(Destination.class), any(Message.class));
    
    // Create the producer for the session
    when(mockSession.createProducer(null))
        .thenReturn(mockMessageProducer);
    
    // Create the mock endpoints from the session
    when(mockSession.createQueue(anyString()))
        .thenReturn(mockQueue);
    when(mockSession.createTopic(anyString()))
        .thenReturn(mockTopic);
    
    // And of course our JMS text message, which we do some fricking awesomeness and inner mock it's content.
    // This makes sure the mock message returns the same content it was created with.
    doAnswer( (invocation) -> {
      when(mockJmsTextMessage.getText())
          .thenReturn(invocation.getArgument(0));
      
      return mockJmsTextMessage;
    }).when(mockSession).createTextMessage(anyString());
    
    // when we convert the jms message we need to handle the properties, so they'll not null.
    when(mockJmsTextMessage.getPropertyNames())
        .thenReturn(Collections.emptyEnumeration());

  }
  
  public void destroy() {}
  
  public String getName() {
    return getClass().getName();
  }
  
  public JmsConnection getJmsConnection() {
    return mockJmsConnection;
  }
  
  public JmsConnection getJmsConnection(BasicActiveMqImplementation vendorImp, boolean useTcp) {
    return getJmsConnection();
  }
  
  public void setJmsConnection(JmsConnection connection) {
    mockJmsConnection = connection;
  }

  public VendorImplementation getVendorImplementation() {
    return vendorImplementation;
  }

  public void setVendorImplementation(VendorImplementation vendorImplementation) {
    this.vendorImplementation = vendorImplementation;
    getJmsConnection().setVendorImplementation(vendorImplementation);
  }
  
  public class TempVendorImplementationImp extends VendorImplementationImp {
    @Override
    public ConnectionFactory createConnectionFactory() throws JMSException {
      return null;
    }

    @Override
    public boolean connectionEquals(VendorImplementationBase comparable) {
      return false;
    }
  }
  
  class AutoReplier {
    private MessageListener adaptrispMessageListener;

    public MessageListener getAdaptrispMessageListener() {
      return adaptrispMessageListener;
    }

    public void setAdaptrispMessageListener(MessageListener adaptrispMessageListener) {
      this.adaptrispMessageListener = adaptrispMessageListener;
    }
    
  }
}
