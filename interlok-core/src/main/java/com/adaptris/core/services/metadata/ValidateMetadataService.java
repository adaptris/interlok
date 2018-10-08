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

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Verify that a message has all the required metadata keys set.
 * <p>
 * If any of the required keys does not have a values stored against it, a <code>ServiceException</code> is thrown.
 * </p>
 * 
 * @config validate-metadata-service
 * 
 */
@XStreamAlias("validate-metadata-service")
@AdapterComponent
@ComponentProfile(summary = "Verify a message has all the required metadata keys", tag = "service,metadata")
@DisplayOrder(order = {"requiredKeys"})
public class ValidateMetadataService extends ServiceImp {

  @XStreamImplicit(itemFieldName = "required-key")
  @NotNull
  @AutoPopulated
  private List<String> requiredKeys;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ValidateMetadataService() {
    requiredKeys = new ArrayList<String>();
  }

  public ValidateMetadataService(List<String> list) {
    this();
    setRequiredKeys(list);
  }

  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      for (String requiredKey : requiredKeys) {
        Args.notBlank(msg.getMetadataValue(requiredKey), requiredKey);
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  /**
   * <p>
   * Returns the <code>List</code> of keys which must be present and have non
   * empty values.
   * </p>
   *
   * @return the <code>List</code> of keys which must be present and have non
   *         empty values
   */
  public List<String> getRequiredKeys() {
    return requiredKeys;
  }

  /**
   * <p>
   * Sets the <code>List</code> of keys which must be present and have non empty
   * values.
   * </p>
   *
   * @param l the <code>List</code> of keys which must be present and have non
   *          empty values
   */
  public void setRequiredKeys(List<String> l) {
    requiredKeys = Args.notNull(l, "requiredKeys");
  }

  /**
   * <p>
   * Add a key to the <code>List</code>.
   * </p>
   *
   * @param key the key to add
   */
  public void addRequiredKey(String key) {
    requiredKeys.add(Args.notBlank(key, "requiredKey"));
  }


  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {
  }
  @Override
  public void prepare() throws CoreException {
  }

}
