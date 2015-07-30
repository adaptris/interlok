package com.adaptris.core.jms.jndi;

public class CachedDestinationJndiImplementationTest extends JndiImplementationCase {

  public CachedDestinationJndiImplementationTest(String name) {
    super(name);
  }

  @Override
  protected StandardJndiImplementation createVendorImplementation() {
    return new CachedDestinationJndiImplementation();
  }

}
