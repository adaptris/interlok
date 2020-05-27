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

import org.xml.sax.SAXParseException;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.XStreamMarshaller;

/**
 * Uses {@link SchemaViolations} to render schema violations into something meaningful.
 * 
 *
 */
public abstract class ViolationHandlerImpl implements SchemaViolationHandler {

  public static final String DEFAULT_KEY = "schema_violations";

  private transient XStreamMarshaller marshaller;

  public ViolationHandlerImpl() {
    marshaller = new XStreamMarshaller();
  }

  @Override
  public void handle(Iterable<SAXParseException> exceptions, AdaptrisMessage msg)
      throws ServiceException {
    SchemaViolations v = new SchemaViolations();
    exceptions.forEach((e) -> {
      v.addViolation(new SchemaViolation(e));
    });
    render(v, msg);
  }

  protected abstract void render(SchemaViolations violations, AdaptrisMessage msg)
      throws ServiceException;

  protected String toString(SchemaViolations violations) throws Exception {
    return marshaller.marshal(violations);
  }
}
