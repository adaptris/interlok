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

package com.adaptris.core.jms;

import com.adaptris.core.metadata.MetadataFilter;

/**
 * Interface that abstracts the handling of AdaptrisMessage metadata and JMS
 * Headers away from the MessageTypeTranslator.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public interface MetadataHandlerContext {
  /**
   * <p>
   * Returns true if JMS Headers should be copied as metadata and vice-versa
   * </p>
   *
   * @return true if JMS Headers (as well as JMS Properties) should be copied, otherwise false
   */
  boolean moveJmsHeaders();

  /**
   * @return the reportAllErrors
   */
  boolean reportAllErrors();


  /**
   * Get the metadata filter implementation to be used when converting between AdaptrisMessage and JMS Message objects.
   *
   * @return the metadata filter to use.
   */
  MetadataFilter metadataFilter();

}
