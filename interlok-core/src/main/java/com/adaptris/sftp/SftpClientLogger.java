package com.adaptris.sftp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.MarkerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access=AccessLevel.PACKAGE)
public class SftpClientLogger implements com.jcraft.jsch.Logger {

  private static enum Slf4jLoggingProxy {
    FATAL {
      @Override
      boolean isEnabled() {
        return true;
      }
      @Override
      void log(String s) {
        log.error(MarkerFactory.getMarker("FATAL"), s);
      }
    },
    ERROR {
      @Override
      boolean isEnabled() {
        return log.isErrorEnabled();
      }
      @Override
      void log(String s) {
        log.error(s);
      }
    },
    WARNING {
      @Override
      boolean isEnabled() {
        return log.isWarnEnabled();
      }
      @Override
      void log(String s) {
        log.warn(s);
      }
    },
    INFO {
      @Override
      boolean isEnabled() {
        return log.isInfoEnabled();
      }
      @Override
      void log(String s) {
        log.info(s);
      }
    },
    DEBUG {
      @Override
      boolean isEnabled() {
        return log.isDebugEnabled();
      }
      @Override
      void log(String s) {
        log.debug(s);
      }
    };

    abstract void log(String s);
    abstract boolean isEnabled();
  }

  private static final Map<Integer,Slf4jLoggingProxy> loggers;


  static {
    Map<Integer,Slf4jLoggingProxy> m = new HashMap<>(5);
    m.put(com.jcraft.jsch.Logger.FATAL, Slf4jLoggingProxy.FATAL);
    m.put(com.jcraft.jsch.Logger.ERROR, Slf4jLoggingProxy.ERROR);
    m.put(com.jcraft.jsch.Logger.WARN, Slf4jLoggingProxy.WARNING);
    m.put(com.jcraft.jsch.Logger.INFO, Slf4jLoggingProxy.INFO);
    m.put(com.jcraft.jsch.Logger.DEBUG, Slf4jLoggingProxy.DEBUG);
    loggers = Collections.unmodifiableMap(m);
  }


  @Override
  public boolean isEnabled(int level) {
    return loggers.get(level).isEnabled();
  }

  @Override
  public void log(int level, String message) {
    loggers.get(level).log(message);
  }

}
