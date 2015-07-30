package com.adaptris.core.jms;

import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Wrapper around a {@link MessageProducer} and {@link Session}.
 * 
 * @author lchan
 * 
 */
public abstract class ProducerSession {


  public abstract Session getSession();

  public abstract MessageProducer getProducer();

}
