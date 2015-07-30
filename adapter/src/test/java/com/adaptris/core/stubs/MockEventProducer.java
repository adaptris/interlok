/*
 * $RCSfile: MockMessageProducer.java,v $
 * $Revision: 1.12 $
 * $Date: 2008/10/28 13:34:30 $
 * $Author: lchan $
 */
package com.adaptris.core.stubs;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMarshaller;
import com.adaptris.core.Event;
import com.adaptris.core.ProduceException;

/**
 * Mock implementation of <code>AdaptrisMessageProducer</code> for testing events. Allows you to only keep the events that you're
 * interested in.
 */
public class MockEventProducer extends MockMessageProducer {

  private transient AdaptrisMarshaller eventMarshaller;
  private Set<Class> eventsToKeep = new HashSet<Class>();

  public MockEventProducer() throws CoreException {
    eventMarshaller = DefaultMarshaller.getDefaultMarshaller();
  }

  public MockEventProducer(Collection<Class> eventsToKeep) throws CoreException {
    this();
    this.eventsToKeep = new HashSet<Class>(eventsToKeep);
  }

  /**
   * @see com.adaptris.core.AdaptrisMessageProducer#produce (com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void produce(AdaptrisMessage msg) throws ProduceException {
    if (msg == null) {
      throw new ProduceException("msg is null");
    }
    if (keepMessage(msg)) {
      super.produce(msg);
    }
  }

  protected boolean keepMessage(AdaptrisMessage m) throws ProduceException {
    if (eventsToKeep.size() == 0) return true;
    InputStream in = null;
    try {
      in = m.getInputStream();
      Event event = (Event) eventMarshaller.unmarshal(m.getInputStream());
      if (eventsToKeep.contains(event.getClass())) {
        log.trace(event.getClass() + " matches filter, keeping it");
        return true;
      }
    }
    catch (Exception e) {
      throw new ProduceException(e);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
    return false;
  }
}
