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

package com.adaptris.core.services.findreplace;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;

/**
 * Interface for handling how find and replace operations occur for {@link FindAndReplaceService}.
 */
public interface ReplacementSource {

  /**
   * <p>
   * Obtains a replacement value for {@link FindAndReplaceService} based on the passed configured value.
   * </p>
   * 
   * @param msg the {@link com.adaptris.core.AdaptrisMessage} being processed
   * @return the String to be used as the replacement.
   */
  String obtainValue(AdaptrisMessage msg) throws ServiceException;
}
