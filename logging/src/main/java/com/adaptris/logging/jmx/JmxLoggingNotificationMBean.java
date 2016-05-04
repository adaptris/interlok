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

package com.adaptris.logging.jmx;

import java.util.Collections;
import java.util.List;

public interface JmxLoggingNotificationMBean {

  /**
   * The default number of log messages to keep for contextual information around an error.
   * 
   */
  int DEFAULT_LOGMSG_COUNT = 150;
  /**
   * The default number of errors to capture and keep a history of.
   */
  int DEFAULT_MAX_ERRORS_COUNT = 100;

  /**
   * Returns any logging saved.
   * 
   * @param index the index to retrieve.
   * @return a list of strings or {@link Collections#EMPTY_LIST} if the index was not valid
   */
  List<String> getErrorLog(int index);

  /**
   * Return the number of errors that are currently being tracked.
   * 
   * @return the number of errors.
   */
  int errorCount();

  /**
   * Remove an error log at the specified index.
   * 
   * @param index the index to remove
   * @return the list of strings that was removed or {@link Collections#EMPTY_LIST} if the index was not valid
   */
  List<String> remove(int index);
}
