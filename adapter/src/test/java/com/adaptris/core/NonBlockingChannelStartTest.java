package com.adaptris.core;

import com.adaptris.core.lifecycle.NonBlockingChannelStartStrategy;

public class NonBlockingChannelStartTest extends DefaultLifecycleStrategyTest {

  public NonBlockingChannelStartTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected ChannelLifecycleStrategy createStrategy() {
    return new NonBlockingChannelStartStrategy();
  }
}
