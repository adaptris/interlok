package com.adaptris.core.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.util.text.DateFormatUtil;

public class CacheExpiryTest extends CacheExpiry {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testBuildExpiry() {
    assertEquals(Optional.empty(), CacheExpiry.buildExpiry(null));
    // Should be relative
    Optional<Expiry> o1 = CacheExpiry.buildExpiry("2000");
    assertTrue(o1.isPresent());
    assertTrue(o1.get().expiresIn().toMilliseconds() > 500);
    Optional<Expiry> o2 = CacheExpiry.buildExpiry(System.currentTimeMillis() + "2000");
    assertTrue(o2.isPresent());
    assertTrue(o2.get().expiresIn().toMilliseconds() > 500);
    Optional<Expiry> o3 = CacheExpiry.buildExpiry(DateFormatUtil.format(new Date(System.currentTimeMillis() + 2000)));
    assertTrue(o3.isPresent());
    System.err.println(o3.get().expiresIn());
    assertTrue(o3.get().expiresIn().toMilliseconds() > 500);
  }

}
