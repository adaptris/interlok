package com.adaptris.core.jms.jndi;

import javax.jms.ConnectionFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ExtraFactoryConfiguration} implementation that does nothing.
 * 
 * @config no-op-jndi-factory-configuration
 * @author lchan
 * 
 */
@XStreamAlias("no-op-jndi-factory-configuration")
public class NoOpFactoryConfiguration implements ExtraFactoryConfiguration {

  @Override
  public void applyConfiguration(ConnectionFactory tcf) {
  }

}
