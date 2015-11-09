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

package com.adaptris.core.services.metadata;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.BranchingServiceImp;
import com.adaptris.core.CoreException;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * Behaviour common to <code>Service</code>s which branch based on
 * <code>AdaptrisMessage</code> metadata.
 * </p>
 */
public abstract class MetadataBranchingServiceImp extends BranchingServiceImp {

  @XStreamImplicit(itemFieldName = "metadata-key")
  private List<String> metadataKeys;
  private String defaultServiceId;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public MetadataBranchingServiceImp() {
    setMetadataKeys(new ArrayList<String>());
  }


  @Override
  protected void initService() throws CoreException {

  }

  @Override
  protected void closeService() {

  }

  /**
   * <p>
   * Returns a <code>List</code> of <code>String</code> metadata keys.
   * </p>
   * @return metadataKeys a <code>List</code> of <code>String</code> metadata
   * keys
   */
  public List<String> getMetadataKeys() {
    return metadataKeys;
  }

  /**
   * <p>
   * Sets a <code>List</code> of <code>String</code> metadata keys.
   * </p>
   * @param l metadataKeys a <code>List</code> of <code>String</code> metadata
   * keys
   */
  public void setMetadataKeys(List l) {
    metadataKeys = l;
  }


  /**
   * <p>
   * Adds a metadata key to the end of the <code>List</code>. (List so you
   * can have the same key more than once if required.)
   * </p>
   * @param metadataKey the metadata key to add, may not be null
   */
  public void addMetadataKey(String metadataKey) {
    if (metadataKey == null || "".equals(metadataKey)) {
      throw new IllegalArgumentException("invalid metadataKey [" + metadataKey + "]");
    }
    getMetadataKeys().add(metadataKey);
  }

  @Override
  public void prepare() throws CoreException {}


  // sets and gets

  /**
   * <p>
   * Returns the ID of an optional default <code>Service</code>.
   * </p>
   * @return the ID of an optional default <code>Service</code>
   */
  public String getDefaultServiceId() {
    return defaultServiceId;
  }

  /**
   * <p>
   * Sets the ID of an optional default <code>Service</code>.
   * </p>
   * @param s the ID of an optional default <code>Service</code>
   */
  public void setDefaultServiceId(String s) {
    defaultServiceId = s;
  }
}
