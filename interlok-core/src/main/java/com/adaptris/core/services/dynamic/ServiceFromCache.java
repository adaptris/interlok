/*******************************************************************************
 * Copyright 2019 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.core.services.dynamic;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.services.cache.CacheConnection;
import com.adaptris.core.services.cache.RetrieveFromCacheService;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Extract the service to execute from a cache
 * 
 * <p>
 * This allows you to retrieve a service (stored as a String) from a configured {@link Cache}
 * instance; it supports the expression syntax so you can build up the key for the cache from
 * metadata or similar. It will not remove the cache entry.
 * </p>
 * <p>
 * The alternative to this would be to use {@link RetrieveFromCacheService} and subsequently a
 * {@link ServiceFromDataInputParameter} (from metadata). How the dynamic services are inserted into
 * the cache is up to you.
 * </p>
 * 
 * @config dynamic-service-from-cache
 * @see DynamicServiceExecutor
 * 
 */
@XStreamAlias("dynamic-service-from-cache")
@ComponentProfile(summary = "Extract the service to execute from a cache",
    recommended = {CacheConnection.class})
@DisplayOrder(order = {"key", "connection"})
public class ServiceFromCache extends ExtractorWithConnection {

  @NotBlank
  @InputFieldHint(expression = true)
  private String key;

  public ServiceFromCache() {

  }

  @Override
  public InputStream getInputStream(AdaptrisMessage msg) throws Exception {
    Args.notBlank(getKey(), "key");
    Cache cache = getConnection().retrieveConnection(CacheConnection.class).retrieveCache();
    String service = (String) cache.get(msg.resolve(getKey()));
    return IOUtils.toInputStream(service, msg.getContentEncoding());
  }

  public String getKey() {
    return key;
  }

  public void setKey(String query) {
    this.key = Args.notBlank(query, "query");
  }

  public ServiceFromCache withKey(String q) {
    setKey(q);
    return this;
  }
}
