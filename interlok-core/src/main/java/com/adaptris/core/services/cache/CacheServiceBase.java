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

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Base class that provides common functions used by all cache services
 * 
 */
public abstract class CacheServiceBase extends CacheServiceImpl {

  @Valid
  @XStreamImplicit
  @NotNull
  @AutoPopulated
  private List<CacheEntryEvaluator> cacheEntryEvaluators;

  public CacheServiceBase() {
    setCacheEntryEvaluators(new ArrayList<CacheEntryEvaluator>());
  }

  public List<CacheEntryEvaluator> getCacheEntryEvaluators() {
    return cacheEntryEvaluators;
  }

  /**
   * Set the list of evaluators that will be used to for generate keys for accessing the cache.
   *
   */
  public void setCacheEntryEvaluators(List<CacheEntryEvaluator> list) {
    cacheEntryEvaluators = Args.notNull(list, "cacheEntryEvaluators");
  }

  public void addCacheEntryEvaluator(CacheEntryEvaluator generator) {
    cacheEntryEvaluators.add(generator);
  }

  /**
   * Retrieves the value from the cache and then stores it against the message using the supplied value translator
   *
   */
  protected void addCacheValueToMessage(AdaptrisMessage msg, String key, CacheValueTranslator cvt, boolean quietly)
      throws CoreException {
    Object value = retrieveCache().get(key);
    if (value == null) {
      if (quietly) {
        log.warn("Unable to find value in cache for key [{}], nothing to do", key);
        return;
      } else {
        throw new ServiceException("No value in cache for key " + key);
      }
    }
    cvt.addValueToMessage(msg, value);
  }

}
