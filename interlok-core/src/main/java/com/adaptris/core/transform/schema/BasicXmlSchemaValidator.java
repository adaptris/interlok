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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
 * This validates an input XML document against a schema and fails with an exception logging all the
 * exceptions found.
 * </p>
 * 
 * @config basic-xml-schema-validator
 * 
 */
@XStreamAlias("basic-xml-schema-validator")
@DisplayOrder(order = {"schema", "schemaCache"})
@ComponentProfile(summary = "Validate an XML document against a schema",
    recommended = {CacheConnection.class}, since = "3.10.2", tag="xml,schema")
public class BasicXmlSchemaValidator extends XmlSchemaValidatorImpl {

  @Override
  public void validate(AdaptrisMessage msg) throws CoreException {
    // Since we don't throw SAXParseExceptions in the collector, any exceptions
    // shouldn't be schema violations we're basically in the normal error handler chain
    CollectingSaxErrorHandler violationCollector = new CollectingSaxErrorHandler();
    try (InputStream in = msg.getInputStream()) {
      Validator validator = this.resolveSchema(msg).newValidator();
      validator.setErrorHandler(violationCollector);
      validator.validate(new SAXSource(new InputSource(in)));
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException("Failed to validate message", e);
    }
    if (violationCollector.hasErrors()) {
      violationHandler().handle(IterableUtils.chainedIterable(violationCollector.fatalErrors(),
          violationCollector.errors()), msg);
    }
  }

  protected SchemaViolationHandler violationHandler() {
    return (iterable, msg) -> {
      iterable.forEach((e) -> {
        log.error("Error validating message[{}] line [{}] column[{}]", e.getMessage(),
            e.getLineNumber(), e.getColumnNumber());
      });
      throw new ServiceException("Schema Violations logged and error thrown");
    };
  }
}
