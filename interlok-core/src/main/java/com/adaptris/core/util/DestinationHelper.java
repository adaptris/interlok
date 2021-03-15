package com.adaptris.core.util;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ProduceException;
import com.adaptris.core.util.LoggingHelper.WarningLoggedCallback;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class DestinationHelper {

  public static void mustHaveEither(String configured) {
    if (configured == null) {
      throw new IllegalArgumentException("Must have string configuration");
    }
  }

  /**
   * Get the configured consume destination
   *
   * <p>
   * Helper method so that consume destinations can still be supported in-situ with string based
   * configurations.
   * </p>
   * <p>
   * No longer accepts a legacy destination (as they no longer exists), but does perform any
   * validity checks on {@code configured}.
   * </p>
   *
   * @return {@code configured}.
   */
  @Deprecated
  public static String consumeDestination(final String configured) {
    return configured;
  }

  /**
   * Get the configured consume destination
   *
   * <p>
   * Helper method so that consume destinations can still be supported in-situ with string based
   * configurations.
   * </p>
   * <p>
   * No longer accepts a legacy destination (as they no longer exists), but does perform any
   * validity checks on {@code configured}.
   * </p>
   *
   * @return {@code configured}.
   */
  @Deprecated
  public static String filterExpression(final String configured) {
    return configured;
  }


  /**
   * Get the thread name.
   * <p>
   * Helper method so that consume destinations can still be supported in-situ with string based
   * configurations.
   * </p>
   * </p>
   * <p>
   * No longer accepts a legacy destination (as they no longer exists), but does perform any
   * validity checks on {@code listener}.
   * </p>
   *
   * @return {@code listener#friendlyName()}.
   */
  public static String threadName(final AdaptrisMessageListener listener) {
    // If nothings defined, then just use the current name.
    return threadName(listener, Thread.currentThread().getName());
  }

  /**
   * Get the thread name.
   * <p>
   * Helper method so that consume destinations can still be supported in-situ with string based
   * configurations.
   * </p>
   * <p>
   * No longer accepts a legacy destination (as they no longer exists), but does perform any
   * validity checks on {@code listener}.
   * </p>
   *
   * @return {@code listener#friendlyName()}.
   */
  public static String threadName(final AdaptrisMessageListener listener, String defaultName) {
    return Optional.ofNullable(listener).map((l) -> l.friendlyName()).orElse(defaultName);
  }

  /**
   * Get the correct produce destination.
   * <p>
   * Helper method so that produce destinations can still be supported in-situ with string based
   * configurations.
   * </p>
   *
   * @return {@code legacy.getDestination(msg)} if legacy is non-null;
   *         {@code msg.resolve(configured)} otherwise.
   */
  @Deprecated
  public static String resolveProduceDestination(String configured, AdaptrisMessage msg) throws ProduceException {
    return msg.resolve(configured);
  }

  /**
   * Log a warning if consume destination is not null.
   *
   * @see LoggingHelper#logWarning(boolean, WarningLoggedCallback, String, Object...)
   * @deprecated use
   *             {@code logWarningIfNotNull(boolean, WarningLoggedCallback, Object, String, Object...)}
   *             instead.
   */
  @Deprecated
  public static void logConsumeDestinationWarning(boolean alreadyLogged,
      WarningLoggedCallback callback,
      String text,
      Object... args) {
    logWarningIfNotNull(alreadyLogged, callback, null, text, args);
  }

  /**
   * Log a warning if the supplied object is not null.
   * <p>
   * Note that regardless of whether the object is null
   * {@link WarningLoggedCallback#warningLogged()} is always executed since we have checked to see
   * if we need to log a warning.
   * </p>
   *
   * @see LoggingHelper#logWarning(boolean, WarningLoggedCallback, String, Object...)
   */
  @Deprecated
  public static void logWarningIfNotNull(boolean alreadyLogged,
      WarningLoggedCallback callback, Object d, String text, Object... args) {
    if (d != null) {
      LoggingHelper.logWarning(alreadyLogged, callback, text, args);
    } else {
      callback.warningLogged();
    }
  }
}
