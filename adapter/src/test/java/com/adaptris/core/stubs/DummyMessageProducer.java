package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.util.license.License;

/**
 * Producer purely used for marshalling example-xml.
 */
public class DummyMessageProducer extends ProduceOnlyProducerImp {


  public DummyMessageProducer() {
  }

  public DummyMessageProducer(ProduceDestination d) {
    this();
    setDestination(d);
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
  }

  public void produce(AdaptrisMessage msg, ProduceDestination destination)
      throws ProduceException {
  }

  public void init() throws CoreException {
  }

  public void start() throws CoreException {
  }

  public void stop() {
  }

  public void close() {
  }

  @Override
  public String getUniqueId() {
    return null;
  }
}
