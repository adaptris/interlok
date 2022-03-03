/*
 * Copyright 2020 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.core.transform.schema;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.SharedConnection;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.services.cache.CacheConnection;
import com.adaptris.core.transform.MessageValidatorImpl;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public abstract class XmlSchemaValidatorImpl extends MessageValidatorImpl {

  private static final int DEFAULT_CACHE_SIZE = 16;
  private static final TimeInterval DEFAULT_CACHE_TTL = new TimeInterval(2L, TimeUnit.HOURS);

  @NotBlank
  @InputFieldHint(expression = true)
  // this will force rfc2396 style validation but we don't know how many people are using
  // file:./relative/path which isn't truly rfc2396 compliant...
  // @UrlExpression
  private String schema;
  @InputFieldDefault(value = "expiring-map-cache, 16 entries, 2 hours")
  @AdvancedConfig
  @Valid
  private AdaptrisConnection schemaCache;

  // transient
  private transient SchemaFactory schemaFactory;
  private transient AdaptrisConnection schemaCacheConnection;

  public XmlSchemaValidatorImpl() {}


  @Override
  public void prepare() throws CoreException {
    schemaCacheConnection =
        ObjectUtils.defaultIfNull(getSchemaCache(), new CacheConnection(new ExpiringMapCache()
            .withExpiration(DEFAULT_CACHE_TTL).withMaxEntries(DEFAULT_CACHE_SIZE)));
    LifecycleHelper.prepare(schemaCacheConnection);
  }

  @Override
  public void init() throws CoreException {
    super.init();
    Args.notBlank(getSchema(), "schema");
    LifecycleHelper.init(schemaCacheConnection);
    schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(schemaCacheConnection);
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(schemaCacheConnection);
  }

  @Override
  public void close() {
    super.close();
    LifecycleHelper.close(schemaCacheConnection);
  }

  protected Schema resolveSchema(AdaptrisMessage msg) throws Exception {
    String schemaUrl = msg.resolve(getSchema());
    return resolveFromCache(schemaUrl);
  }

  @SuppressWarnings({"lgtm [java/xxe]"})
  protected Schema resolveFromCache(String urlString) throws Exception {
    Cache cache = schemaCacheConnection.retrieveConnection(CacheConnection.class).retrieveCache();
    Schema schema = (Schema) cache.get(urlString);
    if (schema == null) {
      schema = schemaFactory.newSchema(new URL(urlString));
      cache.put(urlString, schema);
    }
    return schema;
  }

  /**
   * Sets the schema to validate against. May not be null or empty.
   *
   * @param s the schema to validate against, normally a URL.
   */
  public void setSchema(String s) {
    this.schema = Args.notBlank(s, "schema");
  }

  /**
   * Returns the schema to validate against.
   *
   * @return the schema to validate against
   */
  public String getSchema() {
    return schema;
  }

  public AdaptrisConnection getSchemaCache() {
    return schemaCache;
  }

  /**
   * Configure the internal cache for schemas.
   * <p>
   * While it is possible to configure a distributed cache (a-la ehcache or JSR107) the
   * {@link javax.xml.validation.Schema} object isn't serializable, so you may run into issues. It
   * will be best to stick with {@link ExpiringMapCache} if you want to enable caching. The default
   * behaviour is to cache 16 schemas for a max of 2 hours (last-access) if you don't explicitly
   * configure it differently.
   * </p>
   *
   * @param cache the cache, generally a {@link CacheConnection} or {@link SharedConnection}.
   */
  public void setSchemaCache(AdaptrisConnection cache) {
    this.schemaCache = Args.notNull(cache, "schemaCache");
  }

  public <T extends XmlSchemaValidatorImpl> T withSchemaCache(AdaptrisConnection c) {
    setSchemaCache(c);
    return (T) this;
  }

  public <T extends XmlSchemaValidatorImpl> T withSchema(String c) {
    setSchema(c);
    return (T) this;
  }

}
