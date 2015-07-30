package com.adaptris.core.jms.jndi;

public class StandardJndiImplementationTest extends JndiImplementationCase {

  public StandardJndiImplementationTest(String name) {
    super(name);
  }

  @Override
  protected StandardJndiImplementation createVendorImplementation() {
    return new StandardJndiImplementation();
  }

}
