package com.adaptris.core.jms.jndi;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * Interface that allows you to configure the ConnectionFactory that is returned from a {@link StandardJndiImplementation}.
 * <p>
 * Generally speaking, this is not encouraged, as you are now keeping configuration in 2 separate locations (both JNDI and adapter
 * config). The ConnectionFactory should ideally be configured in JNDI with all the settings that are required for each connection.
 * </p>
 *
 * @author lchan
 *
 */
public interface ExtraFactoryConfiguration {

  /**
   * Apply any additonal configuration to the TopicConnectionFactory.
   * 
   * @param cf the topic connection factory
   */
  void applyConfiguration(ConnectionFactory cf) throws JMSException;

}
