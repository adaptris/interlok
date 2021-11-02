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

import com.adaptris.validation.constraints.ConfigDeprecated;
import org.apache.commons.lang3.StringUtils;

import com.adaptris.core.util.Args;

/**
 * <p>
 * Partial implementation of <code>ConsumeDestination</code> containing
 * behaviour common to all implementations.
 * </p>
 */
@Deprecated()
@ConfigDeprecated(removalVersion = "4.0.0", groups = Deprecated.class)
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

  @Override
  public final String getDeliveryThreadName() {
    return !StringUtils.isBlank(getConfiguredThreadName()) ? getConfiguredThreadName() : null; 
  }

  /**
   * <p>
   * Set a delivery thread name to use.
   * </p>
   * 
   * @param s the delivery thread name to use, may not be null
   */
  public final void setConfiguredThreadName(String s) {
    configuredThreadName = Args.notNull(s, "configuredThreadName");
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
