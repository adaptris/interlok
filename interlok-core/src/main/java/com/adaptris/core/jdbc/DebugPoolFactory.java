package com.adaptris.core.jdbc;

import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.stream.Slf4jLoggingOutputStream;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Constructs a C3P0 connection pool with various debug settings enabled.
 * 
 * @config jdbc-debug-pool-factory
 *
 */
@XStreamAlias("jdbc-debug-pool-factory")
@ComponentProfile(summary = "Build a connection pool using C3P0 with some debug enabled")
public class DebugPoolFactory extends DefaultPoolFactory {
  private static final TimeInterval DEFAULT_MAX_UNRETURNED = new TimeInterval(1L, TimeUnit.DAYS);

  @InputFieldDefault(value = "1 Day")
  @AdvancedConfig
  private TimeInterval unreturnedConnectionTimeout;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean debugUnreturnedConnectionStackTraces;

  @Override
  public ComboPooledDataSource create() throws Exception {
    ComboPooledDataSource result = super.create();
    result.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout());
    result.setDebugUnreturnedConnectionStackTraces(debugUnreturnedConnectionStackTraces());
    result.setLogWriter(new PrintWriter(new Slf4jLoggingOutputStream(logger, "DEBUG"), true)); // lgtm
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

  public DebugPoolFactory withUnreturnedConnectionTimeout(TimeInterval interval) {
    setUnreturnedConnectionTimeout(interval);
    return this;
  }

  private int unreturnedConnectionTimeout() {
    return Long.valueOf(TimeInterval.toSecondsDefaultIfNull(getUnreturnedConnectionTimeout(),
        DEFAULT_MAX_UNRETURNED)).intValue();
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

  public DebugPoolFactory withDebugUnreturnedConnectionStackTraces(Boolean b) {
    setDebugUnreturnedConnectionStackTraces(b);
    return this;
  }


  private boolean debugUnreturnedConnectionStackTraces() {
    return BooleanUtils.toBooleanDefaultIfNull(getDebugUnreturnedConnectionStackTraces(), false);
  }
}
