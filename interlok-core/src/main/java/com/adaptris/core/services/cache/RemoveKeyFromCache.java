package com.adaptris.core.services.cache;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simplified version of {@link RemoveFromCacheService} that doesn't retrieve the value.
 * 
 * 
 * @config remove-keyfrom-cache
 */
@XStreamAlias("remove-key-from-cache")
@ComponentProfile(summary = "Remove a key from the configured cache", since = "3.9.2", tag = "service,cache",
    recommended = {CacheConnection.class})
public class RemoveKeyFromCache extends SingleKeyCacheService {

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      Cache cache = retrieveCache();
      cache.remove(msg.resolve(getKey()));
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }
}
