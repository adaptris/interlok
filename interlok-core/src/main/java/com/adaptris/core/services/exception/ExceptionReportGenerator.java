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

package com.adaptris.core.services.exception;

import org.w3c.dom.Document;

/**
 * Interface for generating an XML report from an exception for use with {@link ExceptionReportService}
 * 
 * @author lchan
 */
public interface ExceptionReportGenerator {

  /**
   * Create a Document from the exception.
   *
   * @param e the exception
   * @param workflow the workflow where it happened
   * @param location where in the workflow it happened.
   * @return a document ready to be merged.
   * @throws Exception on error.
   */
  Document create(Exception e, String workflow, String location) throws Exception;
}
