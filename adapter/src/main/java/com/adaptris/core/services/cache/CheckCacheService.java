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

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link Service} implementation that checks if specific key(s) exist in the cache.
 * 
 * @config check-cache
 * @since 3.5.1
 */
@XStreamAlias("check-cache")
@AdapterComponent
@ComponentProfile(summary = "Check the cache for a key", tag = "service,cache", branchSelector = true, since = "3.5.1")
@DisplayOrder(order = { "keysFoundServiceId", "keysNotFoundServiceId" })
public class CheckCacheService extends CacheServiceBase {

  @NotBlank
  private String keysNotFoundServiceId;
  @NotBlank
  private String keysFoundServiceId;

  public CheckCacheService() {
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      int required = getCacheEntryEvaluators().size();
      if (eval(msg)) {
        msg.setNextServiceId(getKeysFoundServiceId());
      }
      else {
        msg.setNextServiceId(getKeysNotFoundServiceId());
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  protected boolean eval(AdaptrisMessage msg) throws CoreException {
    int count = 0;
    int required = getCacheEntryEvaluators().size();
    Cache cache = retrieveCache();
    for (CacheEntryEvaluator ceg : getCacheEntryEvaluators()) {
      Object key = ceg.getKey(msg);
      count += cache.getKeys().contains(key) ? 1 : 0;
    }
    return count == required;
  }


  public String getKeysNotFoundServiceId() {
    return keysNotFoundServiceId;
  }

  /**
   * Set the service id that will be fired if the keys are not found.
   * 
   * @param id the defaultServiceId to set
   */
  public void setKeysNotFoundServiceId(String id) {
    this.keysNotFoundServiceId = id;
  }

  public String getKeysFoundServiceId() {
    return keysFoundServiceId;
  }

  /**
   * Set the service id that will be fired if the keys are found.
   * 
   * @param id the keysFoundServiceId to set
   */
  public void setKeysFoundServiceId(String id) {
    this.keysFoundServiceId = id;
  }

  @Override
  public boolean isBranching() {
    return true;
  }
}
