/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.services.cache;

import java.io.Serializable;

import org.apache.commons.lang.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Service} implementation that adds entries to a cache based on its configured {@link CacheEntryEvaluator}s.
 * <p>
 * The standard use case for this is if you wish to cache certain information across workflows (e.g. a CorrelationId). In your
 * source workflow you would use this service to add your required value into the cache. Subsequent workflows could then use
 * {@link RetrieveFromCacheService} or {@link RemoveFromCacheService} to subsequently retrieve those values from the cache.
 * </p>
 * 
 * @config add-to-cache
 * @author stuellidge
 * @see com.adaptris.core.cache.Cache
 * @see RemoveFromCacheService
 * @see RetrieveFromCacheService
 */
@XStreamAlias("add-to-cache")
@AdapterComponent
@ComponentProfile(summary = "Add values to a cache", tag = "service,cache")
@DisplayOrder(order = { "enforceSerializable" })
public class AddToCacheService extends CacheServiceBase {

  @InputFieldDefault(value = "false")
  private Boolean enforceSerializable;

  public AddToCacheService() {
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      Cache cache = retrieveCache();
      for (CacheEntryEvaluator ceg : getCacheEntryEvaluators()) {
        String key = ceg.getKey(msg);
        Object value = ceg.getValue(msg);
        if (key == null || value == null) {
          log.warn("{} generated null values for either the key or value, not storing in cache", ceg.friendlyName());
          continue;
        }
        if (enforceSerializable() && !(value instanceof Serializable)) {
          throw new ServiceException("Cache value " + value + " should be Serializable, but is of type " + value.getClass());
        }
        cache.put(key, value);
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  /**
   * If this is set to true then the service will only attempt to cache values that are {@link Serializable}.
   * 
   * <p>
   * An exception will be thrown if any non-serializable object is attempted to be cached. This is to enable non-memory based
   * caching engines to be used (where serialization may be used to store the value)
   * </p>
   * 
   * @param bool default is false.
   */
  public void setEnforceSerializable(Boolean bool) {
    enforceSerializable = bool;
  }

  public Boolean getEnforceSerializable() {
    return enforceSerializable;
  }

  public boolean enforceSerializable() {
    return BooleanUtils.toBooleanDefaultIfNull(getEnforceSerializable(), false);
  }
}
