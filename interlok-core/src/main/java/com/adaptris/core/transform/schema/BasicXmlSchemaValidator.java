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

import java.io.InputStream;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Validator;
import org.apache.commons.collections4.IterableUtils;
import org.xml.sax.InputSource;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.cache.CacheConnection;
import com.adaptris.core.transform.XmlValidationService;
import com.adaptris.core.util.ExceptionHelper;
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
