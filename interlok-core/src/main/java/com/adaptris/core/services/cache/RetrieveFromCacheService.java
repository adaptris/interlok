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

import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service that looks up an object from the cache using the configured {@link CacheEntryEvaluator} instances.
 * 
 * <p>
 * This service uses its configured {@link CacheEntryEvaluator} instances to lookup keys stored in the cache, retrieves the value
 * and then stores it against the message using the supplied value translator.
 * </p>
 * 
 * @config retrieve-from-cache
 * @author stuellidge
 * 
 */
@XStreamAlias("retrieve-from-cache")
@AdapterComponent
@ComponentProfile(summary = "Retrieve values from the cache", tag = "service,cache", recommended = {CacheConnection.class})
public class RetrieveFromCacheService extends CacheServiceBase {

  private Boolean exceptionIfNotFound;

  public RetrieveFromCacheService() {
  }

  /**
   * Looks up an object from the cache and then stores it in the metadata
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      for (CacheEntryEvaluator ceg : getCacheEntryEvaluators()) {
        String key = ceg.getKey(msg);
        if (isEmpty(key)) {
          log.warn("{} generated null value for the key, nothing to do", ceg.friendlyName());
          continue;
        }
        addCacheValueToMessage(msg, key, ceg.valueTranslator(), !exceptionIfNotFound());
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }


  public Boolean getExceptionIfNotFound() {
    return exceptionIfNotFound;
  }

  /**
   * Whether or not to throw an exception if the key is not in the cache.
   *
   * @param b default is true.
   */
  public void setExceptionIfNotFound(Boolean b) {
    exceptionIfNotFound = b;
  }

  boolean exceptionIfNotFound() {
    return BooleanUtils.toBooleanDefaultIfNull(getExceptionIfNotFound(), true);
  }

}
