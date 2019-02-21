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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.util.NumberUtils;
import com.adaptris.util.URLHelper;
import com.adaptris.util.URLString;
import com.adaptris.util.stream.StreamUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Simple resolver that caches URLs that it has previously encountered.
 * 
 * @author Stuart Ellidge
 * 
 * @config simple-entity-resolver
 */
@XStreamAlias("simple-entity-resolver")
public class Resolver implements EntityResolver, URIResolver {

  private static final int DEFAULT_MAX_CACHE_SIZE = 50;

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  private transient HashMap<String, ByteArrayOutputStream> hm = new FixedSizeMap<String, ByteArrayOutputStream>();

  @AdvancedConfig
  @InputFieldDefault(value = "50")
  private Integer maxDestinationCacheSize;

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean additionalDebug;

  protected InputStream retrieveAndCache(URLString url) throws Exception {
    String key = url.toString();
    InputStream result = null;
    if (!hm.containsKey(key)) {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      StreamUtil.copyAndClose(URLHelper.connect(url), output);
      hm.put(key, output);
      result = new ByteArrayInputStream(output.toByteArray());
    }
    else {
      debugLog("Resolve from cache {}", key);
      result = new ByteArrayInputStream(hm.get(key).toByteArray());
    }
    return result;
  }

  /**
   * @see EntityResolver#resolveEntity(String, String)
   */
  @Override
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException {
    debugLog("Resolving [{}][{}]", publicId, systemId);
    InputSource result = null;
    try {
      InputSource ret = new InputSource(retrieveAndCache(new URLString(systemId)));
      ret.setPublicId(publicId);
      ret.setSystemId(systemId);
      result = ret;
    }
    catch (Exception e) {
      debugLog("Couldn't handle [{}][{}], fallback to default parser behaviour", publicId, systemId);
      result = null;
    }
    return result;
  }

  /**
   * @see URIResolver#resolve(java.lang.String, java.lang.String)
   */
  @Override
  public Source resolve(String href, String base) throws TransformerException {
    debugLog("Resolving [{}][{}]", href, base);
    StreamSource result = null;
    try {
      URL myUrl = null;
      try {
        myUrl = new URL(href);
      }
      catch (Exception ex) {
        // Indicates that the URL was probably relative and therefore Malformed
        int end = base.lastIndexOf('/');
        String url = base.substring(0, end + 1);
        myUrl = new URL(url + href);
      }
      StreamSource ret = new StreamSource(retrieveAndCache(new URLString(myUrl)), myUrl.toExternalForm());
      result = ret;
    }
    catch (Exception e) {
      debugLog("Couldn't handle [{}][{}], fallback to default parser behaviour", href, base);
      result = null;
    }
    return result;
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
    return NumberUtils.toIntDefaultIfNull(getMaxDestinationCacheSize(), DEFAULT_MAX_CACHE_SIZE);
  }

  public Boolean getAdditionalDebug() {
    return additionalDebug;
  }

  public void setAdditionalDebug(Boolean b) {
    this.additionalDebug = b;
  }

  private boolean additionalDebug() {
    return BooleanUtils.toBooleanDefaultIfNull(getAdditionalDebug(), false);
  }

  protected void debugLog(String msg, Object... objects) {
    if (additionalDebug()) {
      log.trace(msg, objects);
    }
  }

  int size() {
    return hm.size();
  }

  private class FixedSizeMap<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 2011031601L;

    public FixedSizeMap() {
      super(DEFAULT_MAX_CACHE_SIZE, 0.75f, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
      return super.size() > maxDestinationCacheSize();
    }
  }

}
