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
 * Partial implementation of <code>ConsumeDestination</code> containing
 * behaviour common to all implementations.
 * </p>
 */
public abstract class ConsumeDestinationImp implements ConsumeDestination {

  private String configuredThreadName;

  /**
   * Returns the unique ID of this destination.
   * <p>
   * The unique ID is based on the destination, filter expression and configured
   * thread name.
   * </p>
   * 
   * @see com.adaptris.core.ConsumeDestination#getUniqueId()
   */
  @Override
  public final String getUniqueId() {
    StringBuffer result = new StringBuffer();

    result.append(getDestination());
    result.append("-");
    result.append(getFilterExpression());
    result.append("-");
    result.append(getConfiguredThreadName());
    return result.toString();
  }

  /**
   * <p>
   * The delivery thread name is the configured thread name if one exists or the
   * unique ID of the object. If the unique ID is greater than 30 characters it
   * is truncated to 30 characters.
   * </p>
   * 
   * @see com.adaptris.core.ConsumeDestination#getDeliveryThreadName()
   */
  @Override
  public final String getDeliveryThreadName() {
    // This should really be moved to <code>Workflow</code>.
    if (notNull(getConfiguredThreadName())) {
      return getConfiguredThreadName();
    }
    int length = getUniqueId().length();
    if (length < 30) {
      return "<" + getUniqueId() + "> delivery thread";
    }
    return "<..." + getUniqueId().substring(length - 27, length)
        + "> delivery thread";
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer();

    result.append("[");
    result.append(this.getClass().getName());
    result.append("[").append(getDestination());

    if (notNull(getConfiguredThreadName())) {
      result.append("] configured thread name [");
      result.append(configuredThreadName);
    }
    result.append("]");
    return result.toString();
  }

  /**
   * <p>
   * Set a delivery thread name to use.
   * </p>
   * 
   * @param s the delivery thread name to use, may not be null
   */
  public final void setConfiguredThreadName(String s) {
    if (s == null) {
      throw new IllegalArgumentException("param may not be null");
    }
    configuredThreadName = s;
  }

  /**
   * <p>
   * Returns the configured thread name.
   * </p>
   * 
   * @return the configured thread name
   */
  public final String getConfiguredThreadName() {
    return configuredThreadName;
  }

  protected static boolean notNull(String s) {
    return s != null && !"".equals(s);
  }
}
