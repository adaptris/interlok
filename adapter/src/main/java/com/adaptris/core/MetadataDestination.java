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

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Implementation of {@link ProduceDestination} which concatenates message metadata to create a dynamic destination name.
 * 
 * @config metadata-destination
 */
@XStreamAlias("metadata-destination")
@DisplayOrder(order = {"keys"})
public class MetadataDestination implements MessageDrivenDestination {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  @NotNull
  @XStreamImplicit(itemFieldName = "key")
  private List<String> keys;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public MetadataDestination() {
    keys = new ArrayList<String>();
  }

  /**
   * <p>
   * Adds a key to the end of the list of keys to look for in the metadata.
   * </p>
   * @param s a key to look for in the metadata
   */
  public void addKey(String s) {
    keys.add(s);
  }


  /**
   * <p>
   * Returns the <code>List</code> of keys that are used to construct the destination name.
   * </p>
   *
   * @return the <code>List</code> of keys that are used to construct the destination name
   */
  public List<String> getKeys() {
    return keys;
  }

  /**
   * <p>
   * Sets the <code>List</code> of keys that are used to construct the destination name.
   * </p>
   *
   * @param l the <code>List</code> of keys that are used to construct the destination name
   */
  public void setKeys(List<String> l) {
    if (l == null) {
      throw new IllegalArgumentException("metadata keys may not be null");
    }
    keys = l;
  }

  /**
   * <p>
   * Creates a new <code>ProduceDestination</code> by concatenating
   * the values of configurable metadata from the <code>AdaptrisMessage</code>.
   * </p>
   * @param msg the message to process
   * @return the <code>String</code> destination name
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  public String getDestination(AdaptrisMessage msg) throws CoreException {

    String destinationName = "";
    for (String key : keys) {
      if (msg.containsKey(key)) {
        destinationName += StringUtils.defaultIfEmpty(msg.getMetadataValue(key), "");
      }
    }
    if (StringUtils.isBlank(destinationName)) {
      destinationName = null;
    }
    log.debug("dynamic destination [" + destinationName + "]");

    return destinationName;
  }
}
