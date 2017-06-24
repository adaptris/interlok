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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
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

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  private transient HashMap<String, StringBuffer> hm = new FixedSizeMap<String, StringBuffer>();

  @AdvancedConfig
  @InputFieldDefault(value = "50")
  private Integer maxDestinationCacheSize;


  private String retrieveAndCache(URL url) throws Exception {
    String key = url.toExternalForm();
    String result = null;
    if (!hm.containsKey(key)) {
      URLConnection urlConn = url.openConnection();
      InputStream inputStream = urlConn.getInputStream();
      StringBuffer sb = new StringBuffer();
      String buffer = null;
      try (BufferedReader inBuf = new BufferedReader(new InputStreamReader(inputStream))) {
        while ((buffer = inBuf.readLine()) != null) {
          sb.append(buffer);
        }
      }
      log.trace("Retrieved and cached {}", key);
      hm.put(key, sb);
      result = sb.toString();
    }
    else {
      log.trace("Resolve from cache {}", key);
      result = hm.get(key).toString();
    }
    return result;
  }

  /**
   * @see EntityResolver#resolveEntity(String, String)
   */
  @Override
  public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException {
    log.trace("Resolving [{}][{}]", publicId, systemId);
    InputSource result = null;
    try {
      URL myUrl = new URL(systemId); // throws MalformedURLException
      InputSource ret = new InputSource(new StringReader(retrieveAndCache(myUrl)));
      ret.setPublicId(publicId);
      ret.setSystemId(systemId);
      result = ret;
    }
    catch (Exception e) {
      log.trace("Couldn't handle [{}][{}], fallback to default parser behaviour", publicId, systemId);
      result = null;
    }
    return result;
  }

  /**
   * @see URIResolver#resolve(java.lang.String, java.lang.String)
   */
  @Override
  public Source resolve(String href, String base) throws TransformerException {
    log.trace("Resolving [{}][{}]", href, base);
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
      StreamSource ret = new StreamSource(new StringReader(retrieveAndCache(myUrl)), myUrl.toExternalForm());
      result = ret;
    }
    catch (Exception e) {
      log.trace("Couldn't handle [{}][{}], fallback to default parser behaviour", href, base);
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
    return getMaxDestinationCacheSize() != null ? getMaxDestinationCacheSize().intValue() : DEFAULT_MAX_CACHE_SIZE;
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
      return size() > maxDestinationCacheSize();
    }
  }

}
