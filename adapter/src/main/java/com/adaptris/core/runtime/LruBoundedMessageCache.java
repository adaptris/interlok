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

package com.adaptris.core.runtime;

import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * A {@link MessageCache} implementation that uses an {@link ArrayBlockingQueue} to hold the messages.
 * </p>
 * <p>
 * Once the internal queue reaches it's limit, the oldest message will be removed and the newer message added.
 * </p>
 * <p>
 * You can control the limit of the internal queue by configuring "max-messages".
 * </p>
 * @config lru-bounded-message-cache
 * @license BASIC
 * @author Aaron McGrath
 *
 */
@XStreamAlias("lru-bounded-message-cache")
public class LruBoundedMessageCache implements MessageCache {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private static final long DEFAULT_QUEUE_POLL = 100;
  private static final int DEFAULT_MAX_MESSAGES = 100;

  private int maxMessages;

  private transient ArrayBlockingQueue<CacheableAdaptrisMessageWrapper> boundedCache;
  
  private transient ReentrantLock lock = new ReentrantLock(true);

  public LruBoundedMessageCache() {
    maxMessages = DEFAULT_MAX_MESSAGES;
  }

  @Override
  public void put(CacheableAdaptrisMessageWrapper message) {
    log.debug("Caching message: " + message);
    try {
      lock.lock();
      log.debug("Offering message: " + message);
      while (!this.getBoundedCache().offer(message)) {
        try {
          log.debug("Removing message, making space");
          this.getBoundedCache().poll(DEFAULT_QUEUE_POLL, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
          log.warn("Interrupted while adding message '{}' to the message cache.", message.getMessageId());
          break;
        }
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public CacheableAdaptrisMessageWrapper remove(final String messageId) {
    try {
      lock.lock();
      CacheableAdaptrisMessageWrapper wrapper = new CacheableAdaptrisMessageWrapper();
      wrapper.setMessageId(messageId);
      
      log.debug("Removing message: " + wrapper);
      Spliterator<CacheableAdaptrisMessageWrapper> spliterator = this.getBoundedCache().spliterator();
      CacheMessageConsumer cacheMessageConsumer = new CacheMessageConsumer();
      cacheMessageConsumer.setLookupWrapper(wrapper);
      spliterator.forEachRemaining(cacheMessageConsumer);
      
      return cacheMessageConsumer.getReturnValue();
    } finally {
      lock.unlock();
    }
  }
  
  @Override
  public boolean contains(String messageId) {
    try {
      CacheableAdaptrisMessageWrapper wrapper = new CacheableAdaptrisMessageWrapper();
      wrapper.setMessageId(messageId);
      lock.lock();
      return this.getBoundedCache().contains(wrapper);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void init() {
    boundedCache = new ArrayBlockingQueue<>(this.getMaxMessages(), false);
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {
    boundedCache.clear();
  }

  @Override
  public void close() {

  }

  public int getMaxMessages() {
    return maxMessages;
  }

  public void setMaxMessages(int maxMessages) {
    this.maxMessages = maxMessages;
  }

  public ArrayBlockingQueue<CacheableAdaptrisMessageWrapper> getBoundedCache() {
    return boundedCache;
  }

  public void setBoundedCache(ArrayBlockingQueue<CacheableAdaptrisMessageWrapper> boundedCache) {
    this.boundedCache = boundedCache;
  }
  
  class CacheMessageConsumer implements Consumer<CacheableAdaptrisMessageWrapper> {
    private CacheableAdaptrisMessageWrapper lookupWrapper;
    private CacheableAdaptrisMessageWrapper returnValue;
    
    @Override
    public void accept(CacheableAdaptrisMessageWrapper cachedMessage) {
      log.debug("Scanning over message: " + cachedMessage);
      if(cachedMessage.getMessageId().equals(getLookupWrapper().getMessageId())) {
        setReturnValue(cachedMessage);
      }
    }

    public CacheableAdaptrisMessageWrapper getLookupWrapper() {
      return lookupWrapper;
    }

    public void setLookupWrapper(CacheableAdaptrisMessageWrapper lookupWrapper) {
      this.lookupWrapper = lookupWrapper;
    }

    public CacheableAdaptrisMessageWrapper getReturnValue() {
      return returnValue;
    }

    public void setReturnValue(CacheableAdaptrisMessageWrapper returnValue) {
      this.returnValue = returnValue;
    } 
  }

}
