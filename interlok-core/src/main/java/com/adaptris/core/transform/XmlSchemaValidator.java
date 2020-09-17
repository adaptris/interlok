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

package com.adaptris.core.transform;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.lang3.ObjectUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.SharedConnection;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.services.cache.CacheConnection;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Used with {@link XmlValidationService} to validate an XML message against a schema.
 * <p>
 * This validates an input XML document against a schema. After first use, it caches the schema for re-use against the URL that was
 * resolved as an expression or from static configuration. This means that until first use, no attempt is made to access the schema
 * URL.
 * </p>
 *
 * @config xml-schema-validator
 *
 */
@XStreamAlias("xml-schema-validator")
@DisplayOrder(order =
{
    "schema", "schemaCache"
})
@ComponentProfile(summary = "Validate an XML document against a schema", recommended =
{
    CacheConnection.class
})
@Deprecated
@Removal(version = "3.12.0", message="Use basic-xml-schema-validator or extended-xml-schema-validator instead")
public class XmlSchemaValidator extends MessageValidatorImpl {

  private static final int DEFAULT_CACHE_SIZE = 16;
  private static final TimeInterval DEFAULT_CACHE_TTL = new TimeInterval(2L, TimeUnit.HOURS);

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
  private transient boolean classWarningLogged;
  private transient AdaptrisConnection schemaCacheConnection;

  public XmlSchemaValidator() {
  }

  public XmlSchemaValidator(String schema) {
    this();
    setSchema(schema);
  }

  @Override
  public void validate(AdaptrisMessage msg) throws CoreException {
    try (InputStream in = msg.getInputStream()) {
      Validator validator = obtainSchemaToUse(msg).newValidator();
      validator.setErrorHandler(new ErrorHandlerImp());
      validator.validate(new SAXSource(new InputSource(in)));
    }
    catch (SAXParseException e) {
      throw new ServiceException(String.format("Error validating message[%s] line [%s] column[%s]", e.getMessage(),
          e.getLineNumber(), e.getColumnNumber()), e);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException("Failed to validate message", e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    LoggingHelper.logWarning(classWarningLogged, () -> {
      classWarningLogged = true;
    }, "Use basic-xml-schema-validator or extended-xml-schema-validator instead");

    schemaCacheConnection = ObjectUtils.defaultIfNull(getSchemaCache(), new CacheConnection(
        new ExpiringMapCache().withExpiration(DEFAULT_CACHE_TTL).withMaxEntries(DEFAULT_CACHE_SIZE)));
    LifecycleHelper.prepare(schemaCacheConnection);
  }

  @Override
  public void init() throws CoreException {
    try {
      super.init();
      Args.notNull(schemaCacheConnection, "schemaCache");
      Args.notBlank(getSchema(), "schema");
      LifecycleHelper.init(schemaCacheConnection);
      schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
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

  private Schema obtainSchemaToUse(AdaptrisMessage msg) throws Exception {
    String schemaUrl = msg.resolve(getSchema());
    return resolve(schemaUrl);
  }

  private Schema resolve(String urlString) throws Exception {
    Cache cache = schemaCacheConnection.retrieveConnection(CacheConnection.class).retrieveCache();
    Schema schema = (Schema) cache.get(urlString);
    if (schema == null) {
      schema = schemaFactory.newSchema(toURL(urlString));
      cache.put(urlString, schema);
    }
    return schema;
  }

  // This should cope with when people type in c:/a/b/c instead of a URL.
  private URL toURL(String urlString) throws IOException, URISyntaxException {
    try {
      return new URL(urlString);
    }
    catch (MalformedURLException e) {
      return FsHelper.createUrlFromString(urlString, true);
    }
  }

  /**
   * Implementation of ErrorHandler which logs then rethrows exceptions.
   */
  private class ErrorHandlerImp implements ErrorHandler {

    @Override
    public void error(SAXParseException e) throws SAXException {
      log.debug(e.getMessage());
      throw e;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
      log.debug(e.getMessage());
      throw e;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      log.error(e.getMessage());
      throw e;
    }
  }

  // properties

  /**
   * Sets the schema to validate against. May not be null or empty.
   *
   * @param s the schema to validate against, normally a URL.
   */
  public void setSchema(String s) {
    schema = s;
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
   * While it is possible to configure a distributed cache (a-la ehcache or JSR107) the {@link javax.xml.validation.Schema} object
   * isn't serializable, so you may run into issues. It will be best to stick with {@link ExpiringMapCache} if you want to enable
   * caching. The default behaviour is to cache 16 schemas for a max of 2 hours (last-access) if you don't explicitly configure it
   * differently.
   * </p>
   *
   * @param cache the cache, generally a {@link CacheConnection} or {@link SharedConnection}.
   */
  public void setSchemaCache(AdaptrisConnection cache) {
    schemaCache = Args.notNull(cache, "schemaCache");
  }

  public XmlSchemaValidator withSchemaCache(AdaptrisConnection c) {
    setSchemaCache(c);
    return this;
  }
}
