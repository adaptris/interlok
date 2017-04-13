package com.adaptris.core.management.logging;

import static com.adaptris.core.management.Constants.DBG;
import static com.adaptris.core.management.Constants.ENABLE_JUL_LOGGING_BRIDGE;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import org.slf4j.bridge.SLF4JBridgeHandler;

public abstract class LoggingConfigurator {

  private static enum AvailableLoggingImpls {
    LOG4J_12() {
      boolean available() {
        boolean rc = false;
        try {
          Class.forName("org.apache.log4j.xml.DOMConfigurator");
          // Try for the helper class as well, because with log4j2 + log4j bridge, DOMConfigurator
          // might exist.
          Class.forName("org.apache.log4j.helpers.Loader");
          rc = true;
        } catch (Exception e) {
          rc = false;
        }
        return rc;
      }

      LoggingConfigurator create() {
        return new LegacyLog4jConfigurator();
      }
    },
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
          public boolean initialiseFrom(URL url) {
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


  protected File createFileReference(URL url, String charset) throws UnsupportedEncodingException {
    String charSetToUse = charset == null ? System.getProperty("file.encoding") : charset;
    String filename = URLDecoder.decode(url.getPath(), charSetToUse);
    // Cope with file://localhost/./config/blah -> /./config/blah is the result of getPath()
    // Munge that properly.
    if (filename.startsWith("/.")) {
      filename = filename.substring(1);
    }
    return new File(filename);
  }

  public abstract void defaultInitialisation();

  public abstract boolean initialiseFrom(URL url);

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
