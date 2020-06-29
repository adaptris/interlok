package com.adaptris.core.services.cache;

import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Clear the contents of the specified cache.
 *
 * <p>
 * You may wish to invalidate the contents of the cache from time to time outside of the standard
 * expiry conditions. You can use this service to clear the contents of the cache. Since
 * {@link Cache#clear()} defaults to throwing an {@code UnsupportedOperationException} you can opt
 * for this service to silently ignore that exception if the underlying cache doesn't support it.
 * </p>
 *
 * @config clear-cache-service
 */
@XStreamAlias("clear-cache-service")
@ComponentProfile(summary = "Clear the contents of the configured cache", since = "3.10.2",
    tag = "service,cache", recommended = {CacheConnection.class})
@DisplayOrder(order = {"connection", "quietly"})
public class ClearCacheService extends CacheServiceImpl {

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean ignoreUnsupported;

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      retrieveCache().clear();
    } catch (CoreException e) {
      throw ExceptionHelper.wrapServiceException(e);
    } catch (UnsupportedOperationException e) {
      if (!quietly()) {
        throw ExceptionHelper.wrapServiceException(e);
      }
    }
  }

  public Boolean getIgnoreUnsupported() {
    return ignoreUnsupported;
  }

  /**
   * Whether or not to silently eat a possible {@code UnsupportedOperationException} that could be
   * thrown by {@link Cache#clear()}.
   *
   * @param b true to ignore {@code UnsupportedOperationException}, default is false if not
   *        specified.
   */
  public void setIgnoreUnsupported(Boolean b) {
    ignoreUnsupported = b;
  }

  public ClearCacheService withIgnoreUnsupported(Boolean s) {
    setIgnoreUnsupported(s);
    return this;
  }

  private boolean quietly() {
    return BooleanUtils.toBooleanDefaultIfNull(getIgnoreUnsupported(), false);
  }

}
