package com.adaptris.core.jms;

import javax.jms.JMSException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link ProducerSessionFactory} that creates a new session every time is produced.
 * 
 * 
 * @config jms-per-message-producer-session
 * @license BASIC
 * @author lchan
 * 
 */
@XStreamAlias("jms-per-message-producer-session")
public class PerMessageProducerSessionFactory extends ProducerSessionFactoryImpl {

  public PerMessageProducerSessionFactory() {
    super();
  }

  @Override
  public boolean isEnabled(License l) {
    return l.isEnabled(LicenseType.Basic);
  }

  @Override
  public ProducerSession createProducerSession(JmsProducerImpl producer, AdaptrisMessage msg)
      throws JMSException {
    closeQuietly(session);
    session = createProducerSession(producer);
    return session;
  }

}
