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
 * <p> 
 * Create a <code>TradingRelationship</code> from an 
 * <code>AdaptrisMessage</code>.
 * </p>
 */
public interface TradingRelationshipCreator {

  /**
   * <p>
   * Create a <code>TradingRelationship</code> from an 
   * <code>AdaptrisMessage</code>.
   * </p>
   * @param msg the <code>AdaptrisMessage</code> to process
   * @return a <code>TradingRelationship</code>
   * @throws CoreException wrapping any <code>Exceptions</code> which occur
   */
  TradingRelationship create(AdaptrisMessage msg) throws CoreException;
}
