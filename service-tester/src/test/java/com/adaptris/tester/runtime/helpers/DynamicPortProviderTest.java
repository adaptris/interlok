package com.adaptris.tester.runtime.helpers;

import org.junit.Test;

public class DynamicPortProviderTest extends PortProviderCase {

  private static final int PORT = 8080;

  public DynamicPortProviderTest(String name) {
    super(name);
  }


  @Test
  public void testDefaultOffset(){
    DynamicPortProvider pp = new DynamicPortProvider();
    pp.initPort();
    assertEquals(8080, pp.getOffset());
    assertTrue(8080 <= pp.getPort());
    pp.releasePort();
  }

  @Test
  public void testGetPort(){
    DynamicPortProvider pp = createPortProvider();
    pp.initPort();
    assertEquals(PORT, pp.getOffset());
    assertTrue(PORT <= pp.getPort());
    pp.releasePort();
  }

  @Override
  protected DynamicPortProvider createPortProvider() {
    DynamicPortProvider pp = new DynamicPortProvider();
    pp.setOffset(PORT);
    return pp;
  }
}