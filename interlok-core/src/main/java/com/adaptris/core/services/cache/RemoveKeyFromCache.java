package com.adaptris.core.services.cache;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Simplified version of {@link RemoveFromCacheService} that doesn't retrieve the value for
* insertion into the message.
*
*
* @config remove-key-from-cache
*/
@JacksonXmlRootElement(localName = "remove-key-from-cache")
@XStreamAlias("remove-key-from-cache")
@ComponentProfile(summary = "Remove a key from the configured cache", since = "3.9.2", tag = "service,cache",
recommended = {CacheConnection.class})
@DisplayOrder(order = {"connection", "key"})
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
