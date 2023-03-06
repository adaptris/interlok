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

import java.nio.charset.StandardCharsets;
import org.xml.sax.SAXParseException;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Replaces the current payload with a report of the schema violations.
 * 
 * @config schema-replace-payload-with-violations
 */
@JacksonXmlRootElement(localName = "schema-replace-payload-with-violations")
@XStreamAlias("schema-replace-payload-with-violations")
public class OverwritePayload extends ViolationHandlerImpl {


  @Override
  protected void render(SchemaViolations violations, AdaptrisMessage msg) throws ServiceException {
    try {
      msg.setContent(toString(violations), StandardCharsets.UTF_8.name());
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

}
