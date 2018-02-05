package com.adaptris.core.management.logging;

import static com.adaptris.core.management.Constants.DBG;
import static com.adaptris.core.management.Constants.ENABLE_JUL_LOGGING_BRIDGE;

import org.slf4j.bridge.SLF4JBridgeHandler;

public abstract class LoggingConfigurator {

  private static enum AvailableLoggingImpls {
    LOG4J_2() {
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

      LoggingConfigurator create() {
        return new Log4jConfigurator();
      }
    },
    DEFAULT() {
      boolean available() {
        return true;
      }

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
