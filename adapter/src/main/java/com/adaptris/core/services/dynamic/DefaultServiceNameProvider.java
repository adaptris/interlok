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
 * Implementation of {@link ServiceNameProvider} which returns the passed {@link TradingRelationship} source, destination and type
 * separated by an (optional) configurable character.
 * </p>
 * 
 * @config default-service-name-provider
 */
@XStreamAlias("default-service-name-provider")
public class DefaultServiceNameProvider extends ServiceNameProviderImp {

  private String separator;

  /**
   * <p>
   * Creates a new instance.  Default separator is "-".
   * </p>
   */
  public DefaultServiceNameProvider() {
    this.setSeparator("-");
  }

  @Override
  protected String retrieveName(TradingRelationship t) throws CoreException {
    if (t == null) {
      throw new IllegalArgumentException("null param");
    }

    StringBuffer result = new StringBuffer();
    result.append(t.getSource());
    result.append(this.getSeparator());
    result.append(t.getDestination());
    result.append(this.getSeparator());
    result.append(t.getType());

    return result.toString();
  }

  /**
   * <p>
   * Gets the separator used to delineate source, destination and type. May not
   * be null.
   * </p>
   *
   * @return the separator
   */
  public String getSeparator() {
    return separator;
  }

  /**
   * <p>
   * Sets the separator used to delineate source, destination and type. May not
   * be null.
   * </p>
   *
   * @param s separator, default is '-'
   */
  public void setSeparator(String s) {
    if (s == null) {
      throw new IllegalArgumentException("null param");
    }
    this.separator = s;
  }
}
