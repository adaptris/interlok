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

package com.adaptris.core.services.dynamic;

import com.adaptris.core.CoreException;
import com.adaptris.core.Service;

/**
 * <p> 
 * Implementations provide a store of <code>Service</code>s which may be 
 * retrieved by name.  
 * </p>
 */
public interface ServiceStore {
  
  /**
   * <p>
   * Perform any validation that may be requireed on the store.
   * </p>
   * @throws CoreException if the store is invalid
   */
  void validate() throws CoreException;
  
  /**
   * <p>
   * Returns the <code>Service</code> stored against the passed logical 
   * <code>name</code> if one exists in the store, otherwise null.
   * </p>
   * @param name the name of the <code>Service</code> to obtain
   * @return the named <code>Service</code> or null if it does not exist
   * @throws CoreException wrapping any underlying Exception
   */
  Service obtain(String name) throws CoreException;
}
