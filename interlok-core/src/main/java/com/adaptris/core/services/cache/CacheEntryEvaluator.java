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

import javax.validation.Valid;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Evaluates cache keys and values for the various cache services.
 * 
 * @config cache-entry-evaluator
 */
@XStreamAlias("cache-entry-evaluator")
@DisplayOrder(order ={"friendlyName", "errorOnEmptyKey", "errorOnEmptyValue"})
public class CacheEntryEvaluator {
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @InputFieldDefault(value = "true")
  private Boolean errorOnEmptyKey;
  @InputFieldDefault(value = "true")
  private Boolean errorOnEmptyValue;
  @Valid
  private CacheKeyTranslator keyTranslator;
  @Valid
  private CacheValueTranslator valueTranslator;
  private String friendlyName;

  public CacheEntryEvaluator() {

  }

  protected String getKey(AdaptrisMessage msg) throws ServiceException {

    String value = null;
    try {
      value = keyTranslator().getKeyFromMessage(msg);
      if (value == null && errorOnEmptyKey()) {
        throw new ServiceException("Null cache-key returned");
      }
    }
    catch (Exception e) {
      rethrow(e, "cache-key");
    }
    return value;
  }

  protected Object getValue(AdaptrisMessage msg) throws ServiceException {
    Object value = null;
    try {
      value = valueTranslator().getValueFromMessage(msg);
      if (value == null && errorOnEmptyValue()) {
        throw new ServiceException("Null cache-value returned");
      }
    }
    catch (Exception e) {
      rethrow(e, "cache-value");
    }
    return value;
  }

  private void rethrow(Exception e, String type) throws ServiceException {
    if (e instanceof ServiceException) {
      throw (ServiceException) e;
    }
    else {
      log.warn("Unable to generate {}:{}", type, e.getMessage());
      log.trace(e.getMessage(), e);
    }
  }

  /**
   * Throw an error if we cannot look up the Key value
   *
   * @param bool default is true.
   */
  public void setErrorOnEmptyKey(Boolean bool) {
    errorOnEmptyKey = bool;
  }

  public Boolean getErrorOnEmptyKey() {
    return errorOnEmptyKey;
  }

  boolean errorOnEmptyKey() {
    return BooleanUtils.toBooleanDefaultIfNull(getErrorOnEmptyKey(), true);
  }

  /**
   * Throw an error if we cannot look up the Value to be stored in the cache
   *
   * @param bool default is true.
   */
  public void setErrorOnEmptyValue(Boolean bool) {
    errorOnEmptyValue = bool;
  }

  public Boolean getErrorOnEmptyValue() {
    return errorOnEmptyValue;
  }

  boolean errorOnEmptyValue() {
    return BooleanUtils.toBooleanDefaultIfNull(getErrorOnEmptyValue(), true);
  }

  /**
   * Sets the translator to use to extract the Key value from the message
   *
   * @param translator default is null.
   */
  public void setKeyTranslator(CacheKeyTranslator translator) {
    keyTranslator = Args.notNull(translator, "keyTranslator");
  }


  /**
   * Get the configured key translator.
   *
   * @return the configured key translator.
   */
  public CacheKeyTranslator getKeyTranslator() {
    return keyTranslator;
  }

  /**
   * Get the key translator.
   *
   * @return the configured key translator via {@link #setKeyTranslator(CacheValueTranslator)} or a default translator if null.
   */
  public CacheKeyTranslator keyTranslator() {
    return getKeyTranslator() != null ? getKeyTranslator() : new NullCacheTranslator();
  }

  /**
   * Sets the translator to extract the Value to be stored in the cache
   * 
   * @param translator default is null.
   */
  public void setValueTranslator(CacheValueTranslator translator) {
    valueTranslator = Args.notNull(translator, "valueTranslator");
  }

  /**
   * Get the configured value translator.
   *
   * @return the configured value translator.
   */
  public CacheValueTranslator getValueTranslator() {
    return valueTranslator;
  }

  /**
   * Get the value translator.
   *
   * @return the configured key translator via {@link #setValueTranslator(CacheValueTranslator)} or a default translator if null.
   */
  public CacheValueTranslator valueTranslator() {
    return getValueTranslator() != null ? getValueTranslator() : new NullCacheTranslator();
  }

  public String getFriendlyName() {
    return friendlyName;
  }

  /**
   * Set the name of this cache entry generator for logging purposes.
   *
   * @param name
   */
  public void setFriendlyName(String name) {
    friendlyName = name;
  }

  public String friendlyName() {
    return getFriendlyName() == null ? this.getClass().getSimpleName() : getFriendlyName();
  }

  private class NullCacheTranslator implements CacheValueTranslator, CacheKeyTranslator {

    @Override
    public Object getValueFromMessage(AdaptrisMessage msg) throws CoreException {
      return null;
    }

    @Override
    public void addValueToMessage(AdaptrisMessage msg, Object value) throws CoreException {
    }

    @Override
    public String getKeyFromMessage(AdaptrisMessage msg) throws CoreException {
      return null;
    }
  }
}
