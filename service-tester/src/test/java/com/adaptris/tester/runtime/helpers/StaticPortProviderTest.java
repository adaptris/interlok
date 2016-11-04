package com.adaptris.tester.runtime.helpers;

import org.junit.Test;

public class StaticPortProviderTest extends PortProviderCase {

  private static final int PORT = 9999;

  public StaticPortProviderTest(String name) {
    super(name);
  }

  @Test
  public void testDefaultPort(){
    PortProvider pp = new StaticPortProvider();
    pp.initPort();
    assertEquals(8080, pp.getPort());
    pp.releasePort();
  }

  @Test
  public void testGetPort(){
    PortProvider pp = createPortProvider();
    pp.initPort();
    assertEquals(PORT, pp.getPort());
    pp.releasePort();
  }

  @Override
  protected PortProvider createPortProvider() {
    StaticPortProvider pp = new StaticPortProvider();
    pp.setPort(PORT);
    return pp;
  }
}