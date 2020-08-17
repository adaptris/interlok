package com.adaptris.core.jms;

import javax.jms.Destination;
import javax.jms.JMSException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;

public class MockProducer extends DefinedJmsProducer {

  @Override
  protected Destination createDestination(String name) throws JMSException {
    throw new JMSException("NO!");
  }

  @Override
  protected void doProduce(AdaptrisMessage msg, Destination dest, Destination replyTo)
      throws JMSException, CoreException {
    throw new ProduceException();
  }

  @Override
  public AdaptrisMessage request(AdaptrisMessage msg, ProduceDestination dest, long timeout)
      throws ProduceException {
    throw new ProduceException();
  }

  @Override
  protected Destination createTemporaryDestination() throws JMSException {
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
}
