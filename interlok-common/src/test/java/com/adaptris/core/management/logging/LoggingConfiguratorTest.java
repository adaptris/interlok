package com.adaptris.core.management.logging;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.adaptris.core.management.logging.LoggingConfigurator.AvailableLoggingImpls;

public class LoggingConfiguratorTest {

  @Test
  public void testNewConfigurator() {
    LoggingConfigurator l1 = LoggingConfigurator.newConfigurator();
    assertNotNull(l1);
    LoggingConfigurator l2 = LoggingConfigurator.newConfigurator();
    assertSame(l1, l2);
  }

  @Test
  public void testDefaultConfigurator() {
    assertTrue(AvailableLoggingImpls.DEFAULT.available());
    assertNotNull(AvailableLoggingImpls.DEFAULT.create());
    LoggingConfigurator cfg = AvailableLoggingImpls.DEFAULT.create();
    cfg.bridgeJavaUtilLogging();
    cfg.defaultInitialisation();
    cfg.initialiseFrom("log4j2.xml");
  }

  @Test
  public void testLog4jConfigurator() {
    assertTrue(AvailableLoggingImpls.LOG4J_2.available());
    assertNotNull(AvailableLoggingImpls.LOG4J_2.create());
    LoggingConfigurator cfg = AvailableLoggingImpls.LOG4J_2.create();
    cfg.bridgeJavaUtilLogging();
    cfg.defaultInitialisation();
    cfg.initialiseFrom("log4j2.xml");
  }

}
