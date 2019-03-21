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

import com.adaptris.annotation.Removal;
import com.adaptris.core.CoreException;
import com.adaptris.core.TradingRelationship;

/**
 * <p>
 * Implementations of <code>MatchingStrategy</code> provide alternative
 * <code>TradingRelationship</code>'s for <code>ServiceNameProvider</code>'s to look for if there is
 * no exact match.
 * </p>
 * 
 * @deprecated since 3.8.4 use {@link DynamicServiceExecutor} with a URL based
 *             {@link ServiceExtractor} instead.
 */
@Deprecated
@Removal(version = "3.11.0")
public interface MatchingStrategy {

  /**
   * <p>
   * Returns a <code>TradingRelationship[]</code> containing a list
   * of <code>TradingRelationship</code>s to attempt to match in a 
   * <code>ServiceNameProvider</code>.  <code>ServiceNameProvider</code>s
   * will attempt each element in turn.  Elements should be ordered from
   * more to less specific. 
   * </p>
   * @param t the <code>TradingRelationship</code> to create an array of
   * matches for
   * @return an array of matches
   * @throws CoreException wrapping any underlying <code>Exception</code>
   */
  TradingRelationship[] create(TradingRelationship t) throws CoreException;
}
