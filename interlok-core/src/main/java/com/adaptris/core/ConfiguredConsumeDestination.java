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

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Basic implementation of <code>ConsumeDestination</code>. Equality (as used by <code>WorkflowList</code> to determine whether a
 * duplicate <code>Workflow</code> may be added is based on the equality of <code>String</code> destination AND <code>String</code>
 * filter.
 * </p>
 * 
 * @config configured-consume-destination
 */
@XStreamAlias("configured-consume-destination")
@DisplayOrder(order = {"destination", "filterExpression"})
@Deprecated(forRemoval = true)
@ConfigDeprecated(removalVersion = "4.0.0", groups = Deprecated.class)
public final class ConfiguredConsumeDestination extends ConsumeDestinationImp {

  private String destination;
  @AdvancedConfig
  private String filterExpression;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ConfiguredConsumeDestination() {
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param dest the destination name
   */
  public ConfiguredConsumeDestination(String dest) {
    this();
    this.setDestination(dest);
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param dest the destination name
   * @param filter the filter expression
   */
  public ConfiguredConsumeDestination(String dest, String filter) {

    this(dest);
    this.setFilterExpression(filter);
  }

  public ConfiguredConsumeDestination(String dest, String filter, String threadname) {
    this(dest);
    this.setFilterExpression(filter);
    setConfiguredThreadName(threadname);
  }

  /**
   * <p>
   * Semantic equality is based on equality of the underlying
   * <code>String</code> destination name and <code>String</code> filter
   * expressions.
   * </p>
   *
   * @param obj the <code>Object</code> to test for equality
   * @return true if <code>obj</code> is semantically equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj != null) { // changed from _destination
      if (obj instanceof ConsumeDestination) {
        ConsumeDestination dest = (ConsumeDestination) obj;
        int count = 0;
        count += areEqual(this.getDestination(), dest.getDestination()) ? 1 : 0;
        count += areEqual(this.getFilterExpression(), dest.getFilterExpression()) ? 1 : 0;
        return count == 2;
      }
    }

    return false;
  }

  private static boolean areEqual(String s1, String s2) {
    boolean result = false;

    if (s1 == null) {
      if (s2 == null) {
        result = true;
      }
    }
    else {
      if (s1.equals(s2)) {
        result = true;
      }
    }

    return result;
  }

  /**
   * <p>
   * The hash code of instances of <code>ConsumeDestination</code> is the hash
   * code of the underlying <code>String</code> destination name and
   * <code>String</code> filter expression.
   * </p>
   *
   * @return this instance's hash code
   */
  @Override
  public int hashCode() {
    int result = 0;

    if (destination != null) {
      result += destination.hashCode();
    }

    if (filterExpression != null) {
      result += filterExpression.hashCode();
    }

    return result;
  }

  /**
   * @see com.adaptris.core.ConsumeDestination #setDestination(java.lang.String)
   */
  public void setDestination(String s) {
    destination = s;
  }

  /** @see com.adaptris.core.ConsumeDestination#getDestination() */
  public String getDestination() {
    return destination;
  }

  /**
   * @see com.adaptris.core.ConsumeDestination
   *      #setFilterExpression(java.lang.String)
   */
  public void setFilterExpression(String s) {
    filterExpression = s;
  }

  /** @see com.adaptris.core.ConsumeDestination#getFilterExpression() */
  public String getFilterExpression() {
    return filterExpression;
  }
}
