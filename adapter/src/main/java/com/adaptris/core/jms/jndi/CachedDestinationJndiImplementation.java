/*
 * Copyright 2015 Adaptris Ltd.
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

package com.adaptris.core.jms.jndi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.jms.JmsActorConfig;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Extension of {@link StandardJndiImplementation} that caches destinations in between calls.
 * <p>
 * This cache is preserved across normal component lifecycle; e.g. the cache remains even if you invoke
 * {@link com.adaptris.core.Channel#requestClose()} and then {@link com.adaptris.core.Channel#requestStart()}. <strong>The only way
 * to reset the cache is to create a new instance.</strong> If this {@link com.adaptris.core.jms.VendorImplementation} is used as
 * part of a
 * {@link com.adaptris.core.StandaloneProducer} then the cache is preserved across the component lifecycles. If used as part of a
 * {@link com.adaptris.core.RetryOnceStandaloneProducer} then the cache will be reset when underlying components is restarted (this
 * producer specifically marshal/unmarshal it's components).
 * </p>
 * 
 * @config cached-destination-jndi-implementation
 * 
 * @see StandardJndiImplementation
 */
@XStreamAlias("cached-destination-jndi-implementation")
public class CachedDestinationJndiImplementation extends StandardJndiImplementation {

  @AdvancedConfig
  private Integer maxDestinationCacheSize;
  private static final int DEFAULT_MAX_CACHE_SIZE = 16;

  protected transient Map<String, Topic> topics = Collections.synchronizedMap(new FixedSizeMap<String, Topic>());
  protected transient Map<String, Queue> queues = Collections.synchronizedMap(new FixedSizeMap<String, Queue>());

  public CachedDestinationJndiImplementation() {
    super();
  }

  public CachedDestinationJndiImplementation(int cacheSize) {
    this();
    setMaxDestinationCacheSize(cacheSize);
  }

  /**
   * Checks to see if the queue cache contains the provided name. If so it returns the cached queue. Otherwise, looks the queue up
   * from JNDI and stores it in the cache for future use, before returning it.
   *
   * @see StandardJndiImplementation#createQueue(java.lang.String, JmsActorConfig)
   */
  @Override
  public Queue createQueue(String name, JmsActorConfig c) throws JMSException {
    if (!queues.containsKey(name)) {
      queues.put(name, super.createQueue(name, c));
    }
    return queues.get(name);
  }

  /**
   * Checks to see if the topic cache contains the provided name. If so it returns the cached topic. Otherwise, looks the topic up
   * from JNDI and stores it in the cache for future use, before returning it.
   *
   * @see StandardJndiImplementation#createTopic(java.lang.String, JmsActorConfig)
   */
  @Override
  public Topic createTopic(String name, JmsActorConfig c) throws JMSException {
    if (!topics.containsKey(name)) {
      topics.put(name, super.createTopic(name, c));
    }
    return topics.get(name);
  }

  /**
   * Get the max number of entries in the cache.
   *
   * @return the maximum number of entries.
   */
  public Integer getMaxDestinationCacheSize() {
    return maxDestinationCacheSize;
  }

  /**
   * Set the max number of entries in the cache.
   * <p>
   * Entries will be removed on a least recently accessed basis.
   * </p>
   *
   * @param maxSize the maximum number of entries, default 16
   */
  public void setMaxDestinationCacheSize(Integer maxSize) {
    maxDestinationCacheSize = maxSize;
  }

  public int maxDestinationCacheSize() {
    return getMaxDestinationCacheSize() != null ? getMaxDestinationCacheSize().intValue() : DEFAULT_MAX_CACHE_SIZE;
  }

  private class FixedSizeMap<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 2011031601L;

    public FixedSizeMap() {
      super(DEFAULT_MAX_CACHE_SIZE, 0.75f, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
      return size() > maxDestinationCacheSize();
    }
  }

}
