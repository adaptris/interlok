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

package com.adaptris.util.text.xml;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.InputStream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.URLString;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Resolver that maps URLs to another URL and caches the results.
 * 
 * 
 * @config mapped-entity-resolver
 */
@XStreamAlias("mapped-entity-resolver")
public class MappedResolver extends Resolver {

  @NotNull
  @AutoPopulated
  @Valid
  private KeyValuePairSet mappings;

  public MappedResolver() {
    super();
    setMappings(new KeyValuePairSet());
  }

  protected InputStream retrieveAndCache(URLString url) throws Exception {
    String key = url.toString();
    String candidate = getMappings().getValueIgnoringKeyCase(key);
    if (!isEmpty(candidate)) {
      debugLog("[{}] mapped to [{}]", key, candidate);
      return super.retrieveAndCache(new URLString(candidate));
    }
    debugLog("No mapping for [{}]; as-is", key);
    return super.retrieveAndCache(url);
  }

  public KeyValuePairSet getMappings() {
    return mappings;
  }

  /**
   * Set the mappings.
   * <p>
   * The key is the "public" URL to match against (e.g. {@code http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd} and the value is
   * the corresponding URL that you really want to retrieve
   * </p>
   * 
   * @param kvps
   */
  public void setMappings(KeyValuePairSet kvps) {
    this.mappings = kvps;
  }

}
