package com.adaptris.core.jdbc;

import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.CoreException;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.stream.Slf4jLoggingOutputStream;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @config jdbc-debug-pool-factory
 *
 */
@XStreamAlias("jdbc-debug-pool-factory")
public class DebugPoolFactory extends DefaultPoolFactory {
  private static final TimeInterval DEFAULT_MAX_UNRETURNED = new TimeInterval(1L, TimeUnit.DAYS);

  private TimeInterval unreturnedConnectionTimeout;
  private Boolean debugUnreturnedConnectionStackTraces;

  @Override
  public ComboPooledDataSource create(JdbcPoolConfiguration cfg) throws CoreException {
    ComboPooledDataSource result = super.create(cfg);
    try {

      result.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout());
      result.setDebugUnreturnedConnectionStackTraces(debugUnreturnedConnectionStackTraces());
      result.setLogWriter(new PrintWriter(new Slf4jLoggingOutputStream(logger, "DEBUG"), true));
    } catch (Exception ex) {
      throw new CoreException(ex);
    }
    return result;
  }

  /**
   * @return the unreturnedConnectionTimeout
   */
  public TimeInterval getUnreturnedConnectionTimeout() {
    return unreturnedConnectionTimeout;
  }

  /**
   * @param interval the unreturnedConnectionTimeout to set, default if not specified is 1 day.
   */
  public void setUnreturnedConnectionTimeout(TimeInterval interval) {
    this.unreturnedConnectionTimeout = interval;
  }

  int unreturnedConnectionTimeout() {
    return Long
        .valueOf(getUnreturnedConnectionTimeout() != null
            ? TimeUnit.MILLISECONDS.toSeconds(getUnreturnedConnectionTimeout().toMilliseconds())
        : TimeUnit.MILLISECONDS.toSeconds(DEFAULT_MAX_UNRETURNED.toMilliseconds())).intValue();
  }

  /**
   * @return the debugUnreturnedConnectionStackTraces
   */
  public Boolean getDebugUnreturnedConnectionStackTraces() {
    return debugUnreturnedConnectionStackTraces;
  }

  /**
   * @param onOff the debugUnreturnedConnectionStackTraces to set, default false if not specified.
   */
  public void setDebugUnreturnedConnectionStackTraces(Boolean onOff) {
    this.debugUnreturnedConnectionStackTraces = onOff;
  }

  boolean debugUnreturnedConnectionStackTraces() {
    return getDebugUnreturnedConnectionStackTraces() == null ? false : getDebugUnreturnedConnectionStackTraces().booleanValue();
  }
}
