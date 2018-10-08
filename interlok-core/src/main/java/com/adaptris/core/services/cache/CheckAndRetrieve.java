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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.cache.Cache;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Service} implementation that checks if specific key(s) exist in the cache and retrieves.
 * 
 * @config check-cache-and-retrieve
 * @since 3.6.4
 */
@XStreamAlias("check-cache-and-retrieve")
@AdapterComponent
@ComponentProfile(summary = "Check the cache for a key and retrieve it if it exists", tag = "service,cache", branchSelector = true, since = "3.6.4")
@DisplayOrder(order = { "keysFoundServiceId", "keysNotFoundServiceId" })
public class CheckAndRetrieve extends CheckCacheService {

  public CheckAndRetrieve() {
  }

  @Override
  protected boolean eval(AdaptrisMessage msg) throws CoreException {
    int count = 0;
    int required = getCacheEntryEvaluators().size();
    Cache cache = retrieveCache();
    for (CacheEntryEvaluator ceg : getCacheEntryEvaluators()) {
      String key = (String) ceg.getKey(msg);
      count += cache.getKeys().contains(key) ? 1 : 0;
      addCacheValueToMessage(msg, key, ceg.valueTranslator(), true);
    }
    return count == required;
  }

}
