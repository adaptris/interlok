/*
 * Copyright 2017 Adaptris Ltd.
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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

/**
 * Use with {@link ExceptionReportService} to write the exception as part of the message.
 *
 */
public interface ExceptionSerializer {

  /**
   * Serialize the exception into the adaptris message.
   * 
   * @param e the exception
   * @param msg the message
   * @throws CoreException wrapping other exceptions.
   */
  void serialize(Exception e, AdaptrisMessage msg) throws CoreException;
}
