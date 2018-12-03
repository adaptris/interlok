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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>TradingRelationshipCreator</code> which populates the <code>TradingRelationship</code> with values
 * returned from configurable metadata keys.
 * </p>
 * 
 * @config metadata-trading-relationship-creator
 */
@XStreamAlias("metadata-trading-relationship-creator")
@DisplayOrder(order = {"sourceKey", "destinationKey", "typeKey"})
public class MetadataTradingRelationshipCreator
  implements TradingRelationshipCreator {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private String sourceKey;
  private String destinationKey;
  private String typeKey;

  /**
   * <p>
   * Creates a new instance.  Default keys are empty <code>String</code>s.
   * </p>
   */
  public MetadataTradingRelationshipCreator() {
    this.sourceKey = "";
    this.destinationKey = "";
    this.typeKey = "";
  }

  public MetadataTradingRelationshipCreator(String srcKey, String destKey, String typKey) {
    this();
    setSourceKey(srcKey);
    setDestinationKey(destKey);
    setTypeKey(typKey);
  }

  /**
   * <p>
   * If any key is empty or if any key returns a value of null or empty a
   * <code>CoreException</code> is thrown.
   * </p>
   * @see com.adaptris.core.TradingRelationshipCreator
   *   #create(com.adaptris.core.AdaptrisMessage) */
  public TradingRelationship create(AdaptrisMessage msg) throws CoreException {
    TradingRelationship result = null;

    String source = this.obtainValue(sourceKey, msg);
    String destination = this.obtainValue(destinationKey, msg);
    String type = this.obtainValue(typeKey, msg);

    result = new TradingRelationship(source, destination, type);

    log.trace("created " + result);

    return result;
  }

  private String obtainValue(String key, AdaptrisMessage msg)
    throws CoreException {

    if (StringUtils.isEmpty(key)) {
      throw new CoreException("empty metadata key");
    }

    String result = msg.getMetadataValue(key);

    if (StringUtils.isEmpty(result)) {
      throw new CoreException("key [" + key + "] returned null or empty");
    }

    return result;
  }

  // getters & setters...

  /**
   * <p>
   * Returns the metadata key used to obtain the destination.
   * </p>
   * @return the metadata key used to obtain the destination
   */
  public String getDestinationKey() {
    return destinationKey;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the destination. May not be null or
   * empty.
   * </p>
   * @param s the metadata key used to obtain the destination
   */
  public void setDestinationKey(String s) {
    this.destinationKey = Args.notBlank(s, "destinationKey");
  }

  /**
   * <p>
   * Returns the metadata key used to obtain the source.
   * </p>
   * @return the metadata key used to obtain the source
   */
  public String getSourceKey() {
    return sourceKey;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the source. May not be null or empty.
   * </p>
   * @param s the metadata key used to obtain the source
   */
  public void setSourceKey(String s) {
    this.sourceKey = Args.notBlank(s, "sourceKey");
  }

  /**
   * <p>
   * Returns the metadata key used to obtain the type.
   * </p>
   * @return the metadata key used to obtain the type
   */
  public String getTypeKey() {
    return typeKey;
  }

  /**
   * <p>
   * Sets the metadata key used to obtain the type.  May not be null or empty.
   * </p>
   * @param s the metadata key used to obtain the type
   */
  public void setTypeKey(String s) {
    this.typeKey = Args.notBlank(s, "typeKey");
  }
}
