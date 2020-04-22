package com.adaptris.core.cache;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.DateFormatUtil;

public class CacheExpiry {
  
  private static final List<ExpiryParser> EXPIRY_PARSERS =
      Collections.unmodifiableList(
          Arrays.asList(new ExpiryParser[] {
          (val) -> build(val),
          (val) -> build(DateFormatUtil.parse(val, null))
      }));
  

  private static Optional<Expiry> build(final Date date) {
    return Optional.ofNullable(date).map(d -> Optional.of(new Expiry(d))).orElse(Optional.empty());
  }

  private static Optional<Expiry> build(final String millis) {
    // don't use NumberUtils since we don't want to support a - number...
    return Optional.ofNullable(millis).filter(t -> t.chars().allMatch(c -> c >= '0' && c <= '9'))
        .map(t -> Optional.of(new Expiry(Long.parseLong(t)))).orElse(Optional.empty());
  }

  /**
   * Build an expiry from the provided value.
   * 
   * <p>
   * If the value is null or unparseable then {@link Optional#empty()} is returned otherwise the behaviour is :
   * </p>
   * <ul>
   * <li>If the value is numeric and less than {@link System#currentTimeMillis()} then it is treated as relative to {@code now()};
   * expiry is in milliseconds</li>
   * <li>If the value is numberic and greater than {@link System#currentTimeMillis()} then it is treated as absolute; expiry is in
   * milliseconds</li>
   * <li>If the value is a recognised value from {@link DateFormatUtil#parse(String)} then it is treated as absolute</li>
   * <li>If it can't be resolved by any of those means, then it is ignored (i.e. treated as though there is no expiry).
   * </ul>
   * </p>
   * 
   * @param value the value.
   * @return an Optional containing the expiry.
   */
  public static Optional<Expiry> buildExpiry(String value) {
    return EXPIRY_PARSERS.stream().map((p) -> p.parse(value)).filter((o) -> o.isPresent()).findFirst().orElse(Optional.empty());
  }
  
  public static class Expiry {
    private transient TimeInterval expiry;

    private Expiry(long absoluteOrRelativeMs) {
      long now = System.currentTimeMillis();
      if (absoluteOrRelativeMs < now) {
        expiry = new TimeInterval(absoluteOrRelativeMs, TimeUnit.MILLISECONDS);
      } else {
        expiry = new TimeInterval(absoluteOrRelativeMs - now, TimeUnit.MILLISECONDS);
      }
    }

    private Expiry(Date futureDate) {
      Date now = new Date();
      expiry = new TimeInterval(futureDate.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
    }

    public TimeInterval expiresIn() {
      return expiry;
    }
  }


  // Can't do an array of generic functions so new Function<String,Optional<ExpiryWrapper>>[] isn't loved by the
  // compiler. This is basically a Function though.
  @FunctionalInterface
  private static interface ExpiryParser {
    Optional<Expiry> parse(String s);
  }


}
