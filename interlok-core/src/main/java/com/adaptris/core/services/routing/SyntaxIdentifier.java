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

package com.adaptris.core.services.routing;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.conditional.Condition;

/**
 * Interface used by SyntaxRoutingService.
 * <p>
 * The contract for this interface is such that <code>isThisSyntax(String)</code> should only return true, if and only if <b>ALL</b>
 * the configured patterns are matched within the document.
 * </p>
 * <p>
 * Since <strong>3.10.0</strong> this interface extends {@link Condition} which means that it can be used as part of the conditional
 * services; if used in such a manner, then configuration is contextual, get/setDestination will be ignored (but may still have to
 * be configured due to validation
 * </p>
 * 
 * @see SyntaxRoutingService
 * @see Condition
 */
public interface SyntaxIdentifier extends Condition {

  /** Set the configured destination.
   *  <p>This is the value that will be stored against the metadata key
   *  specified by SyntaxRoutingService.
   * @param dest the destination.
   */
  void setDestination(String dest);

  /** Get the configured destination.
   *
   * @return the destination.
   */
  String getDestination();

  /** Determine if this SyntaxIdentifer considers the message to
   *  match all the configured patterns.
   *
   * @param message the message to identify against.
   * @return true if it matches.
   * @throws ServiceException if there was an error with the pattern.
   */
  boolean isThisSyntax(String message) throws ServiceException;

  /**
   * Default implementation for {@link Condition}
   * 
   * @implNote the default implementation just calls {@link #isThisSyntax(String)} with the message content.
   */
  @Override
  default boolean evaluate(AdaptrisMessage msg) throws CoreException {
    return isThisSyntax(msg.getContent());
  }
}
