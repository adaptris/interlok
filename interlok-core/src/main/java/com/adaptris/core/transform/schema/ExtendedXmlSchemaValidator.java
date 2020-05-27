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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;
import javax.validation.Valid;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Validator;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
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
 * This differs from {@link BasicXmlSchemaValidator} since it allows you to configure pluggable
 * behaviours when a validation error is encountered; if you want schema validation to fail
 * processing, then keep using {@link BasicXmlSchemaValidator} as normal.
 * </p>
 * <p>
 * This validates an input XML document against a schema. After first use, it caches the schema for
 * re-use against the URL that was resolved as an expression or from static configuration. This
 * means that until first use, no attempt is made to access the schema URL.
 * </p>
 * 
 * @config extended-xml-schema-validator
 * 
 */
@XStreamAlias("extended-xml-schema-validator")
@DisplayOrder(order = {"schema", "schemaCache"})
@ComponentProfile(summary = "Validate an XML document against a schema with pluggable behaviour",
    recommended = {CacheConnection.class}, since = "3.10.2", tag="xml,schema")
public class ExtendedXmlSchemaValidator extends BasicXmlSchemaValidator {

  @Valid
  @InputFieldDefault(value = "log-and-throw-exception")
  private SchemaViolationHandler schemaViolationHandler;

  public SchemaViolationHandler getSchemaViolationHandler() {
    return schemaViolationHandler;
  }

  /**
   * Specific behaviour when schema violations are encountered.
   * 
   * <p>
   * If not explicitly specified then each violation will be logged and an exception thrown once
   * everything is logged.
   * </p>
   * 
   * @param handler the handler.
   */
  public void setSchemaViolationHandler(SchemaViolationHandler handler) {
    this.schemaViolationHandler = handler;
  }

  public ExtendedXmlSchemaValidator withSchemaViolationHandler(SchemaViolationHandler e) {
    setSchemaViolationHandler(e);
    return this;
  }

  protected SchemaViolationHandler violationHandler() {
    return ObjectUtils.defaultIfNull(getSchemaViolationHandler(), super.violationHandler());
  }

}
