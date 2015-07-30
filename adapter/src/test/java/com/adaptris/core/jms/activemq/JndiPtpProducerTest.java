package com.adaptris.core.jms.activemq;

import com.adaptris.core.jms.jndi.StandardJndiImplementation;

public class JndiPtpProducerTest extends JndiPtpProducerCase {

  public JndiPtpProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected StandardJndiImplementation createVendorImplementation() {
    return new StandardJndiImplementation();
  }

}
