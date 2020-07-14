package com.adaptris.core.util;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.util.LoggingHelper.WarningLoggedCallback;

public class DestinationHelper {

  public static void mustHaveEither(String configured, ConsumeDestination legacy) {
    if (legacy == null && configured == null) {
      throw new IllegalArgumentException("Must have string configuration or ConsumeDestination");
    }
  }

  public static void mustHaveEither(String configured, ProduceDestination legacy) {
    if (legacy == null && configured == null) {
      throw new IllegalArgumentException("Must either string configuration or ProduceDestination");
    }
  }

  /**
   * Get the configured consume destination
   *
   * <p>
   * Helper method so that consume destinations can still be supported in-situ with string based
   * configurations.
   * </p>
   *
   * @return {@code legacy#getDestination()} if legacy is non-null; {@code configured} otherwise.
   */
  public static String consumeDestination(final String configured,
      final ConsumeDestination legacy) {
    return Optional.ofNullable(legacy).map((d) -> d.getDestination()).orElse(configured);
  }

  /**
   * Get the configured consume destination
   *
   * <p>
   * Helper method so that consume destinations can still be supported in-situ with string based
   * configurations.
   * </p>
   *
   * @return {@code legacy#getFilterExpression()} if legacy is non-null; {@code configured}
   *         otherwise.
   */
  public static String filterExpression(final String configured,
      final ConsumeDestination legacy) {
    return Optional.ofNullable(legacy).map((d) -> d.getFilterExpression()).orElse(configured);
  }


  /**
   * Get the thread name.
   * <p>
   * Helper method so that consume destinations can still be supported in-situ with string based
   * configurations.
   * </p>
   *
   * @return {@code legacy#getDestination()} if legacy is non-null; {@code listener#friendlyName()}
   *         otherwise.
   */
  public static String threadName(final AdaptrisMessageListener listener, final ConsumeDestination legacy) {
    // If nothings defined, then just use the current name.
    return threadName(listener, legacy, Thread.currentThread().getName());
  }

  /**
   * Get the thread name.
   * <p>
   * Helper method so that consume destinations can still be supported in-situ with string based
   * configurations.
   * </p>
   *
   * @return {@code legacy#getDestination()} if legacy is non-null; {@code listener#friendlyName()}
   *         otherwise.
   */
  public static String threadName(final AdaptrisMessageListener listener,
      final ConsumeDestination legacy, String defaultName) {
    String threadName =
        Optional.ofNullable(listener).map((l) -> l.friendlyName()).orElse(defaultName);
    return Optional.ofNullable(legacy).map((d) -> d.getDeliveryThreadName())
        .filter((s) -> StringUtils.isNotEmpty(s)).orElse(threadName);
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
  public static String resolveProduceDestination(String configured, ProduceDestination legacy,
      AdaptrisMessage msg) throws ProduceException {
    // I feel a little sad I can't use Optional, and SneakyThrows doesn't made the code more
    // readable.
    try {
      if (legacy != null) {
        return legacy.getDestination(msg);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
    return msg.resolve(configured);
  }

  /**
   * Log a warning if consume destination is not null.
   *
   * @see LoggingHelper#logWarning(boolean, WarningLoggedCallback, String, Object...)
   * @deprecated use
   *             {@link #logWarningIfNotNull(boolean, WarningLoggedCallback, Object, String, Object...)}
   *             instead.
   */
  @Deprecated
  public static void logConsumeDestinationWarning(boolean alreadyLogged,
      WarningLoggedCallback callback,
      ConsumeDestination d, String text,
      Object... args) {
    logWarningIfNotNull(alreadyLogged, callback, d, text, args);
  }

  /**
   * Log a warning if the supplied object is not null.
   *
   * @see LoggingHelper#logWarning(boolean, WarningLoggedCallback, String, Object...)
   */
  public static void logWarningIfNotNull(boolean alreadyLogged,
      WarningLoggedCallback callback, Object d, String text, Object... args) {
    if (d != null) {
      LoggingHelper.logWarning(alreadyLogged, callback, text, args);
    }
  }
}
