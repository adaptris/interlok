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

package com.adaptris.core.runtime;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;

/**
 * A Digester for handling and recording any errors during the standard workflow processing.
 *
 * @author lchan
 *
 */
public interface MessageErrorDigester extends AdaptrisComponent {

  /**
   * Digest the error that failed.
   *
   * @param message
   */
	void digest(AdaptrisMessage message);

  /**
   * Get the total number of errors that were recorded by this Digester implementatino.
   * 
   * @return the total number of errors.
   */
  int getTotalErrorCount();

}
