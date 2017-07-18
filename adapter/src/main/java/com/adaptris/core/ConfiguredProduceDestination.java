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

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Basic implementation of <code>ProduceDestination</code> that has a configured <code>String</code> destination.
 * </p>
 * 
 * @config configured-produce-destination
 */
@XStreamAlias("configured-produce-destination")
@DisplayOrder(order = {"destination"})
public final class ConfiguredProduceDestination implements MessageDrivenDestination {

  @InputFieldHint(expression = true)
  private String destination;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ConfiguredProduceDestination() {
    // default...
    this.setDestination(""); // null protection
  }

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @param s the destination name to use
   */
  public ConfiguredProduceDestination(String s) {
    this.setDestination(s);
  }

  /**
   * <p>
   * Semantic equality is based on the equality of the underlying
   * <code>String</code> destination names.
   * </p>
   *
   * @param obj the <code>Object</code> to test for equality
   * @return true if <code>obj</code> is semantically equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ConfiguredProduceDestination) {
      return this.getDestination().equals(((ConfiguredProduceDestination) obj).getDestination());
    }
    return false;
  }

  /**
   * <p>
   * The hash code of instances of this class is the hash code of the underlying
   * <code>String</code> destination name.
   * </p>
   *
   * @return this instance's hash code
   */
  @Override
  public int hashCode() {
    return destination.hashCode();
  }

  /**
   * @see com.adaptris.core.ProduceDestination
   *      #getDestination(com.adaptris.core.AdaptrisMessage)
   */
  public String getDestination(AdaptrisMessage msg) {
    return msg.resolve(destination);
  }

  /**
   * <p>
   * Returns the name of the destination.
   * </p>
   *
   * @return the name of the destination
   */
  public String getDestination() {
    return destination;
  }

  /**
   * <p>
   * Sets the name of the destination.
   * </p>
   *
   * @param s the name of the destination
   */
  public void setDestination(String s) {
    destination = Args.notNull(s, "destination");
  }
}
