package com.adaptris.core.management.config;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import org.junit.Test;
import com.adaptris.core.management.BootstrapProperties;

public class CachingConfigLoaderTest {

  @Test
  public void testLoad_NoKey() throws Exception {
    BootstrapProperties mockBootProperties = new MockBootProperties("some-data");
    assertEquals("some-data", CachingConfigLoader.loadInterlokConfig(mockBootProperties));
  }

  @Test
  public void testLoad_WithKey() throws Exception {
    BootstrapProperties mockBootProperties =
        new MockBootProperties("some-data", Arrays.asList("adapterResourceName"));
    assertEquals("some-data", CachingConfigLoader.loadInterlokConfig(mockBootProperties));
    assertEquals(1, CachingConfigLoader.cacheSize());
    assertEquals("some-data", CachingConfigLoader.loadInterlokConfig(mockBootProperties));
  }

}
