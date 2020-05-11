package com.adaptris.core.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation that implements {@link CacheEventListener} and logs all the events.
 * 
 * @config cache-event-logger
 *
 */
@XStreamAlias("cache-event-logger")
public class CacheEventLogger implements CacheEventListener {

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void itemEvicted(String key, Object value) {
    log.trace("Key [{}] evicted", key);
  }

  @Override
  public void itemExpired(String key, Object value) {
    log.trace("Key [{}] expired", key);
  }

  @Override
  public void itemPut(String key, Object value) {
    log.trace("Key [{}] put", key);
  }

  @Override
  public void itemRemoved(String key, Object value) {
    log.trace("Key [{}] removed", key);
  }

  @Override
  public void itemUpdated(String key, Object value) {
    log.trace("Key [{}] updated", key);
  }

}
