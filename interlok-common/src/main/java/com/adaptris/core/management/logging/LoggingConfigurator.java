package com.adaptris.core.management.logging;


import org.slf4j.bridge.SLF4JBridgeHandler;

public abstract class LoggingConfigurator {

  // painful code duplication from its original home.
  protected static final boolean DBG = Boolean.getBoolean("adp.bootstrap.debug") || Boolean.getBoolean("interlok.bootstrap.debug");
  public static final boolean ENABLE_JUL_LOGGING_BRIDGE = Boolean.getBoolean("jul.log4j.bridge");

  static enum AvailableLoggingImpls {
    LOG4J_2() {
      @Override
      boolean available() {
        boolean rc = false;
        try {
          Class.forName("org.apache.logging.log4j.LogManager");
          rc = true;
        } catch (Exception e) {
          rc = false;
        }
        return rc;
      }

      @Override
      LoggingConfigurator create() {
        return new Log4jConfigurator();
      }
    },
    DEFAULT() {
      @Override
      boolean available() {
        return true;
      }

      @Override
      LoggingConfigurator create() {
        return new LoggingConfigurator() {
          @Override
          public void defaultInitialisation() {
          }

          @Override
          public boolean initialiseFrom(String url) {
            return true;
          }

          @Override
          public void requestShutdown() {
          }
        };
      }
    };
    
    abstract boolean available();
    
    abstract LoggingConfigurator create();
  }
  private static LoggingConfigurator configurator = null;

  public abstract void defaultInitialisation();

  public abstract boolean initialiseFrom(String url);

  public abstract void requestShutdown();

  protected void bridgeJavaUtilLogging() {
    if (ENABLE_JUL_LOGGING_BRIDGE) {
      if (!SLF4JBridgeHandler.isInstalled()) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        if (DBG) {
          System.err.println("(Info) LoggingConfigurator : java.util.logging -> slf4j bridge installed");
        }
      }
    }
  }

  public static synchronized LoggingConfigurator newConfigurator() {
    if (configurator == null) {
      configurator = createInstance();
    }
    return configurator;
  }

  private static LoggingConfigurator createInstance() {
    LoggingConfigurator result = null;
    for (AvailableLoggingImpls imp : AvailableLoggingImpls.values()) {
      if (imp.available()) {
        result = imp.create();
        break;
      }
    }
    return result;
  }
}
