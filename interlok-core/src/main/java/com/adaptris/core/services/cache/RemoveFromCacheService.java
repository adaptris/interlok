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

import static org.apache.commons.lang.StringUtils.isEmpty;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service that retrieves an item from the cache and then removes it
 * 
 * @config remove-from-cache
 * @author stuellidge
 * 
 */
@XStreamAlias("remove-from-cache")
@AdapterComponent
@ComponentProfile(summary = "Retrieve values from a cache, and remove those values from the cache", tag = "service,cache",
    recommended = {CacheConnection.class})
public class RemoveFromCacheService extends RetrieveFromCacheService {

  /**
   * Retrieves the item from the cache, stores it against the message and then removes it from the cache
   *
   * @see com.adaptris.core.services.cache.RetrieveFromCacheService#doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      Cache cache = retrieveCache();
      for (CacheEntryEvaluator ceg : getCacheEntryEvaluators()) {
        String key = ceg.getKey(msg);
        if (isEmpty(key)) {
          log.warn("{} generated null values for the key, nothing to do", ceg.friendlyName());
          continue;
        }
        addCacheValueToMessage(msg, key, ceg.valueTranslator(), !exceptionIfNotFound());
        cache.remove(key);
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

}
