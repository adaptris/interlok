package com.adaptris.interlok.junit.scaffolding.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.jms.DefinedJmsProducer;
import com.adaptris.core.jms.ProducerSession;

public class MockProducer extends DefinedJmsProducer {

  @Override
  public Destination createDestination(String name) throws JMSException {
    throw new JMSException("NO!");
  }

  @Override
  public void doProduce(AdaptrisMessage msg, Destination dest, Destination replyTo)
      throws JMSException, CoreException {
    throw new ProduceException();
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg, ProduceDestination dest, long timeout)
      throws ProduceException {
    throw new ProduceException();
  }

  @Override
  public Destination createTemporaryDestination() throws JMSException {
    throw new JMSException("NO!");
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg) throws ProduceException {
    throw new ProduceException();
  }

  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    throw new ProduceException();
  }

  @Override
  public String endpoint(AdaptrisMessage msg) throws ProduceException {
    return null;
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg, long timeout) throws ProduceException {
    throw new ProduceException();
  }

  @Override
  public ProducerSession setupSession(AdaptrisMessage msg) throws JMSException {
    return super.setupSession(msg);
  }

  @Override
  public void logLinkedException(String prefix, Exception e) {
    super.logLinkedException(prefix, e);
  }

  @Override
  public Destination createDestination(ProduceDestination d, AdaptrisMessage msg)
      throws CoreException {
    return super.createDestination(d, msg);
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
