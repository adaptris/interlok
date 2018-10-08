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

package com.adaptris.core;

/**
 * Interface for objects that can be bound to the internal JNDI context.
 * 
 * @author amcgrath
 * 
 */
public interface JndiBindable {

  /**
   * Returns the exact name to bind this object to our {@link javax.naming.Context}.
   * 
   * <p>
   * Specifying a lookupName will not be modified at all when binding to jndi. Therefore you may want to prepend your own chosen
   * subcontexts in this name e.g. "comp/env/"
   * </p>
   * 
   * @return the lookup name.
   */
  String getLookupName();
  
}
