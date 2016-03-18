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
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * <p>
 * Implementation of <code>TradingRelationshipCreator</code> which creates a <code>TradingRelationship</code> with the configured
 * values.
 * </p>
 * 
 * @config configured-trading-relationship-creator
 */
@XStreamAlias("configured-trading-relationship-creator")
@DisplayOrder(order = {"source", "destination", "type"})
public final class ConfiguredTradingRelationshipCreator implements
    TradingRelationshipCreator {

  private String source;
  private String destination;
  private String type;

  /**
   * <p>
   * Creates a new instance. Default keys are empty <code>String</code>s.
   * </p>
   */
  public ConfiguredTradingRelationshipCreator() {
    this.source = "";
    this.destination = "";
    this.type = "";
  }

  public ConfiguredTradingRelationshipCreator(String src, String dest, String type) {
    this();
    setSource(src);
    setDestination(dest);
    setType(type);
  }

  /**
   *
   * @see com.adaptris.core.TradingRelationshipCreator
   *      #create(com.adaptris.core.AdaptrisMessage)
   */
  public TradingRelationship create(AdaptrisMessage msg) throws CoreException {
    return new TradingRelationship(source, destination, type);
  }

  /**
   * <p>
   * Returns the metadata key used to obtain the destination.
   * </p>
   *
   * @return the metadata key used to obtain the destination
   */
  public String getDestination() {
    return destination;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the destination. May not be null.
   * </p>
   *
   * @param s the metadata key used to obtain the destination
   */
  public void setDestination(String s) {
    destination = Args.notNull(s, "destination");
  }

  /**
   * <p>
   * Returns the metadata key used to obtain the source.
   * </p>
   *
   * @return the metadata key used to obtain the source
   */
  public String getSource() {
    return source;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the source. May not be null
   * </p>
   *
   * @param s the metadata key used to obtain the source
   */
  public void setSource(String s) {
    source = Args.notNull(s, "source");
  }

  /**
   * <p>
   * Returns the metadata key used to obtain the type.
   * </p>
   *
   * @return the metadata key used to obtain the type
   */
  public String getType() {
    return type;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the type. May not be null.
   * </p>
   *
   * @param s the metadata key used to obtain the type
   */
  public void setType(String s) {
    type = Args.notNull(s, "type");
  }
}
