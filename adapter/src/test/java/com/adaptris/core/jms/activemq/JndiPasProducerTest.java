package com.adaptris.core.jms.activemq;

import com.adaptris.core.jms.jndi.StandardJndiImplementation;

public class JndiPasProducerTest extends JndiPasProducerCase {

  public JndiPasProducerTest(String name) {
    super(name);
  }

  @Override
  protected StandardJndiImplementation createVendorImplementation() {
    return new StandardJndiImplementation();
  }

}
