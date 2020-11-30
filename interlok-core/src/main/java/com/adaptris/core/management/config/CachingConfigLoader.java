/*
 * Copyright 2015 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.management.config;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import com.adaptris.core.config.ConfigPreProcessorLoader;
import com.adaptris.core.config.DefaultPreProcessorLoader;
import com.adaptris.core.management.BootstrapProperties;
import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

/**
 * Helper to avoid multiple executions of
 * {@link DefaultPreProcessorLoader#load(BootstrapProperties)}.
 * <p>
 * Finds the adapter resource name, that's the key to the cache; and loads the configuration once.
 * This is purely to avoid multiple executions of the preprocessor when in a configuration check
 * chain.
 * </p>
 *
 * @since 3.11.1
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CachingConfigLoader {

  // Artificially small; but since the JVM will terminate we might not care so much.
  private static final ExpiringMap<String, String> CONFIG_CACHE =
      ExpiringMap.builder().maxSize(2)
          .expirationPolicy(ExpirationPolicy.ACCESSED).expiration(10L, TimeUnit.MINUTES).build();

  /**
   * Helper method to avoid multiple executions of
   * {@link DefaultPreProcessorLoader#load(BootstrapProperties)}.
   * <p>
   * Finds the adapter resource name, that's the key to the cache; and loads the configuration once.
   * This is purely to avoid multiple executions of the preprocessor when in a configuration check
   * chain.
   * </p>
   */
  public static String loadInterlokConfig(BootstrapProperties config) {
    Optional<String> cacheKey = Optional.ofNullable(config.findAdapterResource());
    return cacheKey.map((key) -> loadOrGet(key, config))
        .orElseGet(() -> sneakyLoad(config));
  }

  protected static int cacheSize() {
    return CONFIG_CACHE.size();
  }

  @SneakyThrows(Exception.class)
  @Generated
  private static String sneakyLoad(BootstrapProperties config) {
    return ConfigPreProcessorLoader.loadInterlokConfig(config);
  }


  private static synchronized String loadOrGet(String key, BootstrapProperties config) {
    String xml = "";
    if (CONFIG_CACHE.containsKey(key)) {
      xml = CONFIG_CACHE.get(key);
    } else {
      xml = sneakyLoad(config);
      CONFIG_CACHE.put(key, xml);
    }
    return xml;
  }
}
