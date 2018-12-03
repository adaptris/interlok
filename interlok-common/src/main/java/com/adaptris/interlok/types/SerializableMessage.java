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

package com.adaptris.interlok.types;

import java.io.Serializable;
import java.util.Map;

/**
 * Basic message implementation that can be serialized.
 * 
 * 
 */
public interface SerializableMessage extends Serializable {

  public String getUniqueId();

  public void setUniqueId(String uniqueId);

  public String getContent();

  public void setContent(String payload);

  /**
   * Returns a view of all the existing headers associated with the message.
   * <p>
   * Any changes to the returned {@link Map} are not guaranteed to be reflected in underlying map.
   * You should treat the returned Map as a read only view of the current message headers. Use
   * {@link #addMessageHeader(String, String)} or {@link #removeMessageHeader(String)} to manipulate
   * individual headers.
   * </p>
   * 
   * @return a read only view of the messages.
   */
  public Map<String, String> getMessageHeaders();

  /**
   * Overwrite all the headers.
   * <p>
   * Clear and overwrite all the headers
   * </p>
   * 
   * @param metadata
   */
  public void setMessageHeaders(Map<String, String> metadata);

  public void addMessageHeader(String key, String value);

  public void removeMessageHeader(String key);

  public String getContentEncoding();

  public void setContentEncoding(String payloadEncoding);

  public void setNextServiceId(String next);

  public String getNextServiceId();


}
