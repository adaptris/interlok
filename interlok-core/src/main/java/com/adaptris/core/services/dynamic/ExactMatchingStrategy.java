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
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * <code>ExactMatchingStrategy</code> returns a <code>TradingRelationship[]</code> containing the passed
 * <code>TradingRelationship</code> only, or an empty array if the parameter is null.
 * </p>
 * 
 * @config exact-matching-strategy
 */
@XStreamAlias("exact-matching-strategy")
public class ExactMatchingStrategy implements MatchingStrategy {

  /**
   * <p>
   * Returns a <code>TradingRelationship[]</code> containing the passed
   * <code>TradingRelationship</code> only, or an empty array if the parameter
   * is null.
   * </p>
   * @see com.adaptris.core.services.dynamic.MatchingStrategy
   *   #create(com.adaptris.core.TradingRelationship)
   */
  @Override
  public TradingRelationship[] create(TradingRelationship t)
    throws CoreException {
    
    TradingRelationship[] result = null;
    
    if (t == null) {
      result = new TradingRelationship[0];
    }
    else {
      result = new TradingRelationship[1];
      result[0] = t;
    }
    
    return result;
  }
}
