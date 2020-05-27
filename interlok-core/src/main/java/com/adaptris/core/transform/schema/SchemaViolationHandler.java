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

import java.util.Spliterator;
import org.xml.sax.SAXParseException;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

@FunctionalInterface
public interface SchemaViolationHandler {

  /**
   * Handle any schema violations.
   * 
   * @param iterable the iterable representing the violations, non-null and not-empty.
   * @param msg the adaptris message
   */
  void handle(Iterable<SAXParseException> iterable, AdaptrisMessage msg) throws ServiceException;

}
