package com.adaptris.core.services.cache;

import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Version of {@link RetrieveFromCacheService} that doesn't use {@link CacheEntryEvaluators}.
 * 
 * <p>
 * Most of the time, you only want to retrieve a single item to the cache rather than a list of
 * items; this simplified service allows you to do just that. It does not have a list of entries
 * that are evaluated, you simply specify a key and a {@link CacheValueTranslator} which is used to
 * insert the value from the cache into the current message.
 * </p>
 * 
 * @config get-single-value-from-cache
 */
@XStreamAlias("get-single-value-from-cache")
@ComponentProfile(summary = "Retrieve a value from the configured cache", since = "3.9.2", tag = "service,cache",
    recommended = {CacheConnection.class})
@DisplayOrder(order = {"connection", "key", "valueTranslator", "exceptionIfNotFound"})
public class GetValueFromCache extends SingleKeyValueCacheImpl {

  @InputFieldDefault(value = "true")
  private Boolean exceptionIfNotFound;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      Cache cache = retrieveCache();
      String key = msg.resolve(getKey());
      Object value = cache.get(key);
      if (value != null || !exceptionIfNotFound()) {
        getValueTranslator().addValueToMessage(msg, value);
      } else {
        throw new ServiceException(String.format("%s not found in cache", key));
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }


  public Boolean getExceptionIfNotFound() {
    return exceptionIfNotFound;
  }

  /**
   * Whether or not to throw an exception if the key is not in the cache.
   *
   * @param b default is true
   */
  public void setExceptionIfNotFound(Boolean b) {
    exceptionIfNotFound = b;
  }

  public GetValueFromCache withExceptionIfNotFound(Boolean b) {
    setExceptionIfNotFound(b);
    return this;
  }

  private boolean exceptionIfNotFound() {
    return BooleanUtils.toBooleanDefaultIfNull(getExceptionIfNotFound(), true);
  }

}
