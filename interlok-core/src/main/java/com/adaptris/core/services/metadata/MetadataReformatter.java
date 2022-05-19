/*
 * Copyright 2018 Adaptris Ltd.
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
package com.adaptris.core.services.metadata;

import com.adaptris.core.AdaptrisMessage;

/**
 * Interface that handles metadata reformatting.
 *
 * @apiNote {@link ReformatMetadata} which will always invoke the {@link #reformat(String, AdaptrisMessage)} variant which by
 *          default delegates immediately to {@link #reformat(String, String)}. If you are simple reformatter, then you only need to
 *          implement {@link #reformat(String, String)}
 * @since 3.8.1
 *
 */
public interface MetadataReformatter {

  /**
   * Reformat a metadata value
   *
   * @implNote The default implementation simply returns the input string.
   * @param s the string to reformat
   * @param msgCharset the charset of the message
   * @return the replacment string.
   */
  default String reformat(String s, String msgCharset) throws Exception {
    return s;
  }

  /**
   * Reformat a metadata value
   *
   * @implNote The default implementation calls {@link MetadataReformatter#reformat(String, String)} with
   *           {@link AdaptrisMessage#getContentEncoding()}.
   * @param s the string to reformat
   * @param msg the current Mesage.
   * @return the replacment string.
   */
  default String reformat(String s, AdaptrisMessage msg) throws Exception {
    return reformat(s, msg.getContentEncoding());
  }
}
