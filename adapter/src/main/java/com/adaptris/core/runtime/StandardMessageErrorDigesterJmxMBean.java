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

public interface StandardMessageErrorDigesterJmxMBean extends ChildRuntimeInfoComponentMBean {

  MessageErrorDigest getDigest();

  MessageErrorDigest getDigestSubset(int fromIndex);

  MessageErrorDigest getDigestSubset(int fromIndex, int toIndex);

  int getTotalErrorCount();

  /**
   * Remove an entry from this digester.
   * 
   * @param entry the entry
   * @return whether or not the entry was successfully removed.
   * @deprecated since 3.4.1; use {@link #remove(MessageDigestErrorEntry, boolean)} instead.
   */
  @Deprecated
  boolean remove(MessageDigestErrorEntry entry);

  /**
   * Remove an entry from this digester.
   * 
   * @param uniqueId the messageID of the entry
   * @return whether or not the entry was successfully removed.
   * @deprecated since 3.4.1; use {@link #remove(String, boolean)} instead.
   */
  @Deprecated
  boolean remove(String uniqueId);

  /**
   * Remove an entry from this digester.
   * 
   * @param entry the entry
   * @param deleteFile whether or not to attempt to delete the underlying file (if possible).
   * @return whether or not the entry was successfully removed.
   */
  boolean remove(MessageDigestErrorEntry entry, boolean deleteFile);

  /**
   * Remove an entry from this digester.
   * 
   * @param uniqueId the messageID of the entry
   * @param deleteFile whether or not to attempt to delete the underlying file (if possible).
   * @return whether or not the entry was successfully removed.
   */
  boolean remove(String uniqueId, boolean deleteFile);
}
