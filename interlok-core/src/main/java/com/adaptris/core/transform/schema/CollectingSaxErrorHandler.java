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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class CollectingSaxErrorHandler implements ErrorHandler {
  private transient Logger log = LoggerFactory.getLogger(BasicXmlSchemaValidator.class);

  // Warnings are intentionally ignored.
  private transient List<SAXParseException> errors, fatalErrors;

  public CollectingSaxErrorHandler() {
    errors = new ArrayList<>();
    fatalErrors = new ArrayList<>();
  }

  @Override
  public void error(SAXParseException e) throws SAXException {
    log.trace(e.getMessage());
    errors.add(e);
  }

  @Override
  public void warning(SAXParseException e) throws SAXException {
    log.trace(e.getMessage());
  }

  // should we just throw an exception instead?
  @Override
  public void fatalError(SAXParseException e) throws SAXException {
    log.error(e.getMessage());
    fatalErrors.add(e);
  }

  public boolean hasErrors() {
    return BooleanUtils.or(new boolean[] {errors.size() > 0, fatalErrors.size() > 0});
  }

  public Iterable<SAXParseException> errors() {
    return errors;
  }

  public Iterable<SAXParseException> fatalErrors() {
    return fatalErrors;
  }

}
