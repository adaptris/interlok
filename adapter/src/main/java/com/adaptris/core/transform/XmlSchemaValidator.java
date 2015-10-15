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

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Used with {@link XmlValidationService} to validate an XML message against a schema.
 * <p>
 * This validates an input XML document against a schema. The schema may either be configured or obtained at runtime from a
 * configured metadata key. If a schema and a schemaMetadata key are configured, and a value is stored against the metadata key,
 * this value will be used. If no value is stored against the metadata key the configured schema will be used.
 * </p>
 * 
 * @config xml-schema-validator
 * @license BASIC
 * @author lchan
 * 
 */
@XStreamAlias("xml-schema-validator")
public class XmlSchemaValidator extends MessageValidatorImpl {

  // marshalled
  private String schema;
  private String schemaMetadataKey;

  // transient
  private transient DocumentBuilder parser;
  private transient SchemaFactory schemaFactory;
  private transient Schema schemaObject;

  public XmlSchemaValidator() {

  }

  public XmlSchemaValidator(String schema, String metadataKey) {
    this();
    setSchema(schema);
    setSchemaMetadataKey(metadataKey);
  }

  @Override
  public void validate(AdaptrisMessage msg) throws CoreException {
    try (InputStream in = msg.getInputStream()) {
      Validator validator = this.obtainSchemaToUse(msg).newValidator();
      validator.setErrorHandler(new ErrorHandlerImp());
      validator.validate(new SAXSource(new InputSource(in)));
    }
    catch (SAXParseException e) {
      throw new ServiceException("error validating message [" + e.getMessage() + "] line [" + e.getLineNumber() + "] column ["
          + e.getColumnNumber() + "]", e);
    }
    catch (Exception e) {
      throw new ServiceException("Failed to validate message", e);
    }
  }

  @Override
  public void init() throws CoreException {
    if (this.getSchema() == null) {
      if (this.getSchemaMetadataKey() == null) {
        throw new CoreException("no schema or schemaMetadataKey set");
      }
      else {
        log.info("no schema configured, schema must be set against metadata key [" + this.getSchemaMetadataKey() + "]");
      }
    }
    try {
      this.parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      this.parser.setErrorHandler(new ErrorHandlerImp());
      schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      if (this.getSchema() != null) {
        this.schemaObject = schemaFactory.newSchema(new URL(this.getSchema()));
      }
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
  }

  private Schema obtainSchemaToUse(AdaptrisMessage msg) throws Exception {
    Schema result = null;

    if (isEmpty(getSchemaMetadataKey())) {
      result = this.schemaObject;
    }
    else {
      if (msg.containsKey(getSchemaMetadataKey())) {
        result = schemaFactory.newSchema(new URL(msg.getMetadataValue(this.getSchemaMetadataKey())));
      }
      else {
        result = this.schemaObject;
      }
    }
    return result;
  }

  /**
   * Implementation of ErrorHandler which logs then rethrows exceptions.
   */
  private class ErrorHandlerImp implements ErrorHandler {

    public void error(SAXParseException e) throws SAXException {
      log.debug(e.getMessage());
      throw e;
    }

    public void warning(SAXParseException e) throws SAXException {
      log.debug(e.getMessage());
      throw e;
    }

    public void fatalError(SAXParseException e) throws SAXException {
      log.error(e.getMessage());
      throw e;
    }
  }

  // properties

  /**
   * <p>
   * Sets the schema to validate against. May not be null or empty.
   * </p>
   * 
   * @param s the schema to validate against
   */
  public void setSchema(String s) {
    if ("".equals(s)) {
      throw new IllegalArgumentException("setSchema() may not be empty");
    }
    this.schema = s;
  }

  /**
   * <p>
   * Returns the schema to validate against.
   * </p>
   * 
   * @return the schema to validate against
   */
  public String getSchema() {
    return schema;
  }

  /**
   * Returns the (optional) metadata key against which a schema can be provided at run time.
   * 
   * @return the (optional) metadata key against which a schema can be provided at run time
   */
  public String getSchemaMetadataKey() {
    return schemaMetadataKey;
  }

  /**
   * Sets the (optional) metadata key against which a schema can be provided at run time
   * 
   * @param s the (optional) metadata key against which a schema can be provided at run time
   */
  public void setSchemaMetadataKey(String s) {
    if ("".equals(s)) {
      throw new IllegalArgumentException("setSchemaMetadataKey() may not have an empty param");
    }
    this.schemaMetadataKey = s;
  }
}
