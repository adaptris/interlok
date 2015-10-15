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
 * Represents a destination such as a directory or JMS Queue in the Framework.
 * May optionally have an expression which may be used to filter the 
 * messages to consume.
 * </p>
 */
public interface ConsumeDestination {

  /**
   * <p>
   * Returns the name of the destination.
   * </p>
   * @return the name of the destination
   */
  String getDestination();

  /**
   * <p>
   * Sets the name of the destination.
   * </p>
   * @param destination the name of the destination
   */
  void setDestination(String destination);

  /**
   * <p>
   * Returns the filter expression to use.
   * </p>
   * @return the filter expression to use
   */
  String getFilterExpression();

  /**
   * <p>
   * Sets the filter expression to use.
   * </p>
   * @param filter the filter expression to use
   */
  void setFilterExpression(String filter);

  /**
   * <p>
   * Returns the unique ID.  This method is implemented in 
   * <code>ConsumeDestinationImp</code> and is <code>final</code>.
   * </p>
   * @return the unique ID
   */
  String getUniqueId();

  /**
   * <p>
   * Returns the name to use for the delivery thread.  This method needs to be
   * moved to <code>Workflow</code> or <code>AdaptrisMessageConsumer</code>.
   * </p>
   * @return the name to use for the delivery thread
   */
  String getDeliveryThreadName();
}
