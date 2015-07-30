package com.adaptris.core.jms;

import javax.jms.JMSException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Default implementation of {@link ProducerSessionFactory}.
 * 
 * <p>
 * The default implementation creates a single session, and reuses that until restarted.
 * </p>
 * 
 * @config jms-default-producer-session
 * @license BASIC
 * @author lchan
 * 
 */
@XStreamAlias("jms-default-producer-session")
public class DefaultProducerSessionFactory extends ProducerSessionFactoryImpl {

  public DefaultProducerSessionFactory() {
    super();
  }

  @Override
  public boolean isEnabled(License l) {
    return l.isEnabled(LicenseType.Basic);
  }

  @Override
  public ProducerSession createProducerSession(JmsProducerImpl producer, AdaptrisMessage msg)
      throws JMSException {
    if (session == null) {
      session = createProducerSession(producer);
    }
    return session;
  }

}
