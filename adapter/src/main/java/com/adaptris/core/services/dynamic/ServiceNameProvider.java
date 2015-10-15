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
import com.adaptris.core.TradingRelationship;

/**
 * <p> 
 * Returns a logical name for a passed <code>TradingRelationship</code> or
 * <code>TradingRelationship[]</code>.
 * </p>
 */
public interface ServiceNameProvider {

  /**
   * <p>
   * Returns the logical name of the <code>Service</code> to use for a passed
   * <code>TradingRelationship</code>.
   * </p>
   * @param t the <code>TradingRelationship</code>, may not be null
   * @return the logical name of the <code>Service</code> to obtain or null if
   * no name exists
   * @throws CoreException wrapping any underlying Exceptions that occur
   */
  String obtain(TradingRelationship t) throws CoreException;
  
  /**
   * <p>
   * Returns the logical name of the <code>Service</code> to use for the passed
   * <code>TradingRelationship[]</code>.  <code>TradingRelationship</code>s
   * in the array are attempted in turn and are thus expected to become
   * increasingly generic.
   * </p>
   * @param t the <code>TradingRelationship</code>, may not be null
   * @return the logical name of the <code>Service</code> to obtain or null if
   * no name exists
   * @throws CoreException wrapping any underlying Exceptions that occur
   */
  String obtain(TradingRelationship[] t) throws CoreException;
}
