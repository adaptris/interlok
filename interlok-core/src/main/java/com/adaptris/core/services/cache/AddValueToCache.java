package com.adaptris.core.services.cache;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Version of {@link AddToCacheService} that doesn't use {@link CacheEntryEvaluators}.
 * 
 * <p>
 * Most of the time, you only want to add a single item to the cache rather than a list of items;
 * this simplified service allows you to do just that. It does not have a list of entries that are
 * evaluated, you simply specify a key and a {@link CacheValueTranslator} which is used to extract
 * the value for storing in the cache; no checking is done of the resulting serializable-ness (or
 * not) of the value, it is simply inserted into the cache.
 * </p>
 * 
 * @config add-single-value-to-cache
 */
@XStreamAlias("add-single-value-to-cache")
@ComponentProfile(summary = "Add a single key/value to the configured cache cache", since = "3.9.2", tag = "service,cache",
    recommended = {CacheConnection.class})
public class AddValueToCache extends SingleKeyValueCacheImpl {

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      Cache cache = retrieveCache();
      cache.put(msg.resolve(getKey()), getValueTranslator().getValueFromMessage(msg));
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }
}
