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

package com.adaptris.core.services;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Branching <code>Service</code> which sets the unique ID of the next <code>Service</code> to apply based on the size of the
 * <code>AdaptrisMessage</code>.
 * </p>
 * <p>
 * If the size of the message is exactly equal to the specified criteria then the smaller service id is selected.
 * </p>
 * 
 * @config size-based-branching-service
 * 
 */
@XStreamAlias("size-based-branching-service")
public class SizeBasedBranchingService extends BranchingServiceImp {
  @NotBlank
  private String greaterThanServiceId;
  @NotBlank
  private String smallerThanServiceId;
  private long sizeCriteriaBytes;

  /**
   * Creates a new instance.
   * <p>
   * size-criteria-bytes = 1024 * 1024 * 10 (10Mb)
   * </p>
   */
  public SizeBasedBranchingService() {
    setSizeCriteriaBytes(1024 * 1024 * 10);
  }


  @Override
  protected void initService() throws CoreException {
    if (getGreaterThanServiceId() == null || getSmallerThanServiceId() == null) {
      throw new CoreException("Service id's may not be null");
    }
  }

  @Override
  protected void closeService() {
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (msg.getSize() > getSizeCriteriaBytes()) {
      msg.setNextServiceId(getGreaterThanServiceId());
    } else {
      msg.setNextServiceId(getSmallerThanServiceId());
    }
  }

  /**
   * @return the greaterThanServiceId
   */
  public String getGreaterThanServiceId() {
    return greaterThanServiceId;
  }

  /**
   * @param serviceId the greaterThanServiceId to set
   */
  public void setGreaterThanServiceId(String serviceId) {
    greaterThanServiceId = serviceId;
  }

  /**
   * @return the smallerThanServiceId
   */
  public String getSmallerThanServiceId() {
    return smallerThanServiceId;
  }

  /**
   * @param serviceId the smallerThanServiceId to set
   */
  public void setSmallerThanServiceId(String serviceId) {
    smallerThanServiceId = serviceId;
  }

  /**
   * @return the sizeCriteriaBytes
   */
  public long getSizeCriteriaBytes() {
    return sizeCriteriaBytes;
  }

  /**
   * @param l the sizeCriteriaBytes to set
   */
  public void setSizeCriteriaBytes(long l) {
    sizeCriteriaBytes = l;
  }

  @Override
  public void prepare() throws CoreException {
  }
}
